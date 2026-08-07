/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Boutique;
import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface BoutiqueRepositories extends JpaRepository<Boutique, Long>{

    @Query("SELECT b FROM Boutique b WHERE b.compagnie.id = :compagnieId AND (" +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.quartier) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Boutique> findBySearchAndCompagnieId(@Param("search") String search, @Param("compagnieId") Long compagnieId, Pageable pageable);

    Page<Boutique> findByCompagnie_Id(Long compagnieId, Pageable pageable);

    List<Boutique> findByCompagnie_Id(Long compagnieId);

    Optional<Boutique> findByIdAndCompagnie_Id(Long id, Long compagnieId);

    @Query("SELECT COUNT(b) FROM Boutique b WHERE b.compagnie.id = :compagnieId")
    long countByCompagnieId(@Param("compagnieId") Long compagnieId);
}
