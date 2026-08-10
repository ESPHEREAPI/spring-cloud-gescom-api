package com.mproduits.dto;

import java.util.List;
import lombok.Data;

@Data
public class CompagniePubliqueDTO {
    private Long id;
    private String nom;
    private String code;
    private String logoChemin;
    private List<BoutiquePubliqueDTO> boutiques;
}
