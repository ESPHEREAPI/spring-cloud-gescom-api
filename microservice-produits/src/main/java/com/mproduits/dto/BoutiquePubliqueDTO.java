package com.mproduits.dto;

import lombok.Data;

@Data
public class BoutiquePubliqueDTO {
    private Long id;
    private String nom;
    private String quartier;
    private boolean principale;
}
