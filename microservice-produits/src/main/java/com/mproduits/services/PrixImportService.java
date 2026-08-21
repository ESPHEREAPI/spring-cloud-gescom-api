package com.mproduits.services;

import com.mproduits.dto.ApercuPrixImportDTO;
import com.mproduits.dto.LignePrixImportDTO;
import com.mproduits.dto.PrixArticlesAdminDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.MetierException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PrixArticles;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.security.TenantContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Import de prix par lot via un fichier Excel a 2 colonnes (Reference, Prix
 * de vente net) - complement au filtre "Sans prix" de l'ecran Gestion des
 * Points de Vente, pour corriger d'un coup les centaines de produits importes
 * a 0 FCFA (voir StockRestaurationService.lireNombreOuZero pour la cause).
 * Volontairement independant du mecanisme StockImportFormat/ChampImportStock
 * (configurable, pense pour la restauration de stock) : le format ici est
 * fixe et minimal, pas besoin de plus pour 2 colonnes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrixImportService {

    private final PrixArticlesRepositories prixArticlesRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    private static final int COL_REFERENCE = 0;
    private static final int COL_PRIX = 1;

    /**
     * Genere un modele Excel pre-rempli avec tous les produits actifs de la
     * boutique (reference, libelle, prix actuel) - l'utilisateur n'a plus
     * qu'a completer/corriger la colonne Prix dans Excel et reimporter le
     * meme fichier, sans avoir a retaper les references a la main.
     */
    public byte[] genererModele(Long boutiqueId) {
        Long compagnieId = tenantContext.currentCompagnieId();
        Boutique boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnieId)
                .orElseThrow(() -> new BadRequestException("Boutique introuvable"));
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        List<PrixArticlesAdminDTO> produits = prixArticlesRepositories
                .findAllByEntrepriseProduitActifAdminDTO(entreprise, Boolean.TRUE, boutiqueId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Prix - " + boutique.getNom());

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] entetes = {"Reference", "Prix de vente net", "Libelle (informatif, ne pas modifier)"};
            for (int i = 0; i < entetes.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(entetes[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PrixArticlesAdminDTO pa : produits) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(COL_REFERENCE).setCellValue(pa.pointVente().produit().reference());
                row.createCell(COL_PRIX).setCellValue(pa.prixVenteNet() != null ? pa.prixVenteNet().doubleValue() : 0d);
                row.createCell(2).setCellValue(pa.pointVente().produit().libelle());
            }
            for (int i = 0; i < entetes.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la generation du modele d'import de prix", e);
            throw new MetierException("Erreur lors de la generation du modele Excel");
        }
    }

    @Transactional(readOnly = true)
    public ApercuPrixImportDTO previsualiser(MultipartFile fichier, Long boutiqueId) {
        return new ApercuPrixImportDTO(lireEtResoudreLignes(fichier, boutiqueId));
    }

    @Transactional
    public int appliquer(MultipartFile fichier, Long boutiqueId) {
        List<LignePrixImportDTO> lignes = lireEtResoudreLignes(fichier, boutiqueId);
        Long compagnieId = tenantContext.currentCompagnieId();
        int miseAJour = 0;
        for (LignePrixImportDTO ligne : lignes) {
            if (ligne.getErreur() != null || ligne.getPrixArticlesId() == null) {
                continue;
            }
            // Recherche par id (ligne deja resolue et scopee compagnie/boutique
            // via la carte de lireEtResoudreLignes) - une seule entite a la
            // fois, jamais une liste complete chargee en EAGER (voir la meme
            // precaution deja prise pour /prix-articles/bulk-prix).
            PrixArticles prixArticles = prixArticlesRepositories
                    .findByIdAndCompagnieId(ligne.getPrixArticlesId(), compagnieId)
                    .orElse(null);
            if (prixArticles == null) {
                continue;
            }
            prixArticles.setPrixVenteNet(ligne.getNouveauPrix());
            BigDecimal tva = prixArticles.getTva() != null ? prixArticles.getTva() : BigDecimal.ZERO;
            prixArticles.setPrixVenteTTC(ligne.getNouveauPrix().multiply(BigDecimal.ONE.add(tva.divide(BigDecimal.valueOf(100)))));
            prixArticlesRepositories.save(prixArticles);
            miseAJour++;
        }
        return miseAJour;
    }

    private List<LignePrixImportDTO> lireEtResoudreLignes(MultipartFile fichier, Long boutiqueId) {
        Long compagnieId = tenantContext.currentCompagnieId();
        boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnieId)
                .orElseThrow(() -> new BadRequestException("Boutique introuvable"));

        // Une seule requete en projection scalaire pour resoudre TOUTES les
        // references d'un coup (pas un aller-retour DB par ligne du fichier,
        // et jamais d'entite PrixArticles/PointVente/Produit chargee en EAGER
        // pour des centaines de lignes - meme precaution que
        // findAllByEntrepriseProduitActifAdminDTO partout ailleurs ce chantier).
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        Map<String, PrixArticlesAdminDTO> parReference = new HashMap<>();
        for (PrixArticlesAdminDTO pa : prixArticlesRepositories
                .findAllByEntrepriseProduitActifAdminDTO(entreprise, Boolean.TRUE, boutiqueId)) {
            parReference.put(pa.pointVente().produit().reference(), pa);
        }

        List<LignePrixImportDTO> resultat = new ArrayList<>();
        try (InputStream in = fichier.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || estLigneVide(row)) {
                    continue;
                }
                int ligneNo = r + 1;
                String reference = lireTexte(row, COL_REFERENCE);
                if (reference.isBlank()) {
                    resultat.add(new LignePrixImportDTO(ligneNo, reference, "", null, null, "Reference manquante", null));
                    continue;
                }

                PrixArticlesAdminDTO existant = parReference.get(reference);
                if (existant == null) {
                    resultat.add(new LignePrixImportDTO(ligneNo, reference, "", null, null,
                            "Reference introuvable dans cette boutique", null));
                    continue;
                }
                String libelle = existant.pointVente().produit().libelle();

                BigDecimal nouveauPrix;
                try {
                    nouveauPrix = lireNombre(row, COL_PRIX);
                } catch (NumberFormatException e) {
                    nouveauPrix = null;
                }
                if (nouveauPrix == null || nouveauPrix.compareTo(BigDecimal.ZERO) <= 0) {
                    resultat.add(new LignePrixImportDTO(ligneNo, reference, libelle, existant.prixVenteNet(), null,
                            "Prix manquant ou invalide", null));
                    continue;
                }

                resultat.add(new LignePrixImportDTO(ligneNo, reference, libelle, existant.prixVenteNet(), nouveauPrix,
                        null, existant.id()));
            }
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier d'import de prix", e);
            throw new MetierException("Fichier illisible - verifiez qu'il s'agit bien d'un fichier Excel (.xlsx)");
        }
        return resultat;
    }

    private boolean estLigneVide(Row row) {
        for (int i = 0; i <= COL_PRIX; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !lireTexte(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String lireTexte(Row row, int index) {
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
