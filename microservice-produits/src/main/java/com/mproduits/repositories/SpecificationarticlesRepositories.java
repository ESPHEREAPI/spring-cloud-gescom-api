/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Specificationarticles;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author USER01
 */
public interface SpecificationarticlesRepositories extends JpaRepository<Specificationarticles, Long>{

    List<Specificationarticles> findByArtticleId_Id(Long produitId);

    Optional<Specificationarticles> findByIdAndArtticleId_Id(Long id, Long produitId);
}
