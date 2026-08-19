package com.mproduits.ecommerce.dto.web;

import com.mproduits.dto.ClientDto;
import com.mproduits.dto.DevisDTO;
import com.mproduits.dto.DevisItemDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Client;
import com.mproduits.model.Compagnie;
import com.mproduits.model.PrixArticles;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.ClientRepository;
import com.mproduits.repositories.CompagnieRepositories;
import com.mproduits.repositories.DevisRepository;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.security.CustomerContext;
import com.mproduits.services.DevisService;
import com.mproduits.services.EntrepriseService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Commande en ligne (checkout) - client connecte (JWT customer, voir
 * CustomerContext) OU invite (champs guestXxx du corps de la requete).
 * Cree un Devis EN_ATTENTE/EN_LIGNE (voir DevisService.creerDevisPublic) :
 * jamais une vente immediate, le personnel valide manuellement (Phase 2,
 * pas encore implementee) avant toute deduction de stock.
 *
 * Recalcule systematiquement prix et disponibilite cote serveur pour
 * chaque ligne - ne jamais faire confiance a un prix/quantite envoye par
 * un visiteur anonyme.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/microservice-produits/e-com/compagnie")
public class EcomCheckoutController {

    private final CompagnieRepositories compagnieRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final ClientRepository clientRepository;
    private final DevisRepository devisRepository;
    private final DevisService devisService;
    private final EntrepriseService entrepriseService;
    private final CustomerContext customerContext;

    public record ItemCommande(Long produitId, Integer quantite) {
    }

    public record CommandeRequest(List<ItemCommande> items,
            String guestNom, String guestEmail, String guestTelephone, String guestAdresse) {
    }

    public record CommandeResponse(Long devisId, String numeroDevis) {
    }

    public record CommandeResumeDTO(Long id, String numeroDevis, String statut, String statutLibelle,
            java.util.Date dateDevis, BigDecimal total) {
    }

    @PostMapping("/{code}/boutiques/{boutiqueId}/commandes")
    public ResponseEntity<CommandeResponse> passerCommande(@PathVariable String code, @PathVariable Long boutiqueId,
            @RequestBody CommandeRequest request) {
        Compagnie compagnie = resolveCompagnie(code);
        Boutique boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnie.getId())
                .orElseThrow(() -> new BadRequestException("Boutique introuvable pour cette compagnie"));

        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("La commande ne contient aucun article");
        }

        Client client = resolveClient(compagnie, request);

        // Recalcul serveur : prix et stock viennent de PrixArticles, jamais
        // du corps de la requete (voir la javadoc de classe).
        List<DevisItemDTO> items = new ArrayList<>();
        for (ItemCommande ligne : request.items()) {
            if (ligne.produitId() == null || ligne.quantite() == null || ligne.quantite() <= 0) {
                throw new BadRequestException("Ligne de commande invalide");
            }
            PrixArticles prixArticles = prixArticlesRepositories
                    .findActifByProduitIdAndBoutiqueId(ligne.produitId(), boutiqueId)
                    .orElseThrow(() -> new BadRequestException("Produit indisponible dans cette boutique : " + ligne.produitId()));

            BigDecimal stockDisponible = prixArticles.getPointVente() != null
                    ? prixArticles.getPointVente().getStockFinalTheorie() : BigDecimal.ZERO;
            if (stockDisponible == null || stockDisponible.compareTo(BigDecimal.valueOf(ligne.quantite())) < 0) {
                throw new BadRequestException("Stock insuffisant pour le produit " + ligne.produitId());
            }

            DevisItemDTO item = new DevisItemDTO();
            item.setProduitId(ligne.produitId());
            item.setQuantite(ligne.quantite());
            item.setPrixUnitaire(prixArticles.getPrixVenteNet());
            items.add(item);
        }

        BigDecimal montantHT = items.stream()
                .map(i -> i.getPrixUnitaire().multiply(BigDecimal.valueOf(i.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(client.getId());

        DevisDTO dto = new DevisDTO();
        dto.setClient(clientDto);
        dto.setItems(items);
        dto.setMontantHT(montantHT);
        dto.setTotalTVA(BigDecimal.ZERO);
        dto.setTotal(montantHT);
        dto.setAppliquerTVA(false);
        dto.setTauxTVA(BigDecimal.ZERO);
        dto.setTotalRemise(BigDecimal.ZERO);
        dto.setVerifierStock(true);

        int anneeid = entrepriseService.obtenirOuCreerExerciceActif(compagnie.getId())
                .getEntreprisePK().getAnneeId();

        var devis = devisService.creerDevisPublic(dto, boutiqueId, anneeid, compagnie.getId());
        return ResponseEntity.ok(new CommandeResponse(devis.getId(), devis.getNumeroDevis()));
    }

    @GetMapping("/{code}/mes-commandes")
    public ResponseEntity<List<CommandeResumeDTO>> mesCommandes(@PathVariable String code) {
        Compagnie compagnie = resolveCompagnie(code);
        Long clientId = customerContext.currentClientId();
        if (clientId == null) {
            throw new BadRequestException("Connexion requise");
        }
        List<CommandeResumeDTO> commandes = devisRepository
                .findByClient_IdAndBoutique_Compagnie_IdOrderByDateDevisDesc(clientId, compagnie.getId())
                .stream()
                .map(d -> new CommandeResumeDTO(d.getId(), d.getNumeroDevis(), d.getStatut().name(),
                        d.getStatut().getLibelle(), d.getDateDevis(), d.getTotal()))
                .toList();
        return ResponseEntity.ok(commandes);
    }

    /**
     * Client connecte (JWT customer, prioritaire) ou invite (champs guestXxx) -
     * un invite avec un email deja associe a un compte existant reutilise ce
     * compte plutot que d'en creer un doublon (meme logique que
     * EcomAuthController.register, dans l'autre sens).
     */
    private Client resolveClient(Compagnie compagnie, CommandeRequest request) {
        Long clientId = customerContext.currentClientId();
        if (clientId != null) {
            Client client = clientRepository.findByIdAndCompagnie_Id(clientId, compagnie.getId())
                    .orElseThrow(() -> new BadRequestException("Compte client invalide pour cette compagnie"));
            return client;
        }

        if (!StringUtils.hasText(request.guestNom()) || !StringUtils.hasText(request.guestTelephone())) {
            throw new BadRequestException("Nom et telephone requis pour une commande invite");
        }

        if (StringUtils.hasText(request.guestEmail())) {
            var existant = clientRepository.findByEmailIgnoreCaseAndCompagnie_Id(request.guestEmail(), compagnie.getId());
            if (existant.isPresent()) {
                return existant.get();
            }
        }

        Client guest = new Client();
        guest.setNom(request.guestNom());
        guest.setTelephone(request.guestTelephone());
        guest.setEmail(request.guestEmail());
        guest.setAdresse(request.guestAdresse());
        guest.setCompagnie(compagnie);
        guest.setStatut("ACTIF");
        guest.setGuestOnly(true);
        return clientRepository.save(guest);
    }

    private Compagnie resolveCompagnie(String code) {
        return compagnieRepositories.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Compagnie introuvable : " + code));
    }
}
