/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Client;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER01
 */
public interface ClientService {
     Client save(Client client);
    Client update(Long id, Client client);
    void delete(Long id);
    List<Client> findAll();
    Optional<Client> findById(Long id);
    List<Client> searchByNom(String nom);
}
