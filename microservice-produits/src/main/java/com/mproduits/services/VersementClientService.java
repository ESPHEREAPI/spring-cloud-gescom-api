/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.VersementClient;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author USER01
 */
public interface VersementClientService {
     VersementClient save(VersementClient versement);
    List<VersementClient> findByFactureId(Long factureId);
    BigDecimal getTotalVersementsByFacture(Long factureId);
}
