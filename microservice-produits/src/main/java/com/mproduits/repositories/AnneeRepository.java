/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Annee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface AnneeRepository extends JpaRepository<Annee, Integer>{
    public Annee findById(int id);
     Optional<Annee> findByCode(String code);

    @Query("SELECT a FROM Annee a ORDER BY a.code DESC")
    List<Annee> findAllOrderByCodeDesc();
}
