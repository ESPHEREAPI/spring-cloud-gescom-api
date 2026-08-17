package com.mproduits.model;

import com.mproduits.enums.ModeRestauration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trace chaque ligne appliquee par une restauration de stock en masse (voir
 * StockRestaurationService) - meme discipline que HistoriqueCorrectionStock
 * (qui, quand, ancienne/nouvelle quantite), avec en plus le mode utilise et
 * un identifiant de lot commun a tout l'import (batchId) pour retrouver
 * l'ensemble des lignes touchees par une meme restauration.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historique_restauration_stock")
public class HistoriqueRestaurationStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Produit produit;

    @JoinColumn(name = "boutique_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Boutique boutique;

    @JoinColumn(name = "compagnie_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Compagnie compagnie;

    @Column(name = "ancienne_quantite")
    private BigDecimal ancienneQuantite;

    @Column(name = "nouvelle_quantite")
    private BigDecimal nouvelleQuantite;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ModeRestauration mode;

    @Column(name = "utilisateur")
    private String utilisateur;

    @Column(name = "date_restauration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRestauration;
}
