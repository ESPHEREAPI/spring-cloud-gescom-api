/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.PaiementAudit;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime;

/**
 *
 * @author USER01
 */
public interface PaiementAuditRepository extends JpaRepository<PaiementAudit, Long> {
    
    @Query("SELECT pa FROM PaiementAudit pa WHERE pa.facture.id = :factureId " +
           "ORDER BY pa.dateCreation DESC")
    List<PaiementAudit> findByFactureId(@Param("factureId") Long factureId);
    
    @Query("SELECT pa FROM PaiementAudit pa WHERE pa.client.id = :clientId " +
           "AND pa.dateCreation >= :from AND pa.dateCreation <= :to")
    List<PaiementAudit> findByClientAndPeriode(@Param("clientId") Long clientId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
    
}
