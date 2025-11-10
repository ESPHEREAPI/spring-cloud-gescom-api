/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Client;
import com.mproduits.repositories.ClientRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 *
 * @author USER01
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ClientService {
      private final ClientRepository clientRepository;
   // CRUD
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }
    
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }
    
    public Client create(Client client) {
        log.info("Création client: {}", client.getNom());
        return clientRepository.save(client);
    }
    
    public Client update(Long id, Client client) {
        client.setId(id);
        log.info("Mise à jour client: {}", id);
        return clientRepository.save(client);
    }
    
    public void delete(Long id) {
        log.info("Suppression client: {}", id);
        clientRepository.deleteById(id);
    }
    
    // ✅ RECHERCHE
    public List<Client> search(String searchTerm) {
        return clientRepository.search(searchTerm);
    }
    
    // ✅ FILTRE STATUT
    public Page<Client> findByStatut(String statut, Pageable pageable) {
        return clientRepository.findByStatut(statut, pageable);
    }
    
    // ✅ LISTE PAR STATUT
    public List<Client> findAllByStatut(String statut) {
        return clientRepository.findAllByStatut(statut);
    }
    
    // ✅ CLIENTS FIDÉLITÉ
    public List<Client> findFidelites() {
        return clientRepository.findByFideliteTrue();
    }
    
    // ✅ PAR VILLE
    public List<Client> findByVille(String ville) {
        return clientRepository.findByVille(ville);
    }
    
    // ✅ PAR RÉGION
    public List<Client> findByRegion(String region) {
        return clientRepository.findByRegion(region);
    }
    
    // STATISTIQUES
    public Object getStatistics() {
        long total = clientRepository.count();
        long actifs = clientRepository.countByStatut("ACTIF");
        long inactifs = clientRepository.countByStatut("INACTIF");
        long enAttente = clientRepository.countByStatut("EN_ATTENTE");
        long fideles = clientRepository.countByFideliteTrue();
        
        return Map.of(
            "total", total,
            "actifs", actifs,
            "inactifs", inactifs,
            "enAttente", enAttente,
            "fideles", fideles
        );
    } 
    public List<Client> allClient(){
     return clientRepository.findAll();
    }
}
