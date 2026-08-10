package com.mproduits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 * Miroir minimal (lecture seule) de sid.service_admin.model.CompagnieParametres,
 * meme table physique partagee ("compagnie_parametres") - proprietaire reel :
 * microservice-administration (ecran "Option Entreprise", modifiable par
 * l'administrateur de la compagnie). Ce module ne lit que les champs dont il
 * a besoin, meme pattern que la duplication existante de Compagnie.
 */
@Entity
@Table(name = "compagnie_parametres")
@Data
public class CompagnieParametres implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "compagnie_id")
    private Compagnie compagnie;

    @Column(name = "bon_achat_duree_validite_jours")
    private Integer bonAchatDureeValiditeJours;
}
