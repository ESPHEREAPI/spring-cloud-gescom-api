package com.mproduits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompagnieDashboardDTO {

    private BigDecimal totalVentesJour;
    private long nombreVentesJour;

    private BigDecimal totalVentesMois;
    private long nombreVentesMois;

    private BigDecimal montantImpaye;
    private long nombreFacturesImpayees;

    private long nombreProduitsStockFaible;

    private List<PointEvolution> evolutionVentes;
    private List<VenteParBoutique> ventesParBoutique;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointEvolution {
        private String mois;
        private BigDecimal total;
        private long nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenteParBoutique {
        private Long boutiqueId;
        private String boutiqueNom;
        private BigDecimal total;
    }
}
