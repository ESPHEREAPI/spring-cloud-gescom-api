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
    @Column(name = "Employeurid")
    private long employeurId;

    public EntreprisePK() {
    }

    public EntreprisePK(int anneeId, long employeurId) {
        this.anneeId = anneeId;
        this.employeurId = employeurId;
    }

    public int getAnneeId() {
        return anneeId;
    }

    public void setAnneeId(int anneeId) {
        this.anneeId = anneeId;
    }

    public long getEmployeurId() {
        return employeurId;
    }

    public void setEmployeurId(long employeurId) {
        this.employeurId = employeurId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) anneeId;
        hash += (int) employeurId;
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
        if (this.employeurId != other.employeurId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.EntreprisePK[ anneeId=" + anneeId + ", employeurId=" + employeurId + " ]";
    }
    
}
