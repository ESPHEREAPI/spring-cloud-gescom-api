package com.mproduits.services;

import com.mproduits.dto.ApercuImportStockDTO;
import com.mproduits.dto.LigneApercuImportStockDTO;
import com.mproduits.enums.ChampImportStock;
import com.mproduits.enums.ModeRestauration;
import com.mproduits.enums.MovementType;
import com.mproduits.enums.TypeMagasin;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.MetierException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.Compagnie;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Magasin;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixAchat;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.model.StockImportFormat;
import com.mproduits.model.StockMovement;
import com.mproduits.model.HistoriqueRestaurationStock;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.HistoriqueRestaurationStockRepository;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
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
 *
 * Cas particulier : une reference absente du catalogue n'est PAS une erreur
 * bloquante (usage vise : initialiser une boutique de A a Z) - le produit,
 * son point de vente, son prix de vente (PrixArticles) et, si fourni, son
 * prix d'achat (PrixAchat) sont crees a l'application. Un Magasin "point de
 * vente" par defaut est cree pour la boutique s'il n'en existe encore aucun
 * (voir resolveOuCreerMagasinPointDeVente).
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
    private final CategorieRepositories categorieRepositories;
    private final MagasinRepositories magasinRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final PrixAchatRepositories prixAchatRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    // produit == null et nouveauProduit == true : le produit sera cree par
    // appliquerImport (voir creerProduitEtStockInitial), pas seulement mis a
    // jour - produitLibelle/categorieLibelle/prixVente/prixAchat portent alors
    // les valeurs du fichier necessaires a cette creation.
    private record LigneResolue(int ligneNo, String reference, String boutiqueNom, Produit produit,
            Boutique boutique, BigDecimal ancienneQuantite, BigDecimal nouvelleQuantite, String erreur,
            boolean nouveauProduit, String produitLibelle, String categorieLibelle,
            BigDecimal prixVente, BigDecimal prixAchat) {
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
                        l.ancienneQuantite(), l.nouvelleQuantite(), l.erreur(), l.nouveauProduit()))
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

        Entreprise entrepriseActive = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        for (LigneResolue ligne : lignes) {
            Produit produit;
            PointVente pointVente;

            if (ligne.nouveauProduit()) {
                produit = creerProduit(ligne);
                Magasin magasin = resolveOuCreerMagasinPointDeVente(ligne.boutique());
                pointVente = new PointVente();
                pointVente.setBoutique(ligne.boutique());
                pointVente.setDepotId(magasin);
                pointVente.setEntreprise(entrepriseActive);
                pointVente.setProduit(produit);
                pointVente.setDateReception(maintenant);
                // Premiere reception pour ce produit dans cette boutique :
                // stockInitial reste a zero et entreeProduit porte la
                // quantite (meme convention que ServiceCommande, "premiere
                // reception") - stockInitial ET entreeProduit mis tous les
                // deux a la quantite du fichier faisait apparaitre le double
                // de la vraie quantite recue sur l'ecran Approvisionnement
                // (Quantite = entreeProduit + stockInitial).
                pointVente.setStockInitial(BigDecimal.ZERO);
                pointVente.setStockFinalTheorie(ligne.nouvelleQuantite());
                pointVente.setEntreeProduit(ligne.nouvelleQuantite());
                pointVente.setSortiProduit(BigDecimal.ZERO);
                pointVente = pointVenteRepositories.save(pointVente);

                PrixArticles prixArticles = new PrixArticles();
                prixArticles.setActif(true);
                prixArticles.setDateCreation(maintenant);
                prixArticles.setEntreprise(entrepriseActive);
                prixArticles.setPointVente(pointVente);
                prixArticles.setPrixVenteNet(ligne.prixVente());
                prixArticles.setPrixVenteTTC(ligne.prixVente());
                prixArticles.setRemise(BigDecimal.ZERO);
                prixArticles.setTva(BigDecimal.ZERO);
                prixArticlesRepositories.save(prixArticles);

                if (ligne.prixAchat() != null && ligne.prixAchat().compareTo(BigDecimal.ZERO) > 0) {
                    PrixAchat prixAchat = new PrixAchat();
                    prixAchat.setProduit(produit);
                    prixAchat.setDatedebut(maintenant);
                    prixAchat.setPrix(ligne.prixAchat());
                    prixAchat.setUsercreat(username);
                    prixAchatRepositories.save(prixAchat);
                }
            } else {
                produit = ligne.produit();
                pointVente = pointVenteRepositories
                        .findLatestActiveByProduitBoutiqueAndEntreprise(produit, ligne.boutique(), entrepriseActive)
                        .orElseThrow(() -> new BadRequestException(
                                "Aucun point de vente actif pour " + ligne.reference() + " / " + ligne.boutiqueNom()));
                // entreeProduit REMPLACE par la nouvelle quantite (pas
                // accumule, comme InventairesService.saveStock) et
                // sortiProduit remis a zero. stockInitial remis a zero aussi
                // (contrairement a InventairesService, qui n'y touche pas) :
                // une ligne deja creee par une restauration anterieure au
                // correctif du double-comptage y avait la quantite entiere,
                // et sans cette remise a zero ici, rejouer la restauration
                // ne suffisait pas a corriger l'affichage (Quantite =
                // entreeProduit + stockInitial) sur l'ecran Approvisionnement.
                pointVente.setStockFinalTheorie(ligne.nouvelleQuantite());
                pointVente.setEntreeProduit(ligne.nouvelleQuantite());
                pointVente.setStockInitial(BigDecimal.ZERO);
                pointVente.setSortiProduit(BigDecimal.ZERO);
                pointVenteRepositories.save(pointVente);
            }

            StockMovement mouvement = StockMovement.builder()
                    .produit(produit)
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
            historique.setProduit(produit);
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

    /**
     * findByReferenceAndCompagnie_Id plante (IncorrectResultSizeDataAccessException)
     * si un import anterieur a deja cree un doublon pour cette reference -
     * prend le premier plutot que de faire planter toute la restauration ;
     * le doublon en base reste une donnee a nettoyer separement, mais ne
     * doit plus bloquer l'ecran.
     */
    private Produit resolveProduitTolerantDoublons(String reference, Long compagnieId) {
        List<Produit> trouves = produitRepositories.findAllByReferenceAndCompagnie_Id(reference, compagnieId);
        if (trouves.size() > 1) {
            log.warn("Reference '{}' en double en base pour la compagnie {} ({} produits) - le premier est utilise",
                    reference, compagnieId, trouves.size());
        }
        return trouves.isEmpty() ? null : trouves.get(0);
    }

    /**
     * Cree le produit manquant a partir des colonnes informatives du fichier
     * (Produit/Categorie servent ici, pas seulement d'affichage). Reference
     * et compagnie sont les seuls champs garantis ; Produit (libelle) retombe
     * sur la reference si absente du format ou vide.
     */
    private Produit creerProduit(LigneResolue ligne) {
        Long compagnieId = tenantContext.currentCompagnieId();
        Produit produit = new Produit();
        produit.setReference(ligne.reference());
        produit.setLibelle(ligne.produitLibelle() != null && !ligne.produitLibelle().isBlank()
                ? ligne.produitLibelle() : ligne.reference());
        produit.setDeletes(Boolean.FALSE);
        produit.setPrixVenteModifiable(Boolean.FALSE);
        produit.setUsername(tenantContext.currentUsername());
        produit.setCompagnie(new Compagnie(compagnieId));
        if (ligne.categorieLibelle() != null && !ligne.categorieLibelle().isBlank()) {
            produit.setCategorie(resolveOuCreerCategorie(ligne.categorieLibelle(), compagnieId));
        }
        return produitRepositories.save(produit);
    }

    private Categories resolveOuCreerCategorie(String libelle, Long compagnieId) {
        return categorieRepositories.findByLibelleIgnoreCaseAndCompagnie_Id(libelle, compagnieId)
                .orElseGet(() -> {
                    Categories categorie = new Categories();
                    categorie.setLibelle(libelle);
                    categorie.setCompagnieId(compagnieId);
                    return categorieRepositories.save(categorie);
                });
    }

    /**
     * Depot "point de vente" de la boutique - cree une fois pour toutes s'il
     * n'en existe encore aucun, pour qu'une compagnie qui initialise une
     * nouvelle boutique de A a Z n'ait pas a configurer un Magasin a la main
     * au prealable (voir Boutique/Magasin, aucune creation automatique
     * n'existait avant pour ce cas).
     */
    private Magasin resolveOuCreerMagasinPointDeVente(Boutique boutique) {
        Long compagnieId = tenantContext.currentCompagnieId();
        return magasinRepositories.findFirstByBoutique_IdAndCompagnie_Id(boutique.getId(), compagnieId)
                .orElseGet(() -> {
                    Magasin magasin = new Magasin();
                    magasin.setLibelle("Point de vente - " + boutique.getNom());
                    magasin.setCode(boutique.getCode());
                    magasin.setBoutiqueId(boutique);
                    magasin.setTypeMagasin(TypeMagasin.POINT_DE_VENTE);
                    magasin.setCompagnieId(compagnieId);
                    return magasinRepositories.save(magasin);
                });
    }

    private List<LigneResolue> resoudreLignes(MultipartFile fichier, Long boutiqueIdPreselectionnee, ModeRestauration mode) {
        Long compagnieId = tenantContext.currentCompagnieId();
        StockImportFormat format = formatCourant(compagnieId);
        List<ChampImportStock> colonnes = format.getColonnes();
        int indexReference = colonnes.indexOf(ChampImportStock.REFERENCE);
        int indexQuantite = colonnes.indexOf(ChampImportStock.QUANTITE);
        int indexBoutique = colonnes.indexOf(ChampImportStock.BOUTIQUE);
        int indexProduit = colonnes.indexOf(ChampImportStock.PRODUIT);
        int indexCategorie = colonnes.indexOf(ChampImportStock.CATEGORIE);
        int indexPrixVente = colonnes.indexOf(ChampImportStock.PRIX_VENTE);
        int indexPrixAchat = colonnes.indexOf(ChampImportStock.PRIX_ACHAT);

        Boutique boutiquePreselectionnee = indexBoutique < 0
                ? boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueIdPreselectionnee, compagnieId)
                        .orElseThrow(() -> new BadRequestException("Boutique introuvable"))
                : null;

        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        List<LigneResolue> resultat = new ArrayList<>();

        // Une meme reference peut legitimement apparaitre sur plusieurs
        // lignes du fichier (plusieurs lots recus pour le meme produit,
        // dans la meme boutique) - leurs quantites sont cumulees en une
        // seule ligne resolue plutot que de creer un doublon de produit ou
        // de faire planter la restauration (voir resolveProduitTolerantDoublons).
        record LigneBrute(int ligneNo, String reference, String boutiqueNom, String produitLibelle,
                String categorieLibelle, BigDecimal prixVente, BigDecimal prixAchat, BigDecimal quantite, String erreur) {
        }

        try (InputStream in = fichier.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<LigneBrute> brutes = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || estLigneVide(row, colonnes.size())) {
                    continue;
                }
                int ligneNo = r + 1;
                String reference = lireTexte(row, indexReference);
                String boutiqueNom = indexBoutique >= 0 ? lireTexte(row, indexBoutique)
                        : boutiquePreselectionnee.getNom();
                String produitLibelle = lireTexte(row, indexProduit);
                String categorieLibelle = lireTexte(row, indexCategorie);
                BigDecimal prixVenteFichier = lireNombreOuZero(row, indexPrixVente);
                BigDecimal prixAchatFichier = lireNombreOuZero(row, indexPrixAchat);

                if (reference.isBlank()) {
                    brutes.add(new LigneBrute(ligneNo, reference, boutiqueNom, produitLibelle, categorieLibelle,
                            prixVenteFichier, prixAchatFichier, null, "Reference manquante"));
                    continue;
                }

                BigDecimal quantiteFichier;
                try {
                    quantiteFichier = lireNombre(row, indexQuantite);
                } catch (NumberFormatException e) {
                    brutes.add(new LigneBrute(ligneNo, reference, boutiqueNom, produitLibelle, categorieLibelle,
                            prixVenteFichier, prixAchatFichier, null, "Quantite invalide"));
                    continue;
                }
                if (quantiteFichier == null || quantiteFichier.compareTo(BigDecimal.ZERO) < 0) {
                    brutes.add(new LigneBrute(ligneNo, reference, boutiqueNom, produitLibelle, categorieLibelle,
                            prixVenteFichier, prixAchatFichier, null, "Quantite manquante ou negative"));
                    continue;
                }

                brutes.add(new LigneBrute(ligneNo, reference, boutiqueNom, produitLibelle, categorieLibelle,
                        prixVenteFichier, prixAchatFichier, quantiteFichier, null));
            }

            for (LigneBrute lb : brutes) {
                if (lb.erreur() != null) {
                    resultat.add(new LigneResolue(lb.ligneNo(), lb.reference(), lb.boutiqueNom(), null, null, null,
                            null, lb.erreur(), false, null, null, null, null));
                }
            }

            // Regroupe les lignes valides par couple (reference, boutique) -
            // ordre de premiere apparition conserve (LinkedHashMap).
            Map<String, List<LigneBrute>> groupes = new java.util.LinkedHashMap<>();
            for (LigneBrute lb : brutes) {
                if (lb.erreur() == null) {
                    groupes.computeIfAbsent(lb.reference() + " " + lb.boutiqueNom(), k -> new ArrayList<>()).add(lb);
                }
            }

            for (List<LigneBrute> groupe : groupes.values()) {
                LigneBrute premiere = groupe.get(0);
                String reference = premiere.reference();
                String boutiqueNom = premiere.boutiqueNom();
                BigDecimal quantiteFichier = groupe.stream()
                        .map(LigneBrute::quantite)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Produit introuvable : pas une erreur bloquante en soi -
                // appliquerImport le creera (voir creerProduit), a condition
                // que la boutique reste valide ci-dessous.
                Produit produit = resolveProduitTolerantDoublons(reference, compagnieId);
                boolean nouveauProduit = produit == null;

                Boutique boutique = indexBoutique >= 0
                        ? boutiqueRepositories.findByNomIgnoreCaseAndCompagnie_Id(boutiqueNom, compagnieId).orElse(null)
                        : boutiquePreselectionnee;
                if (boutique == null) {
                    resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, produit, null, null, null,
                            "Boutique introuvable : " + boutiqueNom, nouveauProduit, premiere.produitLibelle(),
                            premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
                    continue;
                }

                if (nouveauProduit) {
                    resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, null, boutique,
                            BigDecimal.ZERO, quantiteFichier, null, true, premiere.produitLibelle(),
                            premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
                    continue;
                }

                BigDecimal ancienneQuantite = pointVenteRepositories
                        .findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique, entreprise)
                        .map(PointVente::getStockFinalTheorie)
                        .orElse(null);
                if (ancienneQuantite == null) {
                    resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, produit, boutique, null,
                            null, "Aucun point de vente actif pour ce produit dans cette boutique", false,
                            premiere.produitLibelle(), premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
                    continue;
                }

                BigDecimal nouvelleQuantite = mode == ModeRestauration.AJOUT
                        ? ancienneQuantite.add(quantiteFichier)
                        : quantiteFichier;

                resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, produit, boutique,
                        ancienneQuantite, nouvelleQuantite, null, false, premiere.produitLibelle(),
                        premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
            }

            resultat.sort(java.util.Comparator.comparingInt(LigneResolue::ligneNo));
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

    // Colonnes purement informatives (Prix Vente/Prix Achat) : une valeur
    // absente ou illisible ne doit jamais bloquer la ligne, contrairement a
    // Quantite - retombe sur ZERO plutot que de lever une exception.
    private BigDecimal lireNombreOuZero(Row row, int index) {
        try {
            BigDecimal valeur = lireNombre(row, index);
            return valeur != null ? valeur : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
