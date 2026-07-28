/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant un contrôle de vente journalier.
 * Consolide toutes les recettes d'une journée (caisse, clients, photocopies, ressources).
 * Cette entité est calculée et non persistée directement.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControleVente {

    /**
     * Date du contrôle (jour de la vente).
     */
    private LocalDate date;

    /**
     * Montant total de la caisse pour cette date.
     * Inclut toutes les ventes en caisse.
     */
    @Builder.Default
    private BigDecimal caisse = BigDecimal.ZERO;

    /**
     * Montant total des versements clients pour cette date.
     * Inclut les paiements des clients.
     */
    @Builder.Default
    private BigDecimal client = BigDecimal.ZERO;

    /**
     * Montant total des photocopies pour cette date.
     * Inclut photocopies, impressions, scanner, etc.
     */
    @Builder.Default
    private BigDecimal photocopies = BigDecimal.ZERO;

    /**
     * Montant total des ressources pour cette date.
     * Inclut les autres types de ressources.
     */
    @Builder.Default
    private BigDecimal resources = BigDecimal.ZERO;

    /**
     * Montant total des remises accordées pour cette date.
     * Montant à déduire du total.
     */
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;

    /**
     * Montant total de toutes les recettes (avant remise).
     * Total = caisse + client + photocopies + resources
     */
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Montant total net (après remise).
     * Total net = total - remise
     */
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    /**
     * Identifiant du mois auquel appartient cette date.
     */
    private Long moisId;

    /**
     * Libellé du mois.
     */
    private String moisLibelle;

    /**
     * Identifiant de l'année.
     */
    private int  anneeId;

    /**
     * Valeur de l'année.
     */
    private Integer anneeValeur;

    /**
     * Calcule le total de toutes les recettes.
     */
    public void calculateTotal() {
        this.total = caisse.add(client).add(photocopies).add(resources);
        this.totalNet = this.total.subtract(remise);
    }

    /**
     * Vérifie si le contrôle a des données.
     *
     * @return true si au moins une recette existe
     */
    public boolean hasData() {
        return total.compareTo(BigDecimal.ZERO) > 0;
    }
}
