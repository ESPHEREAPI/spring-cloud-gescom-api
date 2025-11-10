/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.FactureItemSnapshot;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 *
 * @author USER01
 */

    public interface FactureItemSnapshotRepository extends JpaRepository<FactureItemSnapshot, Long> {
    
    @Query("SELECT fis FROM FactureItemSnapshot fis WHERE fis.facture.id = :factureId")
    List<FactureItemSnapshot> findByFactureId(@Param("factureId") Long factureId);
}
