/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;


import com.mproduits.model.Ville;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {
    
    @Query("SELECT v FROM Ville v WHERE " +
           "LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.libelle) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Ville> findBySearch(@Param("search") String search, Pageable pageable);
    
}
