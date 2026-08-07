package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Charge;
import com.mproduits.model.TypeDepense;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.ChargeRepositories;
import com.mproduits.repositories.TypeDepenseRepositories;
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
public class ChargeService {

    private final ChargeRepositories repository;
    private final BoutiqueRepositories boutiqueRepositories;
    private final TypeDepenseRepositories typeDepenseRepositories;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<Charge> findAll() {
        return repository.findByBoutique_Compagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public List<Charge> findByBoutiqueAndPeriode(Long boutiqueId, LocalDate debut, LocalDate fin) {
        verifierBoutique(boutiqueId);
        return repository.findByBoutiqueAndPeriode(boutiqueId, debut, fin);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumByBoutiqueAndPeriode(Long boutiqueId, LocalDate debut, LocalDate fin) {
        verifierBoutique(boutiqueId);
        return repository.sumByBoutiqueAndPeriode(boutiqueId, debut, fin);
    }

    private Boutique verifierBoutique(Long boutiqueId) {
        return boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvee avec l'ID: " + boutiqueId));
    }

    public Charge create(Long boutiqueId, Long typeDepenseId, Charge charge) {
        Long compagnieId = compagnieCourante();
        Boutique boutique = verifierBoutique(boutiqueId);
        TypeDepense typeDepense = typeDepenseRepositories.findByIdAndCompagnie_Id(typeDepenseId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Type depense non trouve avec l'ID: " + typeDepenseId));

        charge.setId(null);
        charge.setBoutique(boutique);
        charge.setTypeDepense(typeDepense);
        return repository.save(charge);
    }

    public Charge update(Long id, Charge charge) {
        Charge existant = repository.findByIdAndBoutique_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Charge non trouvee avec l'ID: " + id));
        existant.setMontant(charge.getMontant());
        existant.setDateCharge(charge.getDateCharge());
        existant.setCommentaire(charge.getCommentaire());
        return repository.save(existant);
    }

    public void deleteById(Long id) {
        Charge existant = repository.findByIdAndBoutique_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Charge non trouvee avec l'ID: " + id));
        repository.deleteById(existant.getId());
    }
}
