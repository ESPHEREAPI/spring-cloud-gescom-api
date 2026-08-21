package com.mproduits.dto;

import java.math.BigDecimal;

/** Une ligne de l'edition en masse des prix (voir CommandeController.definirPrixEnMasse). */
public record PrixEnMasseItem(Long id, BigDecimal prixVenteNet) {
}
