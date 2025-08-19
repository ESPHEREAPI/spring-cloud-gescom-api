/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Facture;
import com.mproduits.repositories.FactureRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class FactureServiceImpl implements FactureService {
    private final FactureRepository factureRepository;

    public FactureServiceImpl(FactureRepository factureRepository) {
        this.factureRepository = factureRepository;
    }

    @Override
    public Facture create(Facture facture) {
        return factureRepository.save(facture);
    }

    @Override
    public Facture update(Long id, Facture facture) {
        facture.setId(id);
        return factureRepository.save(facture);
    }

    @Override
    public void delete(Long id) {
        factureRepository.deleteById(id);
    }

    @Override
    public List<Facture> findAll() {
        return factureRepository.findAll();
    }

    @Override
    public List<Facture> findByClientId(Long clientId) {
        return factureRepository.findByClientId(clientId);
    }

    @Override
    public Optional<Facture> findById(Long id) {
        return factureRepository.findById(id);
    }
}

