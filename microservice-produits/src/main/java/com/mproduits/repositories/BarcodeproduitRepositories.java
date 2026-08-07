/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Barcodeproduit;
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
public interface BarcodeproduitRepositories extends JpaRepository<Barcodeproduit, Long>{
   Barcodeproduit findByCodeBard(String codeBard );

    @Query("SELECT b FROM Barcodeproduit b WHERE b.prixArticles.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY b.id DESC")
    Page<Barcodeproduit> findByCompagnieId(@Param("compagnieId") Long compagnieId, Pageable pageable);

    @Query("SELECT b FROM Barcodeproduit b WHERE b.prixArticles.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY b.id DESC")
    List<Barcodeproduit> findByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT b FROM Barcodeproduit b WHERE b.id = :id AND b.prixArticles.entreprise.entreprisePK.compagnieId = :compagnieId")
    Optional<Barcodeproduit> findByIdAndCompagnieId(@Param("id") Long id, @Param("compagnieId") Long compagnieId);

    @Query("SELECT b FROM Barcodeproduit b WHERE b.codeBard = :codeBard AND b.prixArticles.entreprise.entreprisePK.compagnieId = :compagnieId")
    Optional<Barcodeproduit> findByCodeBardAndCompagnieId(@Param("codeBard") String codeBard, @Param("compagnieId") Long compagnieId);
}
