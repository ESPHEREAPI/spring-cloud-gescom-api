/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface EntrepriseRepositories extends JpaRepository<Entreprise, EntreprisePK>{

    /**
     * @deprecated recherche globale non scopee par compagnie (fuite multi-tenant :
     * renvoie l'exercice actif d'une compagnie quelconque). Conservee pour
     * compatibilite avec les appelants pas encore migres vers
     * findFirstByEntreprisePK_CompagnieIdAndActifTrue. Ne pas utiliser dans du code neuf.
     */
    @Deprecated
    Entreprise findByActif(Boolean actif);

    /** Exercice actif de la compagnie courante - toujours derive du TenantContext, jamais du client. */
    Optional<Entreprise> findFirstByEntreprisePK_CompagnieIdAndActifTrue(Long compagnieId);
}
