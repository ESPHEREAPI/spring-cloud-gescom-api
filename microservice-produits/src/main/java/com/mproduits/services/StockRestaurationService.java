package com.mproduits.services;

import com.mproduits.dto.ApercuImportStockDTO;
import com.mproduits.dto.LigneApercuImportStockDTO;
import com.mproduits.enums.ChampImportStock;
import com.mproduits.enums.ModeRestauration;
import com.mproduits.enums.MovementType;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.MetierException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PointVente;
import com.mproduits.model.Produit;
import com.mproduits.model.StockImportFormat;
import com.mproduits.model.StockMovement;
import com.mproduits.model.HistoriqueRestaurationStock;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.HistoriqueRestaurationStockRepository;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.StockImportFormatRepositories;
import com.mproduits.repositories.StockMovementRepository;
import com.mproduits.security.TenantContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Restauration de stock par boutique a partir d'un fichier Excel dont la
 * compagnie a defini elle-meme le format (voir StockImportFormat,
 * personnalise depuis microservice-administration). Premier import de
 * fichier de tout le projet - le fichier est toujours issu du modele genere
 * par genererModele (memes colonnes, meme ordre), donc le parsing se fait
 * par position de colonne, jamais par detection d'en-tete.
 *
 * previsualiserImport ne modifie rien ; appliquerImport refuse d'ecrire si
 * la moindre ligne est en erreur (tout ou rien), et trace chaque ligne dans
 * HistoriqueRestaurationStock + StockMovement, meme discipline que
 * InventairesService pour les corrections manuelles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockRestaurationService {

    private static final Map<ChampImportStock, String> LIBELLE_CHAMP = Map.of(
            ChampImportStock.REFERENCE, "Reference",
            ChampImportStock.PRODUIT, "Produit",
            ChampImportStock.CATEGORIE, "Categorie",
            ChampImportStock.PRIX_VENTE, "Prix Vente",
            ChampImportStock.PRIX_ACHAT, "Prix Achat",
            ChampImportStock.BOUTIQUE, "Boutique",
            ChampImportStock.QUANTITE, "Quantite");

    private final StockImportFormatRepositories stockImportFormatRepositories;
    private final ProduitRepositories produitRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final PointVenteRepositories pointVenteRepositories;
    private final StockMovementRepository stockMovementRepository;
    private final HistoriqueRestaurationStockRepository historiqueRestaurationStockRepository;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    private record LigneResolue(int ligneNo, String reference, String boutiqueNom, Produit produit,
            Boutique boutique, BigDecimal ancienneQuantite, BigDecimal nouvelleQuantite, String erreur) {
    }

    private StockImportFormat formatCourant(Long compagnieId) {
        return stockImportFormatRepositories.findByCompagnie_Id(compagnieId)
                .filter(f -> !f.getColonnes().isEmpty())
                .orElseThrow(() -> new BadRequestException(
                        "Aucun format de restauration de stock n'a ete configure pour cette compagnie "
                                + "(voir Administration > Initialisation Stock)"));
    }

    @Transactional(readOnly = true)
    public byte[] genererModele(Long boutiqueIdPreselectionnee) {
        Long compagnieId = tenantContext.currentCompagnieId();
        StockImportFormat format = formatCourant(compagnieId);
        List<ChampImportStock> colonnes = format.getColonnes();
        boolean boutiqueDansFormat = colonnes.contains(ChampImportStock.BOUTIQUE);

        List<Boutique> boutiques = boutiqueDansFormat
                ? boutiqueRepositories.findByCompagnie_Id(compagnieId)
                : List.of(boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueIdPreselectionnee, compagnieId)
                        .orElseThrow(() -> new BadRequestException("Boutique introuvable")));

        List<Produit> produits = produitRepositories.findAllByCompagnie_Id(compagnieId).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeletes()))
                .toList();

        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Restauration Stock");

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < colonnes.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(LIBELLE_CHAMP.get(colonnes.get(i)));
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Boutique boutique : boutiques) {
                for (Produit produit : produits) {
                    BigDecimal quantiteActuelle = pointVenteRepositories
                            .findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique, entreprise)
                            .map(PointVente::getStockFinalTheorie)
                            .orElse(BigDecimal.ZERO);
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 0; i < colonnes.size(); i++) {
                        row.createCell(i).setCellValue(valeurColonne(colonnes.get(i), produit, boutique, quantiteActuelle));
                    }
                }
            }
            for (int i = 0; i < colonnes.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la generation du modele de restauration de stock", e);
            throw new MetierException("Erreur lors de la generation du modele Excel");
        }
    }

    private String valeurColonne(ChampImportStock champ, Produit produit, Boutique boutique, BigDecimal quantiteActuelle) {
        return switch (champ) {
            case REFERENCE -> Optional.ofNullable(produit.getReference()).orElse("");
            case PRODUIT -> Optional.ofNullable(produit.getLibelle()).orElse("");
            case CATEGORIE -> produit.getCategorie() != null ? produit.getCategorie().getLibelle() : "";
            case PRIX_VENTE -> produit.getPrixVente() != null ? produit.getPrixVente().toPlainString() : "0";
            case PRIX_ACHAT -> produit.getPrixAchat() != null ? produit.getPrixAchat().toPlainString() : "0";
            case BOUTIQUE -> Optional.ofNullable(boutique.getNom()).orElse("");
            case QUANTITE -> quantiteActuelle.toPlainString();
        };
    }

    @Transactional(readOnly = true)
    public ApercuImportStockDTO previsualiserImport(MultipartFile fichier, Long boutiqueIdPreselectionnee, ModeRestauration mode) {
        List<LigneResolue> lignes = resoudreLignes(fichier, boutiqueIdPreselectionnee, mode);
        List<LigneApercuImportStockDTO> dto = lignes.stream()
                .map(l -> new LigneApercuImportStockDTO(l.ligneNo(), l.reference(), l.boutiqueNom(),
                        l.ancienneQuantite(), l.nouvelleQuantite(), l.erreur()))
                .toList();
        return new ApercuImportStockDTO(dto);
    }

    @Transactional
    public String appliquerImport(MultipartFile fichier, Long boutiqueIdPreselectionnee, ModeRestauration mode, String username) {
        List<LigneResolue> lignes = resoudreLignes(fichier, boutiqueIdPreselectionnee, mode);
        if (lignes.isEmpty()) {
            throw new BadRequestException("Le fichier ne contient aucune ligne exploitable");
        }
        if (lignes.stream().anyMatch(l -> l.erreur() != null)) {
            throw new BadRequestException("Le fichier contient des lignes en erreur - corrigez-le avant d'appliquer la restauration");
        }

        String batchId = UUID.randomUUID().toString();
        Date maintenant = new Date();
        LocalDateTime maintenantLdt = LocalDateTime.now();

        for (LigneResolue ligne : lignes) {
            PointVente pointVente = pointVenteRepositories
                    .findLatestActiveByProduitBoutiqueAndEntreprise(ligne.produit(), ligne.boutique(),
                            entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId()))
                    .orElseThrow(() -> new BadRequestException(
                            "Aucun point de vente actif pour " + ligne.reference() + " / " + ligne.boutiqueNom()));

            pointVente.setStockFinalTheorie(ligne.nouvelleQuantite());
            pointVenteRepositories.save(pointVente);

            StockMovement mouvement = StockMovement.builder()
                    .produit(ligne.produit())
                    .pointVente(pointVente)
                    .quantite(ligne.nouvelleQuantite().subtract(ligne.ancienneQuantite()))
                    .stockAvant(ligne.ancienneQuantite())
                    // MovementType.INITIALISATION serait plus precis mais
                    // type_mouvement est une colonne ENUM MySQL native creee
                    // avant l'ajout de toute nouvelle valeur (meme piege que
                    // Permission.operationType/PRINT) - reutilise AJUSTEMENT,
                    // deja present en base, le motif ci-dessous precise le
                    // contexte reel (restauration + lot).
                    .typeMouvement(MovementType.AJUSTEMENT)
                    .motif("Restauration de stock (" + mode + "), lot " + batchId)
                    .usernameCreate(username)
                    .dateCreation(maintenantLdt)
                    .build();
            stockMovementRepository.save(mouvement);

            HistoriqueRestaurationStock historique = new HistoriqueRestaurationStock();
            historique.setBatchId(batchId);
            historique.setProduit(ligne.produit());
            historique.setBoutique(ligne.boutique());
            historique.setCompagnie(ligne.boutique().getCompagnie());
            historique.setAncienneQuantite(ligne.ancienneQuantite());
            historique.setNouvelleQuantite(ligne.nouvelleQuantite());
            historique.setMode(mode);
            historique.setUtilisateur(username);
            historique.setDateRestauration(maintenant);
            historiqueRestaurationStockRepository.save(historique);
        }

        log.info("Restauration de stock appliquee : lot={}, {} lignes, mode={}, utilisateur={}",
                batchId, lignes.size(), mode, username);
        return batchId;
    }

    private List<LigneResolue> resoudreLignes(MultipartFile fichier, Long boutiqueIdPreselectionnee, ModeRestauration mode) {
        Long compagnieId = tenantContext.currentCompagnieId();
        StockImportFormat format = formatCourant(compagnieId);
        List<ChampImportStock> colonnes = format.getColonnes();
        int indexReference = colonnes.indexOf(ChampImportStock.REFERENCE);
        int indexQuantite = colonnes.indexOf(ChampImportStock.QUANTITE);
        int indexBoutique = colonnes.indexOf(ChampImportStock.BOUTIQUE);

        Boutique boutiquePreselectionnee = indexBoutique < 0
                ? boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueIdPreselectionnee, compagnieId)
                        .orElseThrow(() -> new BadRequestException("Boutique introuvable"))
                : null;

        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        List<LigneResolue> resultat = new ArrayList<>();

        try (InputStream in = fichier.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || estLigneVide(row, colonnes.size())) {
                    continue;
                }
                int ligneNo = r + 1;
                String reference = lireTexte(row, indexReference);
                String boutiqueNom = indexBoutique >= 0 ? lireTexte(row, indexBoutique)
                        : boutiquePreselectionnee.getNom();

                Produit produit = produitRepositories.findByReferenceAndCompagnie_Id(reference, compagnieId).orElse(null);
                if (produit == null) {
                    resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, null, null, null, null,
                            "Reference introuvable : " + reference));
                    continue;
                }

                Boutique boutique = indexBoutique >= 0
                        ? boutiqueRepositories.findByNomIgnoreCaseAndCompagnie_Id(boutiqueNom, compagnieId).orElse(null)
                        : boutiquePreselectionnee;
                if (boutique == null) {
                    resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, produit, null, null, null,
                            "Boutique introuvable : " + boutiqueNom));
                    continue;
                }

                BigDecimal quantiteFichier;
                try {
                    quantiteFichier = lireNombre(row, indexQuantite);
                } catch (NumberFormatException e) {
                    resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, produit, boutique, null, null,
                            "Quantite invalide"));
                    continue;
                }
                if (quantiteFichier == null || quantiteFichier.compareTo(BigDecimal.ZERO) < 0) {
                    resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, produit, boutique, null, null,
                            "Quantite manquante ou negative"));
                    continue;
                }

                BigDecimal ancienneQuantite = pointVenteRepositories
                        .findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique, entreprise)
                        .map(PointVente::getStockFinalTheorie)
                        .orElse(null);
                if (ancienneQuantite == null) {
                    resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, produit, boutique, null, null,
                            "Aucun point de vente actif pour ce produit dans cette boutique"));
                    continue;
                }

                BigDecimal nouvelleQuantite = mode == ModeRestauration.AJOUT
                        ? ancienneQuantite.add(quantiteFichier)
                        : quantiteFichier;

                resultat.add(new LigneResolue(ligneNo, reference, boutiqueNom, produit, boutique,
                        ancienneQuantite, nouvelleQuantite, null));
            }
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier de restauration de stock", e);
            throw new MetierException("Fichier illisible - verifiez qu'il s'agit bien du modele genere par cet ecran");
        }

        return resultat;
    }

    private boolean estLigneVide(Row row, int nbColonnes) {
        for (int i = 0; i < nbColonnes; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !lireTexte(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String lireTexte(Row row, int index) {
        if (index < 0) {
            return "";
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private BigDecimal lireNombre(Row row, int index) {
        if (index < 0) {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String texte = lireTexte(row, index);
        if (texte.isBlank()) {
            return null;
        }
        return new BigDecimal(texte.replace(",", "."));
    }
}
