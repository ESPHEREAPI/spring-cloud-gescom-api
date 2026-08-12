package com.mproduits.services;

import com.mproduits.dto.RessourceConsolideeDTO;
import com.mproduits.enums.StatutVente;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Ressource;
import com.mproduits.model.TypeResource;
import com.mproduits.model.Vente;
import com.mproduits.model.VersementClient;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.RessourceRepositories;
import com.mproduits.repositories.TypeResourceRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.repositories.VersementClientRepository;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RessourceService {

    private final RessourceRepositories repository;
    private final BoutiqueRepositories boutiqueRepositories;
    private final TypeResourceRepositories typeResourceRepositories;
    private final VenteRepositories venteRepositories;
    private final VersementClientRepository versementClientRepository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<Ressource> findAll() {
        return repository.findByBoutique_Compagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public List<Ressource> findByBoutiqueAndPeriode(Long boutiqueId, LocalDate debut, LocalDate fin) {
        verifierBoutique(boutiqueId);
        return repository.findByBoutiqueAndPeriode(boutiqueId, debut, fin);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumByBoutiqueAndPeriode(Long boutiqueId, LocalDate debut, LocalDate fin) {
        verifierBoutique(boutiqueId);
        return repository.sumByBoutiqueAndPeriode(boutiqueId, debut, fin);
    }

    /**
     * Ressources d'une boutique sur une periode, consolidees : ressources
     * manuelles + caisse (ventes TERMINEE) + versements clients (VALIDE) -
     * ces deux derniers sont des types de ressource "systeme" reflechis
     * automatiquement, jamais ressaisis a la main par l'utilisateur.
     */
    @Transactional(readOnly = true)
    public RessourceConsolideeDTO getConsolide(Long boutiqueId, LocalDate debut, LocalDate fin) {
        verifierBoutique(boutiqueId);

        List<Ressource> ressourcesManuelles = repository.findByBoutiqueAndPeriode(boutiqueId, debut, fin);
        BigDecimal totalManuelles = ressourcesManuelles.stream()
                .map(Ressource::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCaisse = venteRepositories.findByBoutiqueAndPeriode(boutiqueId, debut, fin).stream()
                .filter(v -> v.getStatut() == StatutVente.TERMINEE)
                .map(Vente::getTotalBrut)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVersement = versementClientRepository.findByBoutiqueAndPeriode(boutiqueId, debut, fin).stream()
                .filter(v -> "VALIDE".equals(v.getStatut()))
                .map(VersementClient::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = totalManuelles.add(totalCaisse).add(totalVersement);

        return RessourceConsolideeDTO.builder()
                .ressourcesManuelles(ressourcesManuelles)
                .totalRessourcesManuelles(totalManuelles)
                .totalCaisse(totalCaisse)
                .totalVersementClient(totalVersement)
                .total(total)
                .build();
    }

    private Boutique verifierBoutique(Long boutiqueId) {
        return boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvee avec l'ID: " + boutiqueId));
    }

    public Ressource create(Long boutiqueId, Long typeResourceId, Ressource ressource) {
        Long compagnieId = compagnieCourante();
        Boutique boutique = verifierBoutique(boutiqueId);
        TypeResource typeResource = typeResourceRepositories.findByIdAndCompagnie_Id(typeResourceId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Type ressource non trouve avec l'ID: " + typeResourceId));

        ressource.setId(null);
        ressource.setBoutique(boutique);
        ressource.setTypeResource(typeResource);
        return repository.save(ressource);
    }

    public Ressource update(Long id, Ressource ressource) {
        Ressource existant = repository.findByIdAndBoutique_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Ressource non trouvee avec l'ID: " + id));
        existant.setMontant(ressource.getMontant());
        existant.setDateRessource(ressource.getDateRessource());
        existant.setCommentaire(ressource.getCommentaire());
        return repository.save(existant);
    }

    public void deleteById(Long id) {
        Ressource existant = repository.findByIdAndBoutique_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Ressource non trouvee avec l'ID: " + id));
        repository.deleteById(existant.getId());
    }
}
