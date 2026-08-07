/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "magasin_fournisseur")
@JsonIgnoreProperties({"entreprise", "MagasinFournisseurPK"})
@NamedQueries({
    @NamedQuery(name = "MagasinFournisseur.findAll", query = "SELECT m FROM MagasinFournisseur m"),
    @NamedQuery(name = "MagasinFournisseur.findByDepotId", query = "SELECT m FROM MagasinFournisseur m WHERE m.magasinFournisseurPK.depotId = :depotId"),
    @NamedQuery(name = "MagasinFournisseur.findByFournisseurId", query = "SELECT m FROM MagasinFournisseur m WHERE m.magasinFournisseurPK.fournisseurId = :fournisseurId")})
public class MagasinFournisseur implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MagasinFournisseurPK magasinFournisseurPK;
   @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "Employeurid", referencedColumnName = "compagnie_id")})
    @ManyToOne(optional = true, fetch = FetchType.LAZY)  // ← LAZY
    private Entreprise entreprise;
    
    @JoinColumn(name = "fournisseurid", referencedColumnName = "id", 
                insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)  // ← LAZY
    private Fournisseur fournisseur;
    
    @JoinColumn(name = "Depotid", referencedColumnName = "id", 
                insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)  // ← LAZY
    private Magasin magasin;
   

    public MagasinFournisseur() {
    }

    public MagasinFournisseur(MagasinFournisseurPK magasinFournisseurPK) {
        this.magasinFournisseurPK = magasinFournisseurPK;
    }

    public MagasinFournisseur(long depotId, long fournisseurId) {
        this.magasinFournisseurPK = new MagasinFournisseurPK(depotId, fournisseurId);
    }

    public MagasinFournisseurPK getMagasinFournisseurPK() {
        return magasinFournisseurPK;
    }

    public void setMagasinFournisseurPK(MagasinFournisseurPK magasinFournisseurPK) {
        this.magasinFournisseurPK = magasinFournisseurPK;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public Magasin getMagasin() {
        return magasin;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }

 

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (magasinFournisseurPK != null ? magasinFournisseurPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MagasinFournisseur)) {
            return false;
        }
        MagasinFournisseur other = (MagasinFournisseur) object;
        if ((this.magasinFournisseurPK == null && other.magasinFournisseurPK != null) || (this.magasinFournisseurPK != null && !this.magasinFournisseurPK.equals(other.magasinFournisseurPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.MagasinFournisseur[ magasinFournisseurPK=" + magasinFournisseurPK + " ]";
    }
    
}
