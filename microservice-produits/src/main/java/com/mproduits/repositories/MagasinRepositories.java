/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Magasin;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface MagasinRepositories  extends JpaRepository<Magasin, Long>{
    @Query("SELECT m FROM Magasin m WHERE m.boutique is not null GROUP BY m.boutique")
    List<Magasin> findByBoutiqueDistinctIsNotNull();
    
}
