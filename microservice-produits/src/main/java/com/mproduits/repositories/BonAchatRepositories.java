/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.BonAchat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author USER01
 */
public interface BonAchatRepositories extends JpaRepository<BonAchat, Long>{
    Optional<BonAchat>findByCodeBon(String codeBon);

    List<BonAchat> findByClientBonAchat_Compagnie_Id(Long compagnieId);

    Optional<BonAchat> findByIdAndClientBonAchat_Compagnie_Id(Long id, Long compagnieId);

    Optional<BonAchat> findByCodeBonAndClientBonAchat_Compagnie_Id(String codeBon, Long compagnieId);
}
