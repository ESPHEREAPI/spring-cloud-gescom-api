/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Specifique;
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
public interface SpecifiqueRepositories extends JpaRepository<Specifique, Long>{

    @Query("SELECT s FROM Specifique s WHERE s.compagnie.id = :compagnieId AND (" +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.libelle) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Specifique> findBySearchAndCompagnieId(@Param("search") String search, @Param("compagnieId") Long compagnieId, Pageable pageable);

    Page<Specifique> findByCompagnie_Id(Long compagnieId, Pageable pageable);

    List<Specifique> findByCompagnie_Id(Long compagnieId);

    Optional<Specifique> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
