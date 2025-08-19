/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 *
 * @author USER01
 */
@Embeddable
public class MagasinFournisseurPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "Depotid")
    private long depotId;
    @Basic(optional = false)
    @Column(name = "fournisseurid")
    private long fournisseurId;

    public MagasinFournisseurPK() {
    }

    public MagasinFournisseurPK(long depotId, long fournisseurId) {
        this.depotId = depotId;
        this.fournisseurId = fournisseurId;
    }

    public long getDepotId() {
        return depotId;
    }

    public void setDepotId(long depotId) {
        this.depotId = depotId;
    }

    public long getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) depotId;
        hash += (int) fournisseurId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MagasinFournisseurPK)) {
            return false;
        }
        MagasinFournisseurPK other = (MagasinFournisseurPK) object;
        if (this.depotId != other.depotId) {
            return false;
        }
        if (this.fournisseurId != other.fournisseurId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.MagasinFournisseurPK[ depotId=" + depotId + ", fournisseurId=" + fournisseurId + " ]";
    }
    
}
