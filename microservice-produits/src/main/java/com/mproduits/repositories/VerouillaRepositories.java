/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Verrouillage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author USER01
 */
public interface VerouillaRepositories extends JpaRepository<Verrouillage, Long>{

    @Query("SELECT v FROM Verrouillage v WHERE v.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY v.datedebut DESC")
    Page<Verrouillage> findByCompagnieId(@Param("compagnieId") Long compagnieId, Pageable pageable);

    @Query("SELECT v FROM Verrouillage v WHERE v.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY v.datedebut DESC")
    List<Verrouillage> findByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT v FROM Verrouillage v WHERE v.id = :id AND v.entreprise.entreprisePK.compagnieId = :compagnieId")
    Optional<Verrouillage> findByIdAndCompagnieId(@Param("id") Long id, @Param("compagnieId") Long compagnieId);
}
