/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Boutique;
import feign.Param;
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
    
    @Query("SELECT b FROM Boutique b WHERE " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.quartier) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Boutique> findBySearch(@Param("search") String search, Pageable pageable);
}
