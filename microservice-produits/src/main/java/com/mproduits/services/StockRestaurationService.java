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
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.HistoriqueRestaurationStockRepository;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.PrixHistoriqueRepository;
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
 * previsualiserImport ne modifie rien ; appliquerImport refuse de commencer
 * si la moindre ligne est en erreur (previsualiserImport doit etre propre au
 * prealable), et trace chaque ligne dans HistoriqueRestaurationStock +
 * StockMovement, meme discipline que InventairesService pour les corrections
 * manuelles. Chaque ligne est ensuite appliquee independamment (voir
 * RestaurationLigneService) - un fichier reel de plusieurs centaines de
 * lignes n'est PLUS tout-ou-rien au niveau de l'application (seule la
 * validation prealable l'est) ; ResultatApplicationStock rapporte les
 * eventuelles lignes en echec malgre une previsualisation propre (rare, ex.
 * modification concurrente).
 *
 * Cas particulier : une reference absente du catalogue n'est PAS une erreur
 * bloquante (usage vise : initialiser une boutique de A a Z) - le produit,
 * son point de vente, son prix de vente (PrixArticles) et, si fourni, son
 * prix d'achat (PrixAchat) sont crees a l'application. Un Magasin "point de
 * vente" par defaut est cree pour la boutique s'il n'en existe encore aucun
 * (voir RestaurationLigneService.resolveOuCreerMagasinPointDeVente).
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

    private final BarcodeproduitRepositories barcodeproduitRepositories;
    private final StockImportFormatRepositories stockImportFormatRepositories;
    private final ProduitRepositories produitRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final PointVenteRepositories pointVenteRepositories;
    private final StockMovementRepository stockMovementRepository;
    private final HistoriqueRestaurationStockRepository historiqueRestaurationStockRepository;
    private final CategorieRepositories categorieRepositories;
    private final MagasinRepositories magasinRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final PrixHistoriqueRepository prixHistoriqueRepository;
    private final PrixAchatRepositories prixAchatRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;
    private final RestaurationLigneService restaurationLigneService;

    // produit == null et nouveauProduit == true : le produit sera cree par
    // appliquerImport (voir creerProduitEtStockInitial), pas seulement mis a
    // jour - produitLibelle/categorieLibelle/prixVente/prixAchat portent alors
    // les valeurs du fichier necessaires a cette creation.
    //
    // nouveauPointVente == true : aucun PointVente actif n'existe pour ce
    // produit DANS CETTE BOUTIQUE (produit deja connu du catalogue mais
    // jamais stocke ici - cas typique : catalogue partage, initialisation
    // d'une DEUXIEME boutique avec des references qui existent deja pour une
    // autre). Toujours vrai quand nouveauProduit l'est aussi. appliquerImport
    // cree alors un PointVente + PrixArticles (meme logique que pour un
    // produit tout neuf), sans recreer le Produit lui-meme.
    //
    // Package-private (pas private) : RestaurationLigneService (meme
    // package) en a besoin pour appliquer chaque ligne dans sa propre
    // transaction - voir appliquerImport.
    record LigneResolue(int ligneNo, String reference, String boutiqueNom, Produit produit,
            Boutique boutique, BigDecimal ancienneQuantite, BigDecimal nouvelleQuantite, String erreur,
            boolean nouveauProduit, boolean nouveauPointVente, String produitLibelle, String categorieLibelle,
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
                        l.ancienneQuantite(), l.nouvelleQuantite(), l.erreur(), l.nouveauProduit(), l.nouveauPointVente()))
                .toList();
        return new ApercuImportStockDTO(dto);
    }

    public record ResultatApplicationStock(String batchId, int lignesAppliquees, int lignesEnErreur,
            List<String> referencesEnErreur) {
    }

    /**
     * Delibermement PAS @Transactional : chaque ligne est appliquee dans sa
     * propre transaction via RestaurationLigneService.appliquerLigne
     * (REQUIRES_NEW) - voir cette classe pour le pourquoi (Hibernate
     * "Found shared references to a collection" sur les gros fichiers reels
     * des que trop de Produit sont geres dans une seule session). Englober
     * la boucle dans une transaction ici annulerait cet isolement (meme
     * piege que reinitialiserBoutique, voir son historique de commits).
     */
    public ResultatApplicationStock appliquerImport(MultipartFile fichier, Long boutiqueIdPreselectionnee, ModeRestauration mode, String username) {
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

        int reussies = 0;
        List<String> referencesEnErreur = new ArrayList<>();
        for (LigneResolue ligne : lignes) {
            try {
                restaurationLigneService.appliquerLigne(ligne, batchId, mode, username, maintenant, maintenantLdt, entrepriseActive);
                reussies++;
            } catch (Exception e) {
                log.error("Restauration de stock : echec ligne {} (reference {}) lot {}",
                        ligne.ligneNo(), ligne.reference(), batchId, e);
                referencesEnErreur.add(ligne.reference());
            }
        }

        log.info("Restauration de stock appliquee : lot={}, {}/{} lignes reussies, mode={}, utilisateur={}",
                batchId, reussies, lignes.size(), mode, username);
        return new ResultatApplicationStock(batchId, reussies, referencesEnErreur.size(), referencesEnErreur);
    }

    /**
     * Remet une boutique a zero pour permettre de reprendre une
     * initialisation de stock depuis un etat propre pendant les tests :
     * supprime tout le stock (PointVente + PrixArticles + StockMovement)
     * et l'historique de restauration de cette boutique, puis supprime
     * aussi chaque Produit qui n'a plus AUCUN point de vente dans une
     * AUTRE boutique (un produit partage avec une autre boutique est
     * uniquement deleste de son stock ici, jamais supprime du catalogue).
     * Ordre de suppression contraint par les FK (historique/mouvements/prix
     * avant PointVente, PrixAchat avant Produit).
     */
    /**
     * Ne fait QUE la partie stock (atomique, tout ou rien) et renvoie les ids
     * Produit candidats a la suppression. La suppression des produits
     * eux-memes est volontairement laissee au caller (voir
     * StockRestaurationController) : elle doit passer par
     * ProduitCleanupService.essayerSupprimerProduitOrphelin SANS transaction
     * ambiante, sinon l'echec (FK) d'un seul produit marque CETTE transaction
     * "rollback-only" cote Spring et annule tout le travail ci-dessous, meme
     * si l'appelant capture l'exception (UnexpectedRollbackException :
     * "Transaction silently rolled back because it has been marked as
     * rollback-only") - Propagation.REQUIRES_NEW seul ne suffit pas tant que
     * l'appel reste imbrique dans une methode @Transactional englobante.
     */
    @Transactional
    public java.util.Map<String, Object> viderStockBoutique(Long boutiqueId) {
        Long compagnieId = tenantContext.currentCompagnieId();
        Boutique boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnieId)
                .orElseThrow(() -> new BadRequestException("Boutique introuvable"));

        // Uniquement des projections/suppressions en masse ci-dessous, jamais
        // de findById/findByBoutique qui chargerait des Produit en entite -
        // avec des references dupliquees en base (voir resolveProduitTolerantDoublons),
        // charger deux Produit qui partagent la meme reference dans la meme
        // session, meme juste pour les supprimer, replante avec "Found shared
        // references to a collection" (meme cause que la restauration).
        List<Long> produitIds = pointVenteRepositories.findDistinctProduitIdsByBoutique(boutique);

        int historiqueSupprime = historiqueRestaurationStockRepository.deleteByBoutiqueBulk(boutique);
        int mouvementsSupprimes = stockMovementRepository.deleteByPointVenteBoutiqueBulk(boutique);
        // Doit precéder la suppression des PrixArticles (FK barcodeproduit.prixarticlesid).
        int barcodesSupprimes = barcodeproduitRepositories.deleteByPointVenteBoutiqueBulk(boutique);
        int prixArticlesSupprimes = prixArticlesRepositories.deleteByPointVenteBoutiqueBulk(boutique);
        // Doit precéder la suppression des PointVente (FK prixhistorique.point_vente_id, NOT NULL).
        prixHistoriqueRepository.deleteByPointVenteBoutiqueBulk(boutique);
        int pointVentesSupprimes = pointVenteRepositories.deleteByBoutiqueBulk(boutique);

        log.info("Boutique '{}' (id={}) : stock vide - {} PointVente, {} PrixArticles, {} Barcodeproduit, "
                        + "{} StockMovement, {} HistoriqueRestaurationStock supprimes, {} produit(s) candidat(s) au nettoyage",
                boutique.getNom(), boutiqueId, pointVentesSupprimes, prixArticlesSupprimes, barcodesSupprimes,
                mouvementsSupprimes, historiqueSupprime, produitIds.size());

        return java.util.Map.of(
                "produitIdsCandidats", produitIds,
                "pointVentesSupprimes", pointVentesSupprimes,
                "prixArticlesSupprimes", prixArticlesSupprimes,
                "barcodesSupprimes", barcodesSupprimes,
                "mouvementsSupprimes", mouvementsSupprimes,
                "historiqueSupprime", historiqueSupprime);
    }

    /**
     * findByReferenceAndCompagnie_Id plante (IncorrectResultSizeDataAccessException)
     * si un import anterieur a deja cree un doublon pour cette reference -
     * prend le premier plutot que de faire planter toute la restauration ;
     * le doublon en base reste une donnee a nettoyer separement, mais ne
     * doit plus bloquer l'ecran. Charge UNIQUEMENT ce premier resultat
     * (LIMIT 1, findFirstBy...OrderByIdAsc) plutot que tous les doublons :
     * les charger tous dans le contexte de persistance (ancien
     * findAllByReferenceAndCompagnie_Id) faisait planter Hibernate au flush
     * avec "Found shared references to a collection" des qu'un doublon
     * jamais modifie restait attache a la session.
     */
    private Produit resolveProduitTolerantDoublons(String reference, Long compagnieId) {
        long nbTrouves = produitRepositories.countByReferenceAndCompagnie_Id(reference, compagnieId);
        if (nbTrouves > 1) {
            log.warn("Reference '{}' en double en base pour la compagnie {} ({} produits) - le premier est utilise",
                    reference, compagnieId, nbTrouves);
        }
        return produitRepositories.findFirstByReferenceAndCompagnie_IdOrderByIdAsc(reference, compagnieId)
                .orElse(null);
    }

    /**
     * Cree le produit manquant a partir des colonnes informatives du fichier
     * (Produit/Categorie servent ici, pas seulement d'affichage). Reference
     * et compagnie sont les seuls champs garantis ; Produit (libelle) retombe
     * sur la reference si absente du format ou vide.
     */
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
                            null, lb.erreur(), false, false, null, null, null, null));
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
                            "Boutique introuvable : " + boutiqueNom, nouveauProduit, nouveauProduit, premiere.produitLibelle(),
                            premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
                    continue;
                }

                if (nouveauProduit) {
                    resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, null, boutique,
                            BigDecimal.ZERO, quantiteFichier, null, true, true, premiere.produitLibelle(),
                            premiere.categorieLibelle(), premiere.prixVente(), premiere.prixAchat()));
                    continue;
                }

                // Produit deja au catalogue mais jamais stocke dans CETTE
                // boutique (catalogue partage entre boutiques, initialisation
                // d'une deuxieme boutique...) : pas une erreur bloquante non
                // plus - appliquerImport cree un nouveau PointVente (comme
                // pour un produit tout neuf), sans recreer le Produit.
                BigDecimal ancienneQuantite = pointVenteRepositories
                        .findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique, entreprise)
                        .map(PointVente::getStockFinalTheorie)
                        .orElse(null);
                boolean nouveauPointVente = ancienneQuantite == null;
                if (nouveauPointVente) {
                    ancienneQuantite = BigDecimal.ZERO;
                }

                BigDecimal nouvelleQuantite = !nouveauPointVente && mode == ModeRestauration.AJOUT
                        ? ancienneQuantite.add(quantiteFichier)
                        : quantiteFichier;

                resultat.add(new LigneResolue(premiere.ligneNo(), reference, boutiqueNom, produit, boutique,
                        ancienneQuantite, nouvelleQuantite, null, false, nouveauPointVente, premiere.produitLibelle(),
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
