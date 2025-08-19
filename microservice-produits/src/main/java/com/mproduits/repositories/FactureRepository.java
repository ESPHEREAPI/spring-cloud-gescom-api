/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Facture;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface FactureRepository extends JpaRepository<Facture ,Long>{
    @Query("SELECT f FROM Facture f WHERE f.client.id= :clientId")
    List<Facture>findByClientId(Long clientId);
    
}
