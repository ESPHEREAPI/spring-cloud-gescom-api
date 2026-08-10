package com.mproduits.services;

import com.mproduits.dto.*;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;
import com.mproduits.ecommerce.dto.DTO.Orders_detailsDTO;
import com.mproduits.ecommerce.dto.entites.ClientOrder;
import com.mproduits.enums.StatutVente;
import com.mproduits.enums.TypePaiement;
import com.mproduits.exceptions.MetierException;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.*;
import com.mproduits.repositories.*;
import com.mproduits.security.TenantContext;
import com.mproduits.utiles.GlobalFonctions;
import com.mproduits.utiles.IdleDate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des codes-barres et des ventes - Optimisé Java 17/21
 *
 * @author USER01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BarcodeService {

    // Constants
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    // Tolerance d'arrondi pour valider qu'un paiement mixte couvre bien le
    // montant net (evite de rejeter un paiement a 1 centime pres).
    private static final BigDecimal MONTANT_TOLERANCE = new BigDecimal("0.01");
    private static final int TOP_ARTICLES_LIMIT = 10000;
    // Doit correspondre exactement au code semé par defaut cote
    // microservice-administration (UserService.seedProfilsParDefautPourCompagnie)
    // - un mismatch ici rend tout compte caissier invisible sans aucune erreur.
    private static final String PROFIL_CAISSE = "CAISSIER";

    // Dependencies (final avec @RequiredArgsConstructor)
    private final BarcodeproduitRepositories barcodeproduitRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final EntrepriseRepositories entrepriseRepositories;
    private final VenteRepositories venteRepositories;
    private final PersonneRepositories personneRepositories;
    private final ClientBonAchatRepositories clientBonAchatRepositories;
    private final LigneVenteRepositories ligneVenteRepositories;
    private final PaiementRepositories paiementRepositories;
    private final PointVenteRepositories pointVenteRepositories;
    private final ProduitRepositories produitRepositories;
    private final ClientRepositories clientRepositories;
    private final MapperDtoImpl mapper;
    private final TicketCaisseService ticketCaisseService;
    private final MoisRepositories moisRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final TenantContext tenantContext;
    private final EntrepriseService entrepriseService;
    private final BonAchatService bonAchatService;

    /**
     * Charge un produit par son code-barres
     */
    @Transactional(readOnly = true)
    public ProduitDto chargeProduitByBarcode(String codeBar) {
        var barcode = GlobalFonctions.getCodeBare(codeBar);
        var barcodeProduit = barcodeproduitRepositories
                .findByCodeBardAndCompagnieId(barcode, tenantContext.currentCompagnieId())
                .orElse(null);

        if (barcodeProduit == null || barcodeProduit.getId() == null) {
            return new ProduitDto();
        }

        var entreprise = findActiveEntreprise();
        return prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, barcodeProduit.getProduit(),barcodeProduit.getPrixArticles().getPointVente().getBoutique().getId())
                .map(this::chargeValue)
                .orElseGet(ProduitDto::new);
    }

    /**
     * Récupère les articles les plus populaires (cache 10 minutes)
     */
    @Cacheable(value = "topArticles", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<ProduitDto> getTopArticles(Long boutiqueid) {
        var limit = PageRequest.of(0, TOP_ARTICLES_LIMIT);
        var articles = prixArticlesRepositories.findTopActifWithStockFinalPositive(limit, boutiqueid);
     
   
        return articles.stream()
                .filter(pa-> pa.getEntreprise().getAnnee().getId()==anneeEnCours())
                .map(this::chargeValue)
                .toList();
    }

    /**
     * Recherche un article par référence
     */
    @Transactional(readOnly = true)
    public ProduitDto getArticlesByReference(String reference,Long boutiqueid) {
        return prixArticlesRepositories.getPrixAticlesByReferenceProduit(reference,boutiqueid)
                 .filter(pa-> pa.getEntreprise().getAnnee().getId()==anneeEnCours())
                .map(this::chargeValue)
                .orElse(null);
    }

    /**
     * Recherche un article par ID produit
     */
    @Transactional(readOnly = true)
    public ProduitDto getArticlesByProduitId(Long produitId,Long boutiqueid) {
        var entreprise = findActiveEntreprise();
        var produit = findProduitOrThrow(produitId);

        return prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, produit, boutiqueid)
                .map(this::chargeValue)
                .orElse(null);
    }

    /**
     * Valide une vente standard
     */
    @Transactional(rollbackFor = Exception.class)
    public Long valideVente(VenteDto venteDto) {
        try {
            var entreprise = findActiveEntreprise();
            // Identite verifiee par le JWT - jamais celle envoyee par le client
            // (falsifiable), qui sert de "Caissier" sur le ticket imprime.
            var vendeur = findVendeurConnecte();
            var client = findOrCreateClient(venteDto.getClient());

            var vente = creerVente(venteDto, entreprise, vendeur, client);
            var venteEnregistree = venteRepositories.save(vente);

            // Créer les lignes de vente
            venteDto.getItems().forEach(item
                    -> createLigneVente(item, venteEnregistree)
            );

            // Enregistrer le paiement
            creerPaiement(venteDto, venteEnregistree);

            log.info("Vente validée avec succès: id={}, ticket={}",
                    venteEnregistree.getId(), venteEnregistree.getNumeroTicket());

            return venteEnregistree.getId();

        } catch (Exception e) {
            log.error("Erreur lors de la validation de la vente", e);
            throw new MetierException("Échec de l'enregistrement de la vente: " + e.getMessage(), e);
        }
    }

    /**
     * Valide une vente à partir d'une commande existante
     */
    @Transactional(rollbackFor = Exception.class)
    public Long valideVente(VenteDto venteDto, long numeroCommande, Long boutiqueid) {
        try {
            var entreprise = findActiveEntreprise();
            var vendeur = findVendeurConnecte();
            var client = findOrCreateClient(venteDto.getClient());

            // Récupérer la vente en cours
            var vente = venteRepositories
                    .findByNumeroTicketAndStatutForCommande(numeroCommande, StatutVente.EN_COURS, entreprise.getAnnee().getId(), boutiqueid)
                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable pour le ticket " + numeroCommande));

            // Mettre à jour la vente
            updateVente(vente, venteDto, entreprise, vendeur, client);
            var venteEnregistree = venteRepositories.save(vente);

            // Synchroniser les lignes de vente
            var lignesReference = venteDto.getItems().stream()
                    .map(mapper::mapperCcaissedtoByLigneDTO)
                    .toList();

            var lignesSource = ligneVenteRepositories.findByVente(vente);
            synchroniserLignesVente(lignesReference, lignesSource, vente);

            // Mettre à jour le paiement
            updatePaiement(venteDto, venteEnregistree);

            log.info("Vente mise à jour avec succès: id={}, commande={}",
                    venteEnregistree.getId(), numeroCommande);

            return venteEnregistree.getId();

        } catch (Exception e) {
            log.error("Erreur lors de la validation de la commande", e);
            throw new MetierException("Échec de l'enregistrement de la vente: " + e.getMessage(), e);
        }
    }

    /**
     * Valide une vente en cours (commande e-commerce)
     */
    @Transactional(rollbackFor = Exception.class)
    public Vente valideVenteEnCours(OrdersDTO ordersDTO) {
        try {
            var entreprise = findActiveEntreprise();
            var vendeur = findPersonneByUsername(ordersDTO.getClient().getUsernane());
            var client = findOrCreateClientFromOrder(ordersDTO.getClient());

            var numeroTicket = genererNumeroTicketUnique(entreprise, vendeur.getBoutique().getId());

            var vente = creerVenteEnCours(ordersDTO, entreprise, vendeur, client, numeroTicket);
            var venteEnregistree = venteRepositories.save(vente);

            // Créer les lignes de vente et décrémenter le stock
            ordersDTO.getProducts().forEach(produit
                    -> createLigneVenteEnCours(produit, venteEnregistree)
            );

            // Enregistrer le paiement
            creerPaiementEnCours(ordersDTO, venteEnregistree);

            log.info("Commande créée avec succès: id={}, ticket={}",
                    venteEnregistree.getId(), numeroTicket);

            return venteEnregistree;

        } catch (Exception e) {
            log.error("Erreur lors de la création de la commande", e);
            throw new MetierException("Échec de l'enregistrement de la commande: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère une vente par son ID
     */
    @Transactional(readOnly = true)
    public Vente getVenteById(Long venteId) {
        return venteRepositories.findByIdAndBoutique_Compagnie_Id(venteId, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new ResourceNotFoundException("Vente non trouvée: id=" + venteId));
    }

    /**
     * Récupère les lignes de vente d'une vente
     */
    @Transactional(readOnly = true)
    public List<LigneVente> listeLigneVenteByVente(Vente vente) {
        return ligneVenteRepositories.findByVente(vente);
    }

    /**
     * Récupère le paiement d'une vente
     */
    @Transactional(readOnly = true)
    public List<Paiement> getPaiementsByVente(Vente vente) {
        var paiements = paiementRepositories.findAllByVente(vente);
        if (paiements.isEmpty()) {
            throw new EntityNotFoundException("Paiement non trouvé pour la vente: " + vente.getId());
        }
        return paiements;
    }

    /**
     * Libellé du/des mode(s) de paiement d'une vente (ex: "ESPECES" ou
     * "ESPECES + ORANGE_MONEY" pour un paiement mixte), pour affichage/impression.
     */
    public String getModePaiementLabel(Vente vente) {
        return getPaiementsByVente(vente).stream()
                .map(p -> p.getTypePaiement().name())
                .distinct()
                .collect(Collectors.joining(" + "));
    }

    /**
     * Génère un ticket de caisse en format texte
     */
    public void createTicketCaisseTXT(Vente vente) throws IOException {
        var mois = getMoisActuel();
        // Un ticket doit toujours pouvoir s'imprimer meme si, pour une raison
        // imprevue, aucune ligne de paiement n'est retrouvee - avant, ce cas
        // (rarissime mais pas impossible : delai de replication, appel
        // concurrent...) faisait echouer TOUTE la generation du ticket, avec
        // un onglet d'impression qui restait vide sans aucune explication.
        var paiements = paiementRepositories.findAllByVente(vente);
        var lignesVente = ligneVenteRepositories.findByVente(vente);

        var ticketRequest = TicketRequest.builder()
                .lignesVente(lignesVente)
                .remboursementAvecBonAchat(Boolean.FALSE)
                .typePaiement(paiements.isEmpty() ? null : paiements.get(0).getTypePaiement())
                .modePaiementLabel(paiements.isEmpty() ? "N/A" : paiements.stream()
                        .map(p -> p.getTypePaiement().name())
                        .distinct()
                        .collect(Collectors.joining(" + ")))
                .mois(mois.getMois())
                .vente(vente)
                .property(new Property())
                .build();

        ticketCaisseService.generateTicket(ticketRequest);
        log.info("Ticket généré pour la vente: {}", vente.getId());
    }

    /**
     * Récupère tous les clients
     */
    @Transactional(readOnly = true)
    public List<ClientDto> allClient() {
        return clientRepositories.findByCompagnie_Id(tenantContext.currentCompagnieId()).stream()
                .map(mapper::mapperClientByClientDto)
                .toList();
    }

    /**
     * Crée un nouveau client depuis la caisse
     */
    @Transactional
    public Client createClientCaisse(ClientDto clientDto) {
        Long compagnieId = tenantContext.currentCompagnieId();
        return clientRepositories.findByNomAndCompagnie_Id(clientDto.getNom(), compagnieId)
                .orElseGet(() -> {
                    var client = new Client();
                    client.setNom(clientDto.getNom());
                    client.setTelephone(clientDto.getTelephone());
                    client.setEmail(clientDto.getEmail());
                    client.setCompagnie(new Compagnie(compagnieId));
                    return clientRepositories.save(client);
                });
    }

    /**
     * Liste tous les caissiers actifs
     */
    @Transactional(readOnly = true)
    public List<UserDTO> listeCaissiers(Long boutiqueid) {
        // Filtre par code de profil directement (pas par une entite Profil
        // resolue au prealable) - la table profil n'a pas de colonne
        // compagnie ici, donc plusieurs compagnies ont chacune leur propre
        // ligne "CAISSIER" et profilRepositories.findByCode() plante des
        // qu'il y en a plus d'une (NonUniqueResultException).
        return personneRepositories.findActiveUserByProfilAndBoutiqueId(PROFIL_CAISSE, boutiqueid).stream()
                .map(this::createUserDTO)
                .toList();
    }

    // ==================== Méthodes privées ====================
    /**
     * Charge les valeurs d'un PrixArticles vers ProduitDto
     */
    private ProduitDto chargeValue(PrixArticles pa) {
        var pdto = mapper.mapperProduitDto(pa.getPointVente().getProduit(), pa.getPointVente().getBoutique());
        pdto.setBarcode(pa.getCodeBar());
        pdto.setPrixVenteNet(pa.getPrixVenteNet());

        var prixTTC = Optional.ofNullable(pa.getPrixVenteTTC())
                .filter(prix -> prix.compareTo(ZERO) != 0)
                .orElse(pa.getPrixVenteNet());

        pdto.setPrixVenteTTC(prixTTC);
        pdto.setStockFinal(pa.getPointVente().getStockFinalTheorie());
        pdto.setTva(pa.getTva());
        pdto.setRemise(pa.getRemise());

        log.debug("Article chargé: {} - PrixTTC: {}",
                pa.getPointVente().getProduit().getLibelle(), prixTTC);

        return pdto;
    }

    /**
     * Crée une vente standard
     */
    private Vente creerVente(VenteDto dto, Entreprise entreprise, Personne vendeur, Client client) {
        var vente = new Vente();
        vente.setDateVente(dto.getDate());
        vente.setEntreprise(entreprise);
        vente.setNumeroTicket(dto.getNumeroTicket());
        vente.setStatut(StatutVente.TERMINEE);
        vente.setTotalBrut(dto.getMontantTotal());
        vente.setTotalNet(dto.getMontantNet());
        vente.setTotalRemise(dto.getRemise());
        vente.setTotalrecu(dto.getMontantRecu());
        vente.setBoutique(boutiqueRepositories.findById(dto.getBoutiqueid()).get());
        vente.setVendeur(vendeur);
        vente.setBoutique(boutiqueRepositories.findById(dto.getBoutiqueid()).get());

        if (client != null) {
            vente.setClient(client);
        }

        return vente;
    }

    /**
     * Met à jour une vente existante
     */
    private void updateVente(Vente vente, VenteDto dto, Entreprise entreprise,
            Personne vendeur, Client client) {
        vente.setDateVente(dto.getDate());
        vente.setEntreprise(entreprise);
        vente.setNumeroTicket(dto.getNumeroTicket());
        vente.setStatut(StatutVente.TERMINEE);
        vente.setTotalBrut(dto.getMontantTotal());
        vente.setTotalNet(dto.getMontantNet());
        vente.setTotalRemise(dto.getRemise());
        vente.setTotalrecu(dto.getMontantRecu());
        vente.setVendeur(vendeur);

        if (client != null) {
            vente.setClient(client);
        }
    }

    /**
     * Crée une vente en cours (commande)
     */
    private Vente creerVenteEnCours(OrdersDTO dto, Entreprise entreprise,
            Personne vendeur, Client client, String numeroTicket) {
        var vente = new Vente();
        vente.setDateVente(dto.getDate());
        vente.setEntreprise(entreprise);
        vente.setStatut(StatutVente.EN_COURS);
        vente.setTotalBrut(dto.getTotalAmount());
        vente.setTotalNet(dto.getTotalAmount());
        vente.setNumeroTicket(numeroTicket);
        vente.setNumerocommande(Long.parseLong(numeroTicket));
        vente.setVendeur(vendeur);
        vente.setBoutique(vendeur.getBoutique());
        vente.setUserecom(dto.getClient().getUsernane());

        if (client != null) {
            vente.setClient(client);
        }

        return vente;
    }

    /**
     * Crée une ligne de vente et gère le stock
     */
    private LigneVente createLigneVente(Caissedto dto, Vente vente) {
        try {
            var produit = mapper.mapperProduit(dto.getArticle());

            var ligneVente = new LigneVente();
            ligneVente.setPrixUnitaire(dto.getPrixUnitaire());
            ligneVente.setQuantite(dto.getQuantite());
            ligneVente.setTotalLigne(dto.getMontantTotal());
            ligneVente.setVente(vente);
            ligneVente.setProduit(produit);

            var ligneSave = ligneVenteRepositories.save(ligneVente);

            // Déstockage
            destockerArticle(vente.getEntreprise(),produit,vente.getBoutique().getId(), dto.getQuantite());

            return ligneSave;

        } catch (Exception e) {
            log.error("Erreur lors de la création de la ligne de vente", e);
            throw new MetierException("Échec de la ligne de vente: " + e.getMessage(), e);
        }
    }

    /**
     * Crée une ligne de vente en cours (commande)
     */
    private LigneVente createLigneVenteEnCours(Orders_detailsDTO dto, Vente vente) {
        try {
            var produit = findProduitOrThrow(dto.getProduct().getProduit());

            var ligneVente = new LigneVente();
            ligneVente.setPrixUnitaire(dto.getPrice());
            ligneVente.setQuantite(dto.getQuantite());
            ligneVente.setTotalLigne(dto.getPrice().multiply(dto.getQuantite()));
            ligneVente.setProduit(produit);
            ligneVente.setVente(vente);

            // Déstockage avec vérification
            destockerArticleAvecVerification(vente, produit,vente.getBoutique().getId(), dto.getQuantite());

            return ligneVenteRepositories.save(ligneVente);

        } catch (Exception e) {
            log.error("Erreur lors de la création de la ligne de vente en cours", e);
            throw new MetierException("Échec de la ligne de vente: " + e.getMessage(), e);
        }
    }

    /**
     * Déstocke un article
     */
    /**
     * Décrémente le stock de manière atomique (thread-safe)
     */
    @Transactional(rollbackFor = Exception.class)
    private void destockerArticle(Entreprise entreprise, Produit produit,Long boutiqueid, BigDecimal quantite) {
        //Optional<Boutique> boutique=boutiqueRepositories.findById(boutiqueid);
        var prixArticle = prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, produit,boutiqueid)
                .orElseThrow(() -> new MetierException("Article introuvable"));

        var pointVente = prixArticle.getPointVente();

        // ✅ SOLUTION : UPDATE atomique en SQL
        int rowsUpdated = pointVenteRepositories.updateStockAtomique(
                pointVente.getId(),
                quantite
        );

        // ❌ Si aucune ligne mise à jour = stock insuffisant
        if (rowsUpdated == 0) {
            // Relire le stock actuel pour le message d'erreur
            var pvActuel = pointVenteRepositories.findById(pointVente.getId())
                    .orElseThrow(() -> new MetierException("Point de vente introuvable"));

            throw new MetierException(
                    String.format("Stock insuffisant pour %s: disponible=%.2f, demandé=%.2f",
                            produit.getLibelle(),
                            pvActuel.getStockFinalTheorie(),
                            quantite));
        }

        log.debug("✅ Stock décrémenté: produit={}, quantité={}", produit.getCode(), quantite);
    }

    /**
     * Déstocke avec vérification et rollback si stock insuffisant
     */
    private void destockerArticleAvecVerification(Vente vente, Produit produit,Long boutiqueid, BigDecimal quantite) {
        var prixArticle = prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(vente.getEntreprise(), produit,boutiqueid)
                .orElseThrow(() -> new MetierException("Article introuvable"));

        var pointVente = prixArticle.getPointVente();
        var stockActuel = pointVente.getStockFinalTheorie();

        if (stockActuel.compareTo(quantite) < 0) {
            log.error("Stock insuffisant: produit={}, stock={}, demandé={}",
                    produit.getLibelle(), stockActuel, quantite);
            venteRepositories.delete(vente);
            throw new MetierException("Stock insuffisant pour " + produit.getLibelle());
        }

        var nouvelleSortie = pointVente.getSortiProduit().add(quantite);
        var nouveauStock = stockActuel.subtract(quantite);

        pointVente.setSortiProduit(nouvelleSortie);
        pointVente.setStockFinalTheorie(nouveauStock);
        pointVenteRepositories.save(pointVente);
    }

    /**
     * Crée ou récupère un client
     */
    private Client findOrCreateClient(ClientDto dto) {
        if (dto == null || dto.getNom() == null || dto.getNom().isBlank()) {
            return null;
        }

        Long compagnieId = tenantContext.currentCompagnieId();
        return clientRepositories.findByNomAndCompagnie_Id(dto.getNom(), compagnieId)
                .orElseGet(() -> {
                    var client = new Client();
                    client.setEmail(dto.getEmail());
                    client.setNom(dto.getNom());
                    client.setTelephone(dto.getTelephone());
                    client.setFidelite(true);
                    client.setCompagnie(new Compagnie(compagnieId));
                    return clientRepositories.save(client);
                });
    }

    /**
     * Crée ou récupère un client depuis une commande
     */
    private Client findOrCreateClientFromOrder(ClientOrder dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            return null;
        }

        Long compagnieId = tenantContext.currentCompagnieId();
        return clientRepositories.findByNomAndCompagnie_Id(dto.getName(), compagnieId)
                .orElseGet(() -> {
                    var client = new Client();
                    client.setEmail(dto.getEmail());
                    client.setNom(dto.getName());
                    client.setTelephone(dto.getPhoneNumber());
                    client.setFidelite(true);
                    client.setCompagnie(new Compagnie(compagnieId));
                    return clientRepositories.save(client);
                });
    }

    /**
     * Crée le(s) paiement(s) d'une nouvelle vente (paiement mixte possible :
     * plusieurs lignes especes/mobile money/bon d'achat).
     */
    private void creerPaiement(VenteDto dto, Vente vente) {
        enregistrerLignesPaiement(dto, vente);
    }

    /**
     * Remplace le(s) paiement(s) d'une vente existante par les lignes fournies.
     */
    private void updatePaiement(VenteDto dto, Vente vente) {
        paiementRepositories.deleteAllByVente(vente);
        enregistrerLignesPaiement(dto, vente);
    }

    /**
     * Enregistre une ou plusieurs lignes de paiement pour une vente, en
     * validant que leur somme couvre le montant net a payer et en
     * consommant le solde des bons d'achat utilises comme moyen de paiement.
     * Compatibilite ascendante : si dto.getPaiements() est vide, on retombe
     * sur l'unique couple typePaiement/montantNet (ancien format).
     */
    private void enregistrerLignesPaiement(VenteDto dto, Vente vente) {
        List<PaiementDto> lignes = dto.getPaiements();
        if (lignes == null || lignes.isEmpty()) {
            var ligneUnique = new PaiementDto();
            ligneUnique.setTypePaiement(dto.getTypePaiement());
            ligneUnique.setMontant(dto.getMontantNet());
            ligneUnique.setReference(dto.getNumeroTicket());
            lignes = List.of(ligneUnique);
        }

        var montantDu = dto.getMontantNet() != null ? dto.getMontantNet() : ZERO;
        var totalPaye = lignes.stream()
                .map(PaiementDto::getMontant)
                .filter(Objects::nonNull)
                .reduce(ZERO, BigDecimal::add);

        if (totalPaye.compareTo(montantDu.subtract(MONTANT_TOLERANCE)) < 0) {
            throw new MetierException("Le total des paiements (" + totalPaye
                    + ") est inférieur au montant net à payer (" + montantDu + ")");
        }

        for (PaiementDto ligne : lignes) {
            if (ligne.getTypePaiement() == null) {
                throw new MetierException("Le type de paiement est requis pour chaque ligne");
            }
            var typePaiement = TypePaiement.valueOf(ligne.getTypePaiement());
            var montant = ligne.getMontant() != null ? ligne.getMontant() : ZERO;

            if (typePaiement == TypePaiement.BON_ACHAT) {
                if (ligne.getReference() == null || ligne.getReference().isBlank()) {
                    throw new MetierException("Le code du bon d'achat est requis pour un paiement par bon d'achat");
                }
                bonAchatService.consommer(ligne.getReference(), montant);
            }

            var paiement = new Paiement();
            paiement.setDatePaiement(new Date());
            paiement.setMontant(montant);
            paiement.setTypePaiement(typePaiement);
            paiement.setReference(ligne.getReference() != null ? ligne.getReference() : dto.getNumeroTicket());
            paiement.setVente(vente);
            paiementRepositories.save(paiement);
        }
    }

    /**
     * Crée un paiement pour une commande
     */
    private void creerPaiementEnCours(OrdersDTO dto, Vente vente) {
        var paiement = new Paiement();
        paiement.setDatePaiement(new Date());
        paiement.setMontant(dto.getTotalAmount());
        paiement.setTypePaiement(TypePaiement.ESPECES);
        paiement.setReference(vente.getNumeroTicket());
        paiement.setVente(vente);
        paiementRepositories.save(paiement);
    }

    /**
     * Synchronise les lignes de vente (suppression et recréation)
     */
    private void synchroniserLignesVente(List<LigneVente> lignesReference,
            List<LigneVente> lignesSource,
            Vente vente) {
        // Supprimer les anciennes lignes
        lignesSource.forEach(ligneVenteRepositories::delete);
        lignesSource.clear();

        // Créer les nouvelles lignes
        lignesReference.forEach(ligneRef -> {
            var nouvelleLigne = new LigneVente();
            nouvelleLigne.setQuantite(ligneRef.getQuantite());
            nouvelleLigne.setPrixUnitaire(ligneRef.getPrixUnitaire());
            nouvelleLigne.setRemise(ligneRef.getRemise());
            nouvelleLigne.setTotalLigne(ligneRef.getTotalLigne());
            nouvelleLigne.setProduit(ligneRef.getProduit());
            nouvelleLigne.setVente(vente);

            var ligneSaved = ligneVenteRepositories.save(nouvelleLigne);
            lignesSource.add(ligneSaved);
        });

        log.debug("Lignes de vente synchronisées: {} lignes", lignesSource.size());
    }

    /**
     * Génère un numéro de ticket unique
     */
    private String genererNumeroTicketUnique(Entreprise entreprise, Long boutiqueid) {
        var numero = genererNumeroCommande(entreprise.getAnnee().getId());

        while (venteRepositories.findByEntrepriseAndNumeroTicketAndBoutiqueId(entreprise, numero, boutiqueid).isPresent()) {
            numero = genererNumeroCommande(entreprise.getAnnee().getId());
        }

        return numero;
    }

    /**
     * Génère un numéro de commande
     */
    private String genererNumeroCommande(int annee) {
        long nombre = venteRepositories.count() + 1;
        return String.format("%d%d", annee, nombre);
    }

    /**
     * Récupère le mois actuel
     */
    private Mois getMoisActuel() {
        var maintenant = new Date();
        int annee = IdleDate.getYear(maintenant);
        int numero = IdleDate.getMonth(maintenant);

        return moisRepositories.findOneByAnneeAndNumero(annee, numero)
                .orElseThrow(() -> new MetierException("MOIS N EXISTE PAS "));
    }

    /**
     * Crée un UserDTO depuis une Personne
     */
    private UserDTO createUserDTO(Personne personne) {
        var userDTO = new UserDTO();
        userDTO.setUserName(personne.getUserName());
        userDTO.setFirstName(personne.getNom());
        userDTO.setLastname(personne.getPrenom());
        return userDTO;
    }

    /**
     * Trouve l'entreprise active
     */
    private Entreprise findActiveEntreprise() {
        return Optional.ofNullable(entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId()))
                .orElseThrow(() -> new MetierException("Aucune entreprise active"));
    }

    /**
     * Trouve un produit ou lève une exception
     */
    private Produit findProduitOrThrow(Long produitId) {
        return produitRepositories.findByIdAndCompagnie_Id(produitId, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new MetierException("Produit non trouvé: id=" + produitId));
    }

    /**
     * Trouve une personne par username - scope par boutique/compagnie car ce
     * username peut venir du client (flux e-commerce, voir plus bas) et doit
     * donc rester verifie avant d'etre utilise.
     */
    private Personne findPersonneByUsername(String username) {
        return personneRepositories.findByUserNameAndBoutique_Compagnie_Id(username, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
    }

    /**
     * Vendeur = utilisateur actuellement authentifie (identite deja verifiee
     * par le JWT, jamais un nom fourni par le client) - recherche non scopee
     * par boutique car ca casserait la vente pour un compte admin/gerant sans
     * boutique fixe assignee ; sans risque puisque le username vient de
     * TenantContext, pas du corps de la requete.
     */
    private Personne findVendeurConnecte() {
        String username = tenantContext.currentUsername();
        return personneRepositories.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
    }

    /**
     * Valide qu'un stock est suffisant
     */
    private void validateStockSuffisant(BigDecimal stockActuel, BigDecimal quantite, String libelle) {
        if (stockActuel.compareTo(quantite) < 0) {
            throw new MetierException(
                    String.format("Stock insuffisant pour %s: disponible=%.2f, demandé=%.2f",
                            libelle, stockActuel, quantite));
        }
    }
    private int  anneeEnCours(){
        try {
             Entreprise e=entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
             return e.getAnnee().getId();
        } catch (Exception e) {
        }
          
      return 0;
    }
}
