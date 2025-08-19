/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "quinzaine")
@NamedQueries({
    @NamedQuery(name = "Quinzaine.findAll", query = "SELECT q FROM Quinzaine q"),
    @NamedQuery(name = "Quinzaine.findById", query = "SELECT q FROM Quinzaine q WHERE q.id = :id"),
    @NamedQuery(name = "Quinzaine.findByDateDebut", query = "SELECT q FROM Quinzaine q WHERE q.dateDebut = :dateDebut"),
    @NamedQuery(name = "Quinzaine.findByDateFin", query = "SELECT q FROM Quinzaine q WHERE q.dateFin = :dateFin"),
    @NamedQuery(name = "Quinzaine.findByQuinzaineMois", query = "SELECT q FROM Quinzaine q WHERE q.quinzaineMois = :quinzaineMois")})
public class Quinzaine implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Date_Debut")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebut;
    @Column(name = "Date_Fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFin;
    @Column(name = "QuinzaineMois")
    private String quinzaineMois;
    @JoinColumn(name = "Moisid", referencedColumnName = "id")
    @ManyToOne
    private Mois mois;


    public Quinzaine() {
    }

    public Quinzaine(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getQuinzaineMois() {
        return quinzaineMois;
    }

    public void setQuinzaineMois(String quinzaineMois) {
        this.quinzaineMois = quinzaineMois;
    }

    public Mois getMois() {
        return mois;
    }

    public void setMois(Mois moisid) {
        this.mois = moisid;
    }

  

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Quinzaine)) {
            return false;
        }
        Quinzaine other = (Quinzaine) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Quinzaine[ id=" + id + " ]";
    }
    
}
