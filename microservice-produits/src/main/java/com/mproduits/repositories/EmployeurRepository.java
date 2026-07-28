/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Employeur;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface EmployeurRepository extends JpaRepository<Employeur, Long>{
    /**
     * Trouve l'entreprise active (il ne devrait y en avoir qu'une seule)
     */
    //Optional<Entreprise> findByActifTrue();
    
    /**
     * Trouve toutes les entreprises actives
     * Utilisé pour désactiver les entreprises lors de l'activation d'une nouvelle
     */
   // List<Entreprise> findAllByActifTrue();
       Optional<Employeur> findByAbreviation(String abreviation);

    @Query("SELECT e FROM Employeur e ORDER BY e.societe ASC")
    List<Employeur> findAllOrderBySociete();
}
