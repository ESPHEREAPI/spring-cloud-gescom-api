package com.mproduits.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Trace chaque correction manuelle de stock (ecran "Mise a jour du Stock") :
 * qui, quand, ancienne/nouvelle quantite et motif obligatoire. Avant cette
 * entite, InventairesService.saveStock ecrasait directement stockFinalTheorie
 * sans aucune trace - impossible de savoir apres coup qui avait corrige quoi
 * ni pourquoi (voir tache #61).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historique_correction_stock")
public class HistoriqueCorrectionStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "utilisateur")
    private String utilisateur;

    @Column(name = "date_correction")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCorrection;
}
