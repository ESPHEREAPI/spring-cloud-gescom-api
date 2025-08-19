/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Devis;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER01
 */
public interface DevisService {
     Devis create(Devis devis);
    Devis update(Long id, Devis devis);
    void delete(Long id);
    Optional<Devis> findById(Long id);
    List<Devis> findByClientId(Long clientId);
}
