/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.BonAchat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author USER01
 */
public interface BonAchatRepositories extends JpaRepository<BonAchat, Long>{
    Optional<BonAchat>findByCodeBon(String codeBon);

    List<BonAchat> findByClientBonAchat_Compagnie_Id(Long compagnieId);

    Optional<BonAchat> findByIdAndClientBonAchat_Compagnie_Id(Long id, Long compagnieId);

    Optional<BonAchat> findByCodeBonAndClientBonAchat_Compagnie_Id(String codeBon, Long compagnieId);

    // Montant total des bons emis en remplacement d'un rendu de monnaie pour
    // un ticket donne - voir Historique Caisse (le caissier n'a pas sorti
    // d'especes pour ce montant, il faut donc le rajouter au "cash reel").
    @Query("SELECT COALESCE(SUM(b.montantTotal), 0) FROM BonAchat b " +
           "WHERE b.numeroTicketOrigine = :numeroTicket AND b.clientBonAchat.compagnie.id = :compagnieId")
    BigDecimal sumMontantByNumeroTicketOrigineAndCompagnie(@Param("numeroTicket") String numeroTicket,
                                                            @Param("compagnieId") Long compagnieId);
}
