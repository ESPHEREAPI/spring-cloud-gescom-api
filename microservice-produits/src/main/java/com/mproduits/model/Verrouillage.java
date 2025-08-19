/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "verrouillage")
@NamedQueries({
    @NamedQuery(name = "Verrouillage.findAll", query = "SELECT v FROM Verrouillage v"),
    @NamedQuery(name = "Verrouillage.findById", query = "SELECT v FROM Verrouillage v WHERE v.id = :id"),
    @NamedQuery(name = "Verrouillage.findByDateFin", query = "SELECT v FROM Verrouillage v WHERE v.dateFin = :dateFin"),
    @NamedQuery(name = "Verrouillage.findByDatedebut", query = "SELECT v FROM Verrouillage v WHERE v.datedebut = :datedebut"),
    @NamedQuery(name = "Verrouillage.findByMontant", query = "SELECT v FROM Verrouillage v WHERE v.montant = :montant"),
    @NamedQuery(name = "Verrouillage.findByTaux", query = "SELECT v FROM Verrouillage v WHERE v.taux = :taux"),
    @NamedQuery(name = "Verrouillage.findByTypeVerrou", query = "SELECT v FROM Verrouillage v WHERE v.typeVerrou = :typeVerrou"),
    @NamedQuery(name = "Verrouillage.findByVerrouAuto", query = "SELECT v FROM Verrouillage v WHERE v.verrouAuto = :verrouAuto"),
    @NamedQuery(name = "Verrouillage.findByVerrouManuel", query = "SELECT v FROM Verrouillage v WHERE v.verrouManuel = :verrouManuel")})
public class Verrouillage implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "dateFin")
    @Temporal(TemporalType.DATE)
    private Date dateFin;
    @Column(name = "datedebut")
    @Temporal(TemporalType.DATE)
    private Date datedebut;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "taux")
    private BigDecimal taux;
    @Column(name = "typeVerrou")
    private String typeVerrou;
    @Column(name = "verrouAuto")
    private Boolean verrouAuto;
    @Column(name = "verrouManuel")
    private Boolean verrouManuel;
    @JoinColumns({
        @JoinColumn(name = "AnneeId", referencedColumnName = "AnneeId"),
        @JoinColumn(name = "EmployeurId", referencedColumnName = "EmployeurId")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;

    public Verrouillage() {
    }

    public Verrouillage(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public Date getDatedebut() {
        return datedebut;
    }

    public void setDatedebut(Date datedebut) {
        this.datedebut = datedebut;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public String getTypeVerrou() {
        return typeVerrou;
    }

    public void setTypeVerrou(String typeVerrou) {
        this.typeVerrou = typeVerrou;
    }

    public Boolean getVerrouAuto() {
        return verrouAuto;
    }

    public void setVerrouAuto(Boolean verrouAuto) {
        this.verrouAuto = verrouAuto;
    }

    public Boolean getVerrouManuel() {
        return verrouManuel;
    }

    public void setVerrouManuel(Boolean verrouManuel) {
        this.verrouManuel = verrouManuel;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
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
        if (!(object instanceof Verrouillage)) {
            return false;
        }
        Verrouillage other = (Verrouillage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Verrouillage[ id=" + id + " ]";
    }
    
}
