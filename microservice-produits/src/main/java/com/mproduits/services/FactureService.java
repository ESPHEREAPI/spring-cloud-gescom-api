/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Facture;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER01
 */
public interface FactureService {
     Facture create(Facture facture);
    Facture update(Long id, Facture facture);
    void delete(Long id);
    List<Facture> findAll();
    List<Facture> findByClientId(Long clientId);
    Optional<Facture> findById(Long id);
}
