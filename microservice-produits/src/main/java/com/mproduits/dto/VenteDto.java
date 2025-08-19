/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.ecommerce.dto.entites.ClientOrder;
import com.mproduits.model.Client;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class VenteDto {

    private Long id;
    private Date date;
    List<Caissedto> items;
    private String typePaiement;
    BigDecimal montantTotal, montantRecu, monnaieRendue, montantNet;
    private Client client;
    BigDecimal remise;
    private String userinsert;
    private String numeroTicket;
    private Long pointventeid;
    private String statut;
    private String userecom;
    private ClientOrder clientOrder;
    private long numerocommande;

}
