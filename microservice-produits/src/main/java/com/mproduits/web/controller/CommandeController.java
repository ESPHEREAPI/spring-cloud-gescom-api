/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.CommandeRequest;
import com.mproduits.dto.CommandeResponseDto;
import com.mproduits.dto.DepoteDto;
import com.mproduits.dto.ProduitDto;
import com.mproduits.dto.StockDepotAndBoutique;
import com.mproduits.exceptions.Success;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Annee;
import com.mproduits.model.Barcodeproduit;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.Commande;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Fournisseur;
import com.mproduits.model.Magasin;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.MagasinFournisseurPK;
import com.mproduits.model.Mois;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.repositories.SpecifiqueRepositories;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.model.Taxeproduit;
import com.mproduits.model.Ville;
import com.mproduits.repositories.AnneeRepository;
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.FournisseurRepositories;
import com.mproduits.repositories.MagasinFournisseurRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.MagasinRepository;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.SpecificationarticlesRepositories;
import com.mproduits.repositories.TaxeproduitRepositories;
import com.mproduits.repositories.VerouillaRepositories;
import com.mproduits.repositories.VilleRepositories;
import com.mproduits.services.IParamModule;
import com.mproduits.services.IProduits;
import com.mproduits.services.IServiceCommande;
import com.mproduits.utiles.GlobalFonctions;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/microservice-produits")
@RequiredArgsConstructor
@Slf4j
@Data
public class CommandeController implements Serializable {

    private final IProduits produitService;
    private final IServiceCommande commandeService;
    private final MoisRepositories moisService;
    private final TaxeproduitRepositories taxeProduitService;
    private final SpecifiqueRepositories specifiqueService;
    private final SpecificationarticlesRepositories specificationArticlesService;
    private final PrixArticlesRepositories prixArticlesService;
    private final MagasinFournisseurRepositories depotFournisseurService;
    private final VerouillaRepositories verouillageService;
    private final PointVenteRepositories pointVenteService;
    private final BarcodeproduitRepositories barcodeProduitService;
    private final MagasinRepositories depotService;
    private final MagasinFournisseurRepositories magasinFournisseurRepositories;
    private final FournisseurRepositories fournisseurRepositories;
    private final IParamModule paramModuleService;
    private final AnneeRepository anneerepository;
    private final EntrepriseRepositories entrepriseRepositories;
    private final VilleRepositories villeRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final AnneeRepository anneeRepository;
    private final MagasinRepositories magasinRepository;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
    private final TenantContext tenantContext;
    private final com.mproduits.services.EntrepriseService entrepriseService;
    private final com.mproduits.repositories.CommandeRepositories commandeRepositories;
    @Autowired
    private MapperDtoImpl mapperDto;

    @GetMapping("/produits-pageable")
    public ResponseEntity<Page<ProduitDto>> getAllProduits(Pageable pageable, HttpSession session) {
        Entreprise entreprise = getEntrepriseFromSession(session);
        Page<ProduitDto> produits = produitService.getAllProduits(entreprise, pageable);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/produits/search")
    public ResponseEntity<List<ProduitDto>> searchProduits(
            @RequestParam String query,
            HttpSession session) {

        Entreprise entreprise = getEntrepriseFromSession(session);
        List<ProduitDto> produits = produitService.searchByLibelleOrReference(query, entreprise);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/produits/{id}/stocks")
    public ResponseEntity<Map<String, Object>> getStockProduit(
            @PathVariable Long id,
            @RequestParam Long depotId,
            @RequestParam(required = false) Long boutiqueId,
            HttpSession session) {

        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueId);
        Entreprise entreprise = getEntrepriseFromSession(session);

        try {
            BigDecimal stock = calculateStock(id, depotId, boutiqueId, entreprise);

            return ResponseEntity.ok(Map.of(
                    "produitId", id,
                    "stock", stock,
                    "depotId", depotId,
                    "boutiqueId", boutiqueId != null ? boutiqueId : ""
            ));
        } catch (Exception e) {
            log.error("Erreur lors du calcul du stock pour le produit {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du calcul du stock"));
        }
    }

    @GetMapping("/produits/{produitId}/prix")
    public ResponseEntity<Map<String, BigDecimal>> getPrixProduit(
            @PathVariable Long produitId
        ) {

        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        try {
            Produit produit = produitService.findById(produitId);
          //  Optional<Magasin> depot = depotService.findById(depotId);

            BigDecimal prixAchat = commandeService.getDernierPrixAchat(produit);
            BigDecimal prixVente = commandeService.getDernierPrixVente(produit);

            return ResponseEntity.ok(Map.of(
                    "prixAchat", prixAchat,
                    "prixVente", prixVente
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des prix pour le produit {}: {}", produitId, e.getMessage());
            return (ResponseEntity<Map<String, BigDecimal>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/commandes")
    public ResponseEntity<List<Commande>> getCommandes() {
        return ResponseEntity.ok(commandeRepositories.findByCompagnieId(tenantContext.currentCompagnieId()));
    }

    @PostMapping("/approvisionner")
    public ResponseEntity<CommandeResponseDto> approvisionnerProduit(
            @Validated @RequestBody CommandeRequest request) {

        Entreprise entreprise = commandeService.getEntrepriseActif(Boolean.TRUE);
        Mois mois = commandeService.getMoisFromSession();

        try {
            // Validation des données
            if (request.getQuantite() == null || request.getQuantite().equals(BigInteger.ZERO)) {
                return ResponseEntity.badRequest()
                        .body(CommandeResponseDto.error("La quantité doit être supérieure à zéro"));
            }

            if (request.getPrixAchat() == null || request.getPrixAchat().equals(BigDecimal.ZERO)) {
                return ResponseEntity.badRequest()
                        .body(CommandeResponseDto.error("Le prix d'achat est obligatoire"));
            }

            if (request.getPrixVente() == null || request.getPrixVente().equals(BigDecimal.ZERO)) {
                return ResponseEntity.badRequest()
                        .body(CommandeResponseDto.error("Le prix de vente est obligatoire"));
            }

            // Création de la commande
            Commande commande = createCommandeFromRequest(request, entreprise, mois);

            // Traitement de l'approvisionnement
            Commande commandeSave = commandeService.approvisionner(commande, mois, entreprise);
            if (commande.getMagasinid().getBoutiqueId() != null && commande.getMagasinid().getBoutiqueId() != null) {
                handleBoutiqueApprovisionnement(commandeSave, request, entreprise, mois);
            }

            return ResponseEntity.ok(CommandeResponseDto.success(Success.OPERATION_SUCCESS.toString(), commande));

        } catch (Exception e) {
            log.error("Erreur lors de l'approvisionnement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommandeResponseDto.error("Erreur lors de l'approvisionnement: " + e.getMessage()));
        }
    }

    @GetMapping("/destinations")
    public ResponseEntity<List<MagasinFournisseur>> getDestinations(
            @RequestParam(defaultValue = "false") boolean destinationDepot,
            HttpSession session) {

        List<MagasinFournisseur> destinations;
        Long compagnieId = tenantContext.currentCompagnieId();

        if (destinationDepot) {
            destinations = (List<MagasinFournisseur>) depotFournisseurService.findDepotByFournisseurInAnneeAndCompagnieId(compagnieId);
        } else {
            destinations = (List<MagasinFournisseur>) depotFournisseurService.findDepotByFournisseurHaveBoutiqueAndCompagnieId(compagnieId);
        }

        return ResponseEntity.ok(destinations);
    }

    @PostMapping("/generate-numero")
    public ResponseEntity<String> generateNumeroCommande(HttpSession session) {
        try {
            String numero = generateCodeCommande();
            return ResponseEntity.ok(numero);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du numéro de commande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génér-ation du numéro");
        }
    }

    @GetMapping("/produits/reference/{reference}")
    public ResponseEntity<ProduitDto> produitsByReference(@PathVariable String reference) {

        ProduitDto pdto = produitService.findByReference(reference);
        return ResponseEntity.ok(pdto);
    }
   @GetMapping("/produits/id/{produitid}/{boutiqueid}")
    public ResponseEntity<ProduitDto> produitsByReference(@PathVariable Long produitid,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        Produit pdto = produitService.findById(produitid);
        Optional<Boutique> boutique=boutiqueRepositories.findById(boutiqueid);
        return ResponseEntity.ok(mapperDto.mapperProduitDto(pdto,boutique.isPresent() ? boutique.get(): new Boutique()));
    }

    @GetMapping("/produits/reference/{barcode}/{boutiqueid}")
    public ResponseEntity<ProduitDto> produitsBycodeBar(@PathVariable String barcode,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        Barcodeproduit barcodeProduit = barcodeProduitService
                .findByCodeBardAndCompagnieId(barcode, tenantContext.currentCompagnieId())
                .orElse(null);
 Optional<Boutique> boutique=boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        return ResponseEntity.ok(barcodeProduit != null ? mapperDto.mapperProduitDto(barcodeProduit.getProduit(),boutique.isPresent() ? boutique.get(): new Boutique()) : new ProduitDto());
    }

    @GetMapping("/taxes")
    public ResponseEntity<List<Taxeproduit>> getTaxesProduit(HttpSession session) {
        Entreprise entreprise = getEntrepriseFromSession(session);
        Mois mois = getMoisFromSession(session);

        List<Taxeproduit> taxes = (List<Taxeproduit>) taxeProduitService.findByEntrepriseAndMois(entreprise, mois);
        return ResponseEntity.ok(taxes);
    }

    @GetMapping("/depots/{boutiqueid}")
    public ResponseEntity<List<DepoteDto>> allDepots(@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<DepoteDto> depots = new ArrayList<>();
        try {
            depots = magasinFournisseurRepositories.findAllByCompagnieId(tenantContext.currentCompagnieId()).stream()
                    .filter(dp -> dp.getMagasin().getBoutiqueId()!=null && dp.getMagasin().getBoutiqueId().getId().equals(boutiqueid)==Boolean.TRUE)
                    .map(d -> mapperDto.mapperDepot(d))
                    .collect(Collectors.toList());
            // Authentification de l'utilisateur ici
            // ...

        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

        return ResponseEntity.ok(depots);
    }
      @GetMapping("/depots-stockage")
    public ResponseEntity<List<DepoteDto>> allDepotsStockage() {
        List<DepoteDto> depots = new ArrayList<>();
        try {
            depots = magasinFournisseurRepositories.findAllByCompagnieId(tenantContext.currentCompagnieId()).stream()
                    .filter(depot -> depot.getMagasin().getBoutiqueId()==null)
                    .map(d -> mapperDto.mapperDepot(d))
                    .collect(Collectors.toList());
            // Authentification de l'utilisateur ici
            // ...

        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

        return ResponseEntity.ok(depots);
    }
    
    
     @GetMapping("/depots-boutique")
    public ResponseEntity<List<DepoteDto>> allDepotsHaveBoutique() {
        List<DepoteDto> depots = new ArrayList<>();
        try {
            depots = depotService.findByBoutiqueIsNotNullAndCompagnie_Id(tenantContext.currentCompagnieId()).stream()
                    .map(d -> mapperDto.mapperDepot(d))
                    .collect(Collectors.toList());
            // Authentification de l'utilisateur ici
            // ...

        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

        return ResponseEntity.ok(depots);
    }
    
     @GetMapping("/boutique")
    public ResponseEntity<List<Boutique>> allBoutique() {
        Long compagnieId = tenantContext.currentCompagnieId();
        List<Boutique> boutiques = compagnieId != null
                ? boutiqueRepositories.findByCompagnie_Id(compagnieId)
                : new ArrayList<>();
        return ResponseEntity.ok(boutiques);
    }

    @GetMapping("/all-annee")
    public ResponseEntity<List<Annee>> allAnnee() {

        List<Annee> allannees = anneeRepository.findAll();
        return ResponseEntity.ok(allannees);
    }

    
    @GetMapping("/produits")
    public ResponseEntity<List<ProduitDto>> produits() {

        List<ProduitDto> allProduits = produitService.getAllProduit();
        return ResponseEntity.ok(allProduits);
    }
    
    @GetMapping("/produits/have-stock/{anneeid}/{boutiqueid}")
    public ResponseEntity<List<ProduitDto>> produitsHaveStock(@PathVariable int anneeid,@PathVariable int boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante((long) boutiqueid);

        List<ProduitDto> allProduits = produitService.getAllProduitHaveStock(boutiqueid, anneeid);
        return ResponseEntity.ok(allProduits);
    }

//    @GetMapping("/prix-articles")
//    public ResponseEntity<Page<PrixArticles>> getPrixArticles(
//            Pageable pageable,
//            HttpSession session) {
//
//        Entreprise entreprise = getEntrepriseFromSession(session);
//        // Page<PrixArticles> prixArticles = prixArticlesService.getAllByEntreprise(entreprise, pageable);
//        Page<PrixArticles> prixArticles = (Page<PrixArticles>) prixArticlesService.findActifByEntrepriseWithStock(entreprise);
//
//        return ResponseEntity.ok(prixArticles);
//    }
    @GetMapping("/produits/{produitId}/stock")
    public BigDecimal getStockProduit(@PathVariable long produitId, HttpSession session) {
        Produit produit = produitService.findById(produitId);
        Entreprise e = (Entreprise) session.getAttribute(GlobalFonctions.ENTREPRISE_ACTIF);
        Optional<PointVente> pv = pointVenteService.findLastByProduitAndEntreprise(produit, e);
        return pv.isPresent() ? pv.get().getStockFinalTheorie() : BigDecimal.ZERO;

    }

    @GetMapping("/produits/barcode/{barcode}/{boutiqueid}")
    public ResponseEntity<ProduitDto> getProduitBarCode(@PathVariable String barcode,@PathVariable Long boutiqueid, HttpSession session) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        System.out.println("code bar " + barcode);

        Barcodeproduit bar = null;
        try {
            bar = barcodeProduitService
                    .findByCodeBardAndCompagnieId(barcode, tenantContext.currentCompagnieId())
                    .orElse(null);
            //log.debug("✅ Produit trouvé : {}", bar.toString());

            Produit produit = bar == null ? new Produit() : bar.getPrixArticles().getPointVente().getProduit();
            //System.out.println("produit " +produit.toString());
            Optional<Boutique> boutique=boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
            return ResponseEntity.ok(mapperDto.mapperProduitDto(produit,boutique.isPresent() ? boutique.get():new Boutique()));
        } catch (Exception e) {
            log.debug("❌ Aucun produit trouvé pour le code-barres : {}", barcode);
            return ResponseEntity.notFound().build();

        }

    }

    @GetMapping("/prix-articles/{boutiqueid}")
    public ResponseEntity<Map<String, Object>> getPrixArticles(@PathVariable Long boutiqueid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        Page<PrixArticles> prixArticles = commandeService.getPrixArticles(page, size, search,boutiqueid);

        Map<String, Object> response = new HashMap<>();
        response.put("content", prixArticles.getContent());
        response.put("totalElements", prixArticles.getTotalElements());
        response.put("totalPages", prixArticles.getTotalPages());
        response.put("page", prixArticles.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/prix-articles/magasin/{magasinid}")
    public ResponseEntity<Map<String, Object>> getPrixArticlesByMagasin(@PathVariable Long magasinid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        magasinRepository.findByIdAndCompagnie_Id(magasinid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable : " + magasinid));
        Page<PrixArticles> prixArticles = commandeService.getPrixArticlesByMagasin(page, size, search, magasinid);

        Map<String, Object> response = new HashMap<>();
        response.put("content", prixArticles.getContent());
        response.put("totalElements", prixArticles.getTotalElements());
        response.put("totalPages", prixArticles.getTotalPages());
        response.put("page", prixArticles.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/prix-articles/filter/{boutiqueid}")
    public ResponseEntity<List<PrixArticles>> getPrixArticles(@PathVariable Long boutiqueid,
            @RequestParam(required = false) String filters
    ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<PrixArticles> results = commandeService.getPrixArticlesFilster(filters,boutiqueid);
        return ResponseEntity.ok(results);
    }

     @GetMapping("/prix-articles/all/{boutiqueid}")
    public ResponseEntity<List<PrixArticles>> getAllPrixArticles(@PathVariable Long boutiqueid

    ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<PrixArticles> results = commandeService.getPrixArticlesFilster("",boutiqueid);
        return ResponseEntity.ok(results);
    }

     @GetMapping("/alert-stock")
    public ResponseEntity<List<PrixArticles>> alertStock() {
        List<PrixArticles> results = commandeService.alertstock(BigDecimal.valueOf(5.0));
        return ResponseEntity.ok(results);
    }
    @PutMapping("/produits/{produitId}")
    public ResponseEntity<BigDecimal> getStockProduit(@PathVariable Long produitId, @RequestBody StockDepotAndBoutique stockDepotAndBoutique) {
        BigDecimal stock = BigDecimal.ZERO;
//        if (boutiqueid==null || boutiqueid==0L) {
//            stock = commandeService.stockDepot(produitId, boutiqueid);
//            return ResponseEntity.ok(stock);
//        }
        Long boutiqueid = stockDepotAndBoutique.getBoutiqueid();
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
       Optional<Magasin>  depot=magasinRepository.findById(stockDepotAndBoutique.getDepotid());
        if (depot.isPresent() &&  depot.get().getBoutiqueId()==null) {
           stock= commandeService.stockDepot(produitId, stockDepotAndBoutique.getDepotid());
            return ResponseEntity.ok(stock);
        }
        stock = commandeService.stockProduit(produitId, boutiqueid);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/produits/{produitId}/{boutiqueid}/depot")
    public ResponseEntity<BigDecimal> getStockProduitDepot(@PathVariable Long produitId, @PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        BigDecimal stock = commandeService.stockDepot(produitId, boutiqueid);
        return ResponseEntity.ok(stock);
    }
    
    @GetMapping("/ville")
    public ResponseEntity<List<Ville>> getAllVille() {
 
        return ResponseEntity.ok(villeRepositories.findAll());
    }

    // Méthodes utilitaires privées
    private BigDecimal calculateStock(Long produitId, Long depotId, Long boutiqueId, Entreprise entreprise) {
        Produit produit = produitService.findById(produitId);
        Optional<Magasin> depot = depotService.findById(depotId);

        if (boutiqueId != null) {
            // Calcul pour boutique
            Optional<PointVente> pv = pointVenteService.findByProduitAndBoutiqueAndEntreprise(
                    produit, depot.get().getBoutiqueId(), entreprise);

            if (pv != null) {
                Optional<PrixArticles> pa = prixArticlesService.findActiveByEntrepriseAndPointVente(entreprise, pv.get());
                return pa != null ? pa.get().getPointVente().getStockFinalTheorie() : BigDecimal.ZERO;
            }
        } else {
            // Calcul pour dépôt
            Commande derniere = commandeService.getDernierCommandeByProduitByDepot(produit, depot.get());
            return derniere != null ? derniere.getStockFinalTheorie() : BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    private Commande createCommandeFromRequest(CommandeRequest request, Entreprise entreprise, Mois mois) {
        Commande commande = new Commande();

        commande.setPrixAchat(request.getPrixAchat());
        commande.setPrixVente(request.getPrixVente());
        //commande.set
        commande.setQuantite(request.getQuantite());
        // Trace d'audit : identite verifiee par le JWT, jamais celle envoyee
        // par le client dans le corps de la requete (falsifiable) - propage
        // vers DetailCommandeEntree/DetailCommandeSortie/PrixHistorique.
        commande.setUsercreate(tenantContext.currentUsername());

        // Récupération du produit
        Produit produit = produitService.findById(request.getProduitid());
        commande.setProduit(produit);

        // Récupération du magasin et du fournisseur directement par leur id -
        // ne suppose plus qu'une association MagasinFournisseur existe deja
        // pour cette paire precise (un fournisseur "disponible partout" peut
        // etre choisi pour un magasin auquel il n'a jamais ete associe
        // explicitement).
        Magasin magasin = depotService.findById(request.getDepotid())
                .orElseThrow(() -> new IllegalArgumentException("Magasin introuvable : " + request.getDepotid()));
        Fournisseur fournisseur = fournisseurRepositories.findById(request.getFournisseurid())
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable : " + request.getFournisseurid()));
        commande.setMagasinid(magasin);
        commande.setFournisseurid(fournisseur);

        // Génération du numéro si auto
        if ("".equals(request.getNumeroRepartition()) || request.getNumeroRepartition() == null) {
            commande.setNumeroRepartition(generateCodeCommande());
        }

        return commande;
    }

    private void handleBoutiqueApprovisionnement(Commande commande, CommandeRequest request,
            Entreprise entreprise, Mois mois) {
        try {

            // Gestion du code-barres
            Barcodeproduit barcodeProduit = new Barcodeproduit();
            if (request.getBarcode() != null) {
                String codeB = request.getBarcode();
                if (codeB.contains(GlobalFonctions.CARACTER_ASCII)) {
                    codeB = GlobalFonctions.getCodeBare(codeB);
                }
                barcodeProduit.setCodeBard(codeB);
            }

            // Ajout au point de vente
            commandeService.addProduitByPointVente(commande, commande.getMagasinid().getBoutiqueId(), entreprise, mois, commande.getMagasinid(), barcodeProduit);

        } catch (Exception e) {
            log.error("Erreur lors du traitement boutique: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du traitement boutique", e);
        }
    }

    private void updatePointVenteStock(PointVente pv) {
        BigDecimal stockFinal = pv.getStockFinalTheorie();
        BigDecimal stockTestFinal = pv.getEntreeProduit()
                .add(pv.getStockInitial())
                .subtract(pv.getSortiProduit());

        if (!stockFinal.subtract(stockTestFinal).equals(BigInteger.ZERO)) {
            pv.setEntreeProduit(stockFinal);
            pv.setSortiProduit(BigDecimal.ZERO);
            pv.setStockInitial(BigDecimal.ZERO);
            pointVenteService.save(pv);
        }
    }

    private String generateCodeCommande() {
        Long count = commandeService.count();
        boolean exists = true;
        String code = null;

        while (exists) {
            StringBuilder sb = new StringBuilder();
            int length = count.toString().length();

            for (int i = 0; i < 5 - length; i++) {
                sb.append("0");
            }
            sb.append(++count);

            code = sb.toString();
            exists = commandeService.existsByCode(code);
        }

        return code;
    }

    private Entreprise getEntrepriseFromSession(HttpSession session) {
        Entreprise entreprise = (Entreprise) session.getAttribute(GlobalFonctions.ENTREPRISE_ACTIF);
        if (entreprise == null) {
            throw new RuntimeException("Aucune entreprise active dans la session");
        }
        return entreprise;
    }

    private Mois getMoisFromSession(HttpSession session) {
        Mois mois = (Mois) session.getAttribute(GlobalFonctions.MOIS);
        if (mois == null) {
            throw new RuntimeException("Aucun mois actif dans la session");
        }
        return mois;
    }
    
    /**
     * Vérifie si une référence produit existe déjà
     * 
     * GET /microservice-produits/exists/{reference}
     * 
     * @param reference Référence à vérifier
     * @return true si la référence existe, false sinon
     */
    @GetMapping("produit/exists/{reference}")
    @Operation(
        summary = "Vérifier l'existence d'une référence",
        description = "Vérifie si une référence produit existe déjà dans le système. " +
                     "La vérification est insensible à la casse et aux espaces."
    )
    @ApiResponse(responseCode = "200", description = "Vérification effectuée avec succès")
    @ApiResponse(responseCode = "400", description = "Référence invalide")
    public ResponseEntity<Boolean> existsByReference(
            @Parameter(description = "Référence à vérifier (2-50 caractères)", required = true)
            @PathVariable
            @NotBlank(message = "La référence ne peut pas être vide")
            @Size(min = 2, max = 50, message = "La référence doit contenir entre 2 et 50 caractères")
            String reference) {

        log.debug("🔍 Vérification existence référence: {}", reference);

        try {
            // Validation et nettoyage
            String cleanReference = validateAndCleanReference(reference);
            
            // Vérification de l'existence
            boolean exists = produitService.existsByReference(cleanReference);
            
            log.debug("✅ Résultat vérification '{}': {}", cleanReference, exists);

            // Cache côté client pendant 1 minute
            CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.MINUTES)
                    .cachePublic();

            return ResponseEntity.ok()
                    .cacheControl(cacheControl)
                    .header("X-Reference-Checked", cleanReference)
                    .header("X-Reference-Exists", String.valueOf(exists))
                    .body(exists);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Référence invalide: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la vérification de la référence", e);
            // En cas d'erreur serveur, retourner false pour ne pas bloquer l'utilisateur
            return ResponseEntity.ok(false);
        }
    }
    
    /**
     * Valide et nettoie une référence
     * 
     * @param reference Référence brute
     * @return Référence nettoyée
     * @throws IllegalArgumentException si la référence est invalide
     */
    private String validateAndCleanReference(String reference) {
        if (!StringUtils.hasText(reference)) {
            throw new IllegalArgumentException("La référence ne peut pas être vide");
        }

        // Nettoyer la référence (trim, normaliser)
        String cleaned = reference.trim();

        // Vérifier la longueur
        if (cleaned.length() < 2) {
            throw new IllegalArgumentException("La référence doit contenir au moins 2 caractères");
        }

        if (cleaned.length() > 50) {
            throw new IllegalArgumentException("La référence ne peut pas dépasser 50 caractères");
        }

        // Vérifier les caractères autorisés (optionnel)
        // Pattern: lettres, chiffres, tirets, underscores
        if (!cleaned.matches("^[a-zA-Z0-9_-]+$")) {
            log.warn("⚠️ Référence contient des caractères non autorisés: {}", cleaned);
            // On peut soit rejeter, soit accepter selon les règles métier
        }

        return cleaned;
    }
    
    @GetMapping("/boutique/{id}")
    public ResponseEntity<Boutique> boutiqueid(@PathVariable Long id) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(id);
      
        try {
         Optional<Boutique>  boutique = boutiqueRepositories.findById(id);
            // Authentification de l'utilisateur ici
            // ...if
            if (boutique.isPresent()) {
                return ResponseEntity.ok(boutique.get());
            }
            
        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

       return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
      @GetMapping("/depots-stockage-all")
    public ResponseEntity<List<DepoteDto>> allDepots() {
        List<DepoteDto> depots = new ArrayList<>();
        try {
            depots = magasinFournisseurRepositories.findDepotByFournisseurByGroupDepotAndCompagnieId(tenantContext.currentCompagnieId()).stream()
                    //.filter(depot -> depot.getMagasin().getBoutiqueId()==null)
                    .map(d -> mapperDto.mapperDepot(d))
                    .collect(Collectors.toList());
            // Authentification de l'utilisateur ici
            // ...

        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

        return ResponseEntity.ok(depots);
    }
}
