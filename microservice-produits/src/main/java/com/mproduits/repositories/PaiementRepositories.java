/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Paiement;
import com.mproduits.model.Vente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author USER01
 */
public interface PaiementRepositories extends JpaRepository<Paiement, Long>{
    // Une vente peut avoir plusieurs lignes de paiement (paiement mixte) -
    // ne pas utiliser une methode derivee a resultat unique ici, elle
    // leverait IncorrectResultSizeDataAccessException des qu'il y a 2+ lignes.
    List<Paiement> findAllByVente(Vente vente);

    void deleteAllByVente(Vente vente);
}
