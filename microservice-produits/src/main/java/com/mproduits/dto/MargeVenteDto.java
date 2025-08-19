/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class MargeVenteDto {

    private Long id;
    private String libelle;
    Integer quantite;
    private Integer prixVente;
    private Double montant;
    private Integer achat;
    private Double montanAchat;
    private Double marge;
    private ProduitDto p;
      private String usercreat;
     private Long prixachatid;
     private Date datevente;
    
}
