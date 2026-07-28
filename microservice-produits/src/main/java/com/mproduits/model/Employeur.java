/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "employeur")
@NamedQueries({
    @NamedQuery(name = "Employeur.findAll", query = "SELECT e FROM Employeur e"),
    @NamedQuery(name = "Employeur.findByAbreviation", query = "SELECT e FROM Employeur e WHERE e.abreviation = :abreviation"),
    @NamedQuery(name = "Employeur.findByCodiicationActivite", query = "SELECT e FROM Employeur e WHERE e.codiicationActivite = :codiicationActivite"),
    @NamedQuery(name = "Employeur.findByCheminLogo", query = "SELECT e FROM Employeur e WHERE e.cheminLogo = :cheminLogo"),
    @NamedQuery(name = "Employeur.findByNumeroContribuable", query = "SELECT e FROM Employeur e WHERE e.numeroContribuable = :numeroContribuable"),
    @NamedQuery(name = "Employeur.findBySociete", query = "SELECT e FROM Employeur e WHERE e.societe = :societe"),
    @NamedQuery(name = "Employeur.findByNumeroCaisseCotissation", query = "SELECT e FROM Employeur e WHERE e.numeroCaisseCotissation = :numeroCaisseCotissation"),
    @NamedQuery(name = "Employeur.findById", query = "SELECT e FROM Employeur e WHERE e.id = :id"), //@NamedQuery(name = "Employeur.findByCheminLogo1", query = "SELECT e FROM Employeur e WHERE e.cheminLogo1 = :cheminLogo1")
})
public class Employeur implements Serializable {

    @JsonIgnore
    @Lob
    @Column(name = "Logos", columnDefinition = "MEDIUMBLOB")
    private byte[] logos;

    private static final long serialVersionUID = 1L;
    @Column(name = "abreviation")
    private String abreviation;
    @Column(name = "codiication_activite")
    private String codiicationActivite;
    @Column(name = "chemin_logo")
    private String cheminLogo;
    @Column(name = "numero_contribuable")
    private String numeroContribuable;
    @Column(name = "societe")
    private String societe;
    @Column(name = "numero_caisse_cotissation")
    private String numeroCaisseCotissation;
    @Column(name = "numero_iddentifiant_unique")
    private String nui;
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
  @JsonIgnore
    @JoinColumn(name = "Id", referencedColumnName = "Id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Personne personne;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "employeur")
    private Collection<Entreprise> entrepriseCollection;

    public Employeur() {
    }

    public Employeur(Long id) {
        this.id = id;
    }

    public String getAbreviation() {
        return abreviation;
    }

    public void setAbreviation(String abreviation) {
        this.abreviation = abreviation;
    }

    public String getCodiicationActivite() {
        return codiicationActivite;
    }

    public void setCodiicationActivite(String codiicationActivite) {
        this.codiicationActivite = codiicationActivite;
    }

    public String getCheminLogo() {
        return cheminLogo;
    }

    public void setCheminLogo(String cheminLogo) {
        this.cheminLogo = cheminLogo;
    }

    public String getNumeroContribuable() {
        return numeroContribuable;
    }

    public void setNumeroContribuable(String numeroContribuable) {
        this.numeroContribuable = numeroContribuable;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public String getNumeroCaisseCotissation() {
        return numeroCaisseCotissation;
    }

    public void setNumeroCaisseCotissation(String numeroCaisseCotissation) {
        this.numeroCaisseCotissation = numeroCaisseCotissation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public Collection<Entreprise> getEntrepriseCollection() {
        return entrepriseCollection;
    }

    public void setEntrepriseCollection(Collection<Entreprise> entrepriseCollection) {
        this.entrepriseCollection = entrepriseCollection;
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
        if (!(object instanceof Employeur)) {
            return false;
        }
        Employeur other = (Employeur) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Employeur[ id=" + id + " ]";
    }

    public byte[] getLogos() {
        return logos;
    }

    public void setLogos(byte[] logos) {
        this.logos = logos;
    }

    public String getNui() {
        return nui;
    }

    public void setNui(String nui) {
        this.nui = nui;
    }

}
