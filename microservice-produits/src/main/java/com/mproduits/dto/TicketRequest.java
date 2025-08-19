/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypePaiement;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Property;
import com.mproduits.model.Vente;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
@Builder
public class TicketRequest {
     private List<LigneVente> lignesVente;
    private Map<String, Object> parameters;
    private Property property;
    private Boolean enregistrer;
    private Boolean remboursementAvecBonAchat;
    private TypePaiement typePaiement;
    private String mois;
    private Vente vente;
}
