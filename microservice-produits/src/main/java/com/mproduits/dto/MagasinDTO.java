/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

/**
 *
 * @author USER01
 */

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagasinDTO {
    private Long id;
    private String code;
    private String libelle;
    private Long boutiqueId;
    private Long villeId;
    private String villeName;
    private String boutiqueName;
 
    
}
