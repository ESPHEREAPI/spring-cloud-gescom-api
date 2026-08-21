package com.mproduits.services;

import com.mproduits.dto.SpecificationArticleDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.model.Produit;
import com.mproduits.model.Specifique;
import com.mproduits.model.Specificationarticles;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.SpecificationarticlesRepositories;
import com.mproduits.repositories.SpecifiqueRepositories;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD des valeurs de specification d'un produit (ex. Produit #42 →
 * "Auteur" = "Victor Hugo"). Chaque operation verifie que le produit ET
 * la specification appartiennent a la compagnie de l'appelant (jamais
 * fourni par le client, toujours derive du token JWT - meme reflexe que
 * SpecifiqueService), et qu'une specification rattachee a une categorie
 * n'est ajoutee qu'a un produit de cette meme categorie.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SpecificationarticlesService {

    private final SpecificationarticlesRepositories specificationarticlesRepository;
    private final ProduitRepositories produitRepositories;
    private final SpecifiqueRepositories specifiqueRepository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<SpecificationArticleDTO> lister(Long produitId) {
        Long compagnieId = compagnieCourante();
        produitRepositories.findByIdAndCompagnie_Id(produitId, compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));

        return specificationarticlesRepository.findByArtticleId_Id(produitId).stream()
                .map(this::toDTO)
                .toList();
    }

    public SpecificationArticleDTO ajouter(Long produitId, Long specifiqueId, String valeur) {
        Long compagnieId = compagnieCourante();
        Produit produit = produitRepositories.findByIdAndCompagnie_Id(produitId, compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));
        Specifique specifique = specifiqueRepository.findByIdAndCompagnie_Id(specifiqueId, compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification introuvable : " + specifiqueId));

        if (specifique.getCategorie() != null
                && (produit.getCategorie() == null || !specifique.getCategorie().getId().equals(produit.getCategorie().getId()))) {
            throw new BadRequestException(
                    "Cette specification (" + specifique.getLibelle() + ") ne s'applique pas a la categorie de ce produit");
        }

        Specificationarticles entite = new Specificationarticles();
        entite.setArtticleId(produit);
        entite.setSpecifiqueId(specifique);
        entite.setLibelle(valeur);
        return toDTO(specificationarticlesRepository.save(entite));
    }

    public SpecificationArticleDTO modifier(Long id, String valeur) {
        Specificationarticles entite = trouverEtVerifierAppartenance(id);
        entite.setLibelle(valeur);
        return toDTO(specificationarticlesRepository.save(entite));
    }

    public void supprimer(Long id) {
        Specificationarticles entite = trouverEtVerifierAppartenance(id);
        specificationarticlesRepository.deleteById(entite.getId());
    }

    // Les URLs de modification/suppression (voir SpecificationarticlesController)
    // n'incluent pas le produitId (elles reprennent celles deja attendues par
    // le frontend existant) - la verification d'appartenance compagnie se
    // fait donc via le produit deja lie a l'entite plutot qu'un produitId
    // fourni separement.
    private Specificationarticles trouverEtVerifierAppartenance(Long id) {
        Specificationarticles entite = specificationarticlesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Valeur de specification introuvable : " + id));
        Long compagnieId = compagnieCourante();
        Produit produit = entite.getArtticleId();
        if (produit == null || produit.getCompagnie() == null || !compagnieId.equals(produit.getCompagnie().getId())) {
            throw new ResourceNotFoundException("Valeur de specification introuvable : " + id);
        }
        return entite;
    }

    private SpecificationArticleDTO toDTO(Specificationarticles entite) {
        return new SpecificationArticleDTO(
                entite.getId(),
                entite.getArtticleId().getId(),
                entite.getSpecifiqueId().getId(),
                entite.getSpecifiqueId().getLibelle(),
                entite.getLibelle());
    }
}
