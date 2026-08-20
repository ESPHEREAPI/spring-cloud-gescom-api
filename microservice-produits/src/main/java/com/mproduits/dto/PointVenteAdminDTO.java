package com.mproduits.dto;

import java.math.BigDecimal;

public record PointVenteAdminDTO(Long id, BigDecimal stockFinalTheorie, BigDecimal sortiProduit,
        ProduitLiteDTO produit, BoutiqueLiteDTO boutique) {
}
