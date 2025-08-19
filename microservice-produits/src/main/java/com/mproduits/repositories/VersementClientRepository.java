/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.VersementClient;
import feign.Param;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface VersementClientRepository extends JpaRepository<VersementClient, Long> {

    @Query("SELECT v FROM VersementClient v WHERE v.facture.id= :factureId")
    List<VersementClient> findByFactureId(@Param("factureId")Long factureId);

    @Query("SELECT SUM(v.montant) FROM VersementClient v WHERE v.facture.id = :factureId")
    BigDecimal sumMontantByFactureId(@Param("factureId") Long factureId);

}
