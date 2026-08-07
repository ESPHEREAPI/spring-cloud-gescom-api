/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Categories;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface CategorieRepositories extends JpaRepository<Categories, Long>{

    @Query("SELECT c FROM Categories c WHERE c.compagnie.id = :compagnieId AND (" +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.libelle) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Categories> findBySearchAndCompagnieId(@Param("search") String search, @Param("compagnieId") Long compagnieId, Pageable pageable);

    Page<Categories> findByCompagnie_Id(Long compagnieId, Pageable pageable);

    List<Categories> findByCompagnie_Id(Long compagnieId);

    Optional<Categories> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
