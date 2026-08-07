/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.ClientBonAchat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author USER01
 */
public interface ClientBonAchatRepositories extends JpaRepository<ClientBonAchat, Long>{

    List<ClientBonAchat> findByCompagnie_Id(Long compagnieId);

    Optional<ClientBonAchat> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
