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
import com.mproduits.model.Taxeproduit;
import com.mproduits.model.Ville;
import com.mproduits.repositories.AnneeRepository;
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.FournisseurRepositories;
import com.mproduits.repositories.MagasinFournisseurRepositories;
import com.mproduits.repositories.MagasinRepositories;
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
    private final CategorieRepositories categorieService;
    private final SpecifiqueRepositories specifiqueService;
    private final SpecificationarticlesRepositories specificationArticlesService;
    private final PrixArticlesRepositories prixArticlesService;
    private final MagasinFournisseurRepositories depotFournisseurService;
    private final VerouillaRepositories verouillageService;
    private final PointVenteRepositories pointVenteService;
    private final BarcodeproduitRepositories barcodeProduitService;
    private final MagasinRepositories depotService;
    private final MagasinFournisseurRepositories magasinFournisseurRepositories;
    private final FournisseurRepositories fourniService;
    private final IParamModule paramModuleService;
    private final AnneeRepository anneerepository;
    private final EntrepriseRepositories entrepriseRepositories;
    private final VilleRepositories villeRepositories;
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

        Entreprise entreprise = entrepriseRepositories.findByActif(Boolean.TRUE);

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

        if (destinationDepot) {
            destinations = (List<MagasinFournisseur>) depotFournisseurService.findDepotByFournisseurInAnnee();
        } else {
            destinations = (List<MagasinFournisseur>) depotFournisseurService.findDepotByFournisseurHaveBoutique();
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
                    .body("Erreur lors de la génération du numéro");
        }
    }

    @GetMapping("/produits/reference/{reference}")
    public ResponseEntity<ProduitDto> produitsByReference(@PathVariable String reference) {

        ProduitDto pdto = produitService.findByReference(reference);
        return ResponseEntity.ok(pdto);
    }

    @GetMapping("/produits/reference/{barcode}")
    public ResponseEntity<ProduitDto> produitsBycodeBar(@PathVariable String barcode) {

        Barcodeproduit barcodeProduit = barcodeProduitService.findByCodeBard(barcode);

        return ResponseEntity.ok(barcodeProduit != null ? mapperDto.mapperProduitDto(barcodeProduit.getProduit()) : new ProduitDto());
    }

    @GetMapping("/taxes")
    public ResponseEntity<List<Taxeproduit>> getTaxesProduit(HttpSession session) {
        Entreprise entreprise = getEntrepriseFromSession(session);
        Mois mois = getMoisFromSession(session);

        List<Taxeproduit> taxes = (List<Taxeproduit>) taxeProduitService.findByEntrepriseAndMois(entreprise, mois);
        return ResponseEntity.ok(taxes);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Categories>> allCategories() {

        List<Categories> categories = categorieService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/depots")
    public ResponseEntity<List<DepoteDto>> allDepots() {
        List<DepoteDto> depots = new ArrayList<>();
        try {
            depots = magasinFournisseurRepositories.findAll().stream()
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
            depots = magasinFournisseurRepositories.findAll().stream()
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
            depots = depotService.findByBoutiqueDistinctIsNotNull().stream()
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
        List<Boutique> boutiques = new ArrayList<>();
        try {
            boutiques = depotService.findByBoutiqueDistinctIsNotNull().stream()
                    .map(d -> d.getBoutiqueId())
                    .collect(Collectors.toList());
            // Authentification de l'utilisateur ici
            // ...

        } catch (Exception e) {
            System.out.println("com.mproduits.web.controller.CommandeController.allDepots()" + e.getMessage() + ' ' + e.getLocalizedMessage());
        }

        return ResponseEntity.ok(boutiques);
    }

    @GetMapping("/fournisseurs")
    public ResponseEntity<List<Fournisseur>> allFournisseur() {

        List<Fournisseur> fournisseurs = fourniService.findAll();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/produits")
    public ResponseEntity<List<ProduitDto>> produits() {

        List<ProduitDto> allProduits = produitService.getAllProduit();
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

    @GetMapping("/produits/barcode/{barcode}")
    public ResponseEntity<ProduitDto> getProduitBarCode(@PathVariable String barcode, HttpSession session) {
        System.out.println("code bar " + barcode);

        Barcodeproduit bar = null;
        try {
            bar = barcodeProduitService.findByCodeBard(barcode);
            //log.debug("✅ Produit trouvé : {}", bar.toString());

            Produit produit = bar == null ? new Produit() : bar.getPrixArticles().getPointVente().getProduit();
            //System.out.println("produit " +produit.toString());
            return ResponseEntity.ok(mapperDto.mapperProduitDto(produit));
        } catch (Exception e) {
            log.debug("❌ Aucun produit trouvé pour le code-barres : {}", barcode);
            return ResponseEntity.notFound().build();

        }

    }

    @GetMapping("/prix-articles")
    public ResponseEntity<Map<String, Object>> getPrixArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Page<PrixArticles> prixArticles = commandeService.getPrixArticles(page, size, search);

        Map<String, Object> response = new HashMap<>();
        response.put("content", prixArticles.getContent());
        response.put("totalElements", prixArticles.getTotalElements());
        response.put("totalPages", prixArticles.getTotalPages());
        response.put("page", prixArticles.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/prix-articles/filter")
    public ResponseEntity<List<PrixArticles>> getPrixArticles(
            @RequestParam(required = false) String filters
    ) {
        List<PrixArticles> results = commandeService.getPrixArticlesFilster(filters);
        return ResponseEntity.ok(results);
    }
    
     @GetMapping("/prix-articles/all")
    public ResponseEntity<List<PrixArticles>> getAllPrixArticles(
          
    ) {
        List<PrixArticles> results = commandeService.getPrixArticlesFilster("");
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
        if (boutiqueid==0L || boutiqueid==null) {
           stock= commandeService.stockDepot(produitId, stockDepotAndBoutique.getDepotid());
            return ResponseEntity.ok(stock);
        }
        stock = commandeService.stockProduit(produitId, boutiqueid);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/produits/{produitId}/{boutiqueid}/depot")
    public ResponseEntity<BigDecimal> getStockProduitDepot(@PathVariable Long produitId, @PathVariable Long boutiqueid) {
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
        commande.setQuantite(request.getQuantite());
        commande.setUsercreate(request.getUsercreate());

        // Récupération du produit
        Produit produit = produitService.findById(request.getProduitid());
        commande.setProduit(produit);

        // Récupération du dépôt fournisseur
        Optional<MagasinFournisseur> depotFournisseur = depotFournisseurService.findById(new MagasinFournisseurPK(request.getDepotid(), request.getFournisseurid()));
        commande.setMagasinid(depotFournisseur.get().getMagasin());
        commande.setFournisseurid(depotFournisseur.get().getFournisseur());

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

}
