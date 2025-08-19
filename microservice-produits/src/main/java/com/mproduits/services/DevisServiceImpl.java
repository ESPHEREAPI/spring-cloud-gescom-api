/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Devis;
import com.mproduits.repositories.DevisRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class DevisServiceImpl implements DevisService {
    private final DevisRepository devisRepository;

    public DevisServiceImpl(DevisRepository devisRepository) {
        this.devisRepository = devisRepository;
    }

    @Override
    public Devis create(Devis devis) {
        return devisRepository.save(devis);
    }

    @Override
    public Devis update(Long id, Devis devis) {
        devis.setId(id);
        return devisRepository.save(devis);
    }

    @Override
    public void delete(Long id) {
        devisRepository.deleteById(id);
    }

    @Override
    public Optional<Devis> findById(Long id) {
        return devisRepository.findById(id);
    }

    @Override
    public List<Devis> findByClientId(Long clientId) {
        return devisRepository.findByClientId(clientId);
    }
}
