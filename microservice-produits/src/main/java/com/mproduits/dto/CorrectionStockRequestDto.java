package com.mproduits.dto;

import lombok.Data;

import java.util.List;

@Data
public class CorrectionStockRequestDto {

    private String motif;
    private List<PointVenteDto> lignes;
}
