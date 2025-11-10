/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class ClientDto {

    private Long id;
    private String code;
    private String nom;
    private String telephone;
    private String email;
    private boolean fidelite;
    private String adresse;
    private String statut;
    private String region;
    private String ville;
    private String quartier;
    private String bp;
    private String fax;
}
