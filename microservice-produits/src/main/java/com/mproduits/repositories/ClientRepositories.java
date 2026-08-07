/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface ClientRepositories extends JpaRepository<Client, Long>{
    
    Optional<Client>findByNom(String nom);

    List<Client> findByCompagnie_Id(Long compagnieId);

    Optional<Client> findByNomAndCompagnie_Id(String nom, Long compagnieId);
}
