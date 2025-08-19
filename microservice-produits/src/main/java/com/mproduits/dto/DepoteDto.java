/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Boutique;
import com.mproduits.model.Fournisseur;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class DepoteDto {
    Long id;
    String code;
    String libelle;
    Boutique boutique;
    Fournisseur fournisseur;
            
    
}
