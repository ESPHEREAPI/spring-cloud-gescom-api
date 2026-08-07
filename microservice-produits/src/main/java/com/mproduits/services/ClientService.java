/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Client;
import com.mproduits.model.Compagnie;
import com.mproduits.repositories.ClientRepository;
import com.mproduits.security.TenantContext;
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
 * Isolation multi-tenant : toutes les lectures sont scopees a la compagnie de
 * l'utilisateur courant (TenantContext). Les endpoints appeles par un compte
 * sans compagnie (SUPER_ADMIN/SYSTEM_ADMIN) sont deja bloques en amont par
 * TenantScopeFilter, donc currentCompagnieId() est toujours renseigne ici.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ClientService {
      private final ClientRepository clientRepository;
      private final TenantContext tenantContext;
   // CRUD
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findByCompagnie_Id(tenantContext.currentCompagnieId(), pageable);
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId());
    }

    public Client create(Client client) {
        client.setCompagnie(new Compagnie(tenantContext.currentCompagnieId()));
        log.info("Création client: {}", client.getNom());
        return clientRepository.save(client);
    }

    public Client update(Long id, Client client) {
        Client existant = clientRepository.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + id));
        client.setId(id);
        client.setCompagnie(existant.getCompagnie());
        log.info("Mise à jour client: {}", id);
        return clientRepository.save(client);
    }

    public void delete(Long id) {
        clientRepository.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + id));
        log.info("Suppression client: {}", id);
        clientRepository.deleteById(id);
    }

    // ✅ RECHERCHE
    public List<Client> search(String searchTerm) {
        return clientRepository.searchByCompagnie(searchTerm, tenantContext.currentCompagnieId());
    }

    // ✅ FILTRE STATUT
    public Page<Client> findByStatut(String statut, Pageable pageable) {
        return clientRepository.findByStatutAndCompagnie_Id(statut, tenantContext.currentCompagnieId(), pageable);
    }

    // ✅ LISTE PAR STATUT
    public List<Client> findAllByStatut(String statut) {
        return clientRepository.findAllByStatutAndCompagnie_Id(statut, tenantContext.currentCompagnieId());
    }

    // ✅ CLIENTS FIDÉLITÉ
    public List<Client> findFidelites() {
        return clientRepository.findByFideliteTrueAndCompagnie_Id(tenantContext.currentCompagnieId());
    }

    // ✅ PAR VILLE
    public List<Client> findByVille(String ville) {
        return clientRepository.findByVilleAndCompagnie_Id(ville, tenantContext.currentCompagnieId());
    }

    // ✅ PAR RÉGION
    public List<Client> findByRegion(String region) {
        return clientRepository.findByRegionAndCompagnie_Id(region, tenantContext.currentCompagnieId());
    }

    // STATISTIQUES
    public Object getStatistics() {
        Long compagnieId = tenantContext.currentCompagnieId();
        long total = clientRepository.countByCompagnie_Id(compagnieId);
        long actifs = clientRepository.countByStatutAndCompagnie_Id("ACTIF", compagnieId);
        long inactifs = clientRepository.countByStatutAndCompagnie_Id("INACTIF", compagnieId);
        long enAttente = clientRepository.countByStatutAndCompagnie_Id("EN_ATTENTE", compagnieId);
        long fideles = clientRepository.countByFideliteTrueAndCompagnie_Id(compagnieId);

        return Map.of(
            "total", total,
            "actifs", actifs,
            "inactifs", inactifs,
            "enAttente", enAttente,
            "fideles", fideles
        );
    }
    public List<Client> allClient(){
     return clientRepository.findAllByCompagnie_Id(tenantContext.currentCompagnieId());
    }


}
