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
public class EntreprisePK implements Serializable {

    @Basic(optional = false)
    @Column(name = "Anneeid")
    private int anneeId;
    @Basic(optional = false)
    @Column(name = "compagnie_id")
    private long compagnieId;

    public EntreprisePK() {
    }

    public EntreprisePK(int anneeId, long compagnieId) {
        this.anneeId = anneeId;
        this.compagnieId = compagnieId;
    }

    public int getAnneeId() {
        return anneeId;
    }

    public void setAnneeId(int anneeId) {
        this.anneeId = anneeId;
    }

    public long getCompagnieId() {
        return compagnieId;
    }

    public void setCompagnieId(long compagnieId) {
        this.compagnieId = compagnieId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) anneeId;
        hash += (int) compagnieId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EntreprisePK)) {
            return false;
        }
        EntreprisePK other = (EntreprisePK) object;
        if (this.anneeId != other.anneeId) {
            return false;
        }
        if (this.compagnieId != other.compagnieId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.EntreprisePK[ anneeId=" + anneeId + ", compagnieId=" + compagnieId + " ]";
    }
    
}
