package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Barcodeproduit;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Gestion des associations code-barres / prix article de la compagnie de
 * l'appelant. Distinct du flux de scan en caisse (BarcodeService /
 * VenteComponent) : cet ecran sert a creer/modifier/supprimer les
 * associations elles-memes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BarcodeGestionService {

    private final BarcodeproduitRepositories barcodeproduitRepository;
    private final PrixArticlesRepositories prixArticlesRepository;
    private final ProduitRepositories produitRepository;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public Page<Barcodeproduit> findAll(Pageable pageable) {
        return barcodeproduitRepository.findByCompagnieId(compagnieCourante(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Barcodeproduit> findAll() {
        return barcodeproduitRepository.findByCompagnieId(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Barcodeproduit> findById(Long id) {
        return barcodeproduitRepository.findByIdAndCompagnieId(id, compagnieCourante());
    }

    public Barcodeproduit create(String codeBard, Long produitId, Long boutiqueId) {
        Long compagnieId = compagnieCourante();

        if (barcodeproduitRepository.findByCodeBardAndCompagnieId(codeBard, compagnieId).isPresent()) {
            throw new BadRequestException("Ce code-barres est deja associe a un article");
        }

        Produit produit = produitRepository.findByIdAndCompagnie_Id(produitId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouve avec l'ID: " + produitId));

        Entreprise exerciceActif = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);

        PrixArticles prixArticles = prixArticlesRepository
                .findLastActiveByEntrepriseAndProduit(exerciceActif, produit, boutiqueId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun prix actif trouve pour ce produit dans cette boutique"));

        Barcodeproduit barcodeproduit = new Barcodeproduit();
        barcodeproduit.setCodeBard(codeBard);
        barcodeproduit.setPrixArticles(prixArticles);
        return barcodeproduitRepository.save(barcodeproduit);
    }

    public Barcodeproduit updateCode(Long id, String codeBard) {
        Barcodeproduit existant = barcodeproduitRepository.findByIdAndCompagnieId(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Association non trouvee avec l'ID: " + id));
        existant.setCodeBard(codeBard);
        return barcodeproduitRepository.save(existant);
    }

    public void deleteById(Long id) {
        Barcodeproduit existant = barcodeproduitRepository.findByIdAndCompagnieId(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Association non trouvee avec l'ID: " + id));
        barcodeproduitRepository.deleteById(existant.getId());
    }
}
