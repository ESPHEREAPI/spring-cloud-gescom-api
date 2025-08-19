/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.UserDTO;
import com.mproduits.model.Personne;
import com.mproduits.model.Profil;
import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface PersonneRepositories extends JpaRepository<Personne, Long> {

    Optional<Personne> findByUserName(String userName);

    Optional<Personne> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Personne p WHERE p.profilid = :profil AND p.compteActif = true")
    List<Personne> findActiveUserByProfil(@Param("profil") Profil profil);

}
