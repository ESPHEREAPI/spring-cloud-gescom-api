/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.VersementClient;
import com.mproduits.repositories.VersementClientRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class VersementClientServiceImpl implements VersementClientService {
    private final VersementClientRepository versementRepository;

    public VersementClientServiceImpl(VersementClientRepository versementRepository) {
        this.versementRepository = versementRepository;
    }

    @Override
    public VersementClient save(VersementClient versement) {
        return versementRepository.save(versement);
    }

    @Override
    public List<VersementClient> findByFactureId(Long factureId) {
        return versementRepository.findByFactureId(factureId);
    }

    @Override
    public BigDecimal getTotalVersementsByFacture(Long factureId) {
        return versementRepository.sumMontantByFactureId(factureId);
    }
}

