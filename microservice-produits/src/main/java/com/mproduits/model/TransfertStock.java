/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transfert_stock")
@NamedQueries({
    @NamedQuery(name = "TransfertStock.findAll", query = "SELECT t FROM TransfertStock t ORDER BY t.dateTransfert DESC"),
    @NamedQuery(name = "TransfertStock.findById", query = "SELECT t FROM TransfertStock t WHERE t.id = :id"),
    @NamedQuery(name = "TransfertStock.findBySource", query = "SELECT t FROM TransfertStock t WHERE t.source.id = :sourceId ORDER BY t.dateTransfert DESC"),
    @NamedQuery(name = "TransfertStock.findByDestination", query = "SELECT t FROM TransfertStock t WHERE t.destination.id = :destinationId ORDER BY t.dateTransfert DESC"),
    @NamedQuery(name = "TransfertStock.findByProduit", query = "SELECT t FROM TransfertStock t WHERE t.produit.id = :produitId ORDER BY t.dateTransfert DESC"),
    @NamedQuery(name = "TransfertStock.findByDateRange", query = "SELECT t FROM TransfertStock t WHERE t.dateTransfert BETWEEN :dateDebut AND :dateFin ORDER BY t.dateTransfert DESC")
})
public class TransfertStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    /**
     * Magasin/Point de vente source (d'où le stock est prélevé)
     */
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Magasin source;

    /**
     * Magasin/Point de vente destination (où le stock est ajouté)
     */
    @JoinColumn(name = "destination_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Magasin destination;

    /**
     * Produit transféré
     */
    @JoinColumn(name = "produit_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Produit produit;

    /**
     * Quantité transférée (nombre d'unités)
     */
    @Column(name = "quantite")
    private BigDecimal quantite;

    /**
     * Date et heure du transfert
     */
    @Column(name = "date_transfert")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTransfert;

    /**
     * Valeur estimative du transfert
     * = quantite * prixAchat (pour magasins)
     * = quantite * prixVenteTTC (pour points de vente)
     */
    @Column(name = "valeur_estimation")
    private BigDecimal valeurEstimation;

    /**
     * Utilisateur ayant effectué le transfert
     */
    @Column(name = "utilisateur")
    private String utilisateur;

    /**
     * Notes optionnelles sur le transfert
     */
    @Column(name = "notes")
    private String notes;

    /**
     * Entreprise associée
     */
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "Employeurid", referencedColumnName = "compagnie_id")
    })
    @ManyToOne(optional = false)
    private Entreprise entreprise;

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TransfertStock)) {
            return false;
        }
        TransfertStock other = (TransfertStock) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TransfertStock[ id=" + id + ", quantite=" + quantite + ", dateTransfert=" + dateTransfert + " ]";
    }
    
}
