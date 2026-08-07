/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "fournisseur")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler","magasinFournisseurCollection","factureCollection"})
@NamedQueries({
    @NamedQuery(name = "Fournisseur.findAll", query = "SELECT f FROM Fournisseur f"),
    @NamedQuery(name = "Fournisseur.findById", query = "SELECT f FROM Fournisseur f WHERE f.id = :id"),
    @NamedQuery(name = "Fournisseur.findByBp", query = "SELECT f FROM Fournisseur f WHERE f.bp = :bp"),
    @NamedQuery(name = "Fournisseur.findByEmail", query = "SELECT f FROM Fournisseur f WHERE f.email = :email"),
    @NamedQuery(name = "Fournisseur.findByFax", query = "SELECT f FROM Fournisseur f WHERE f.fax = :fax"),
    @NamedQuery(name = "Fournisseur.findByIndicatifPays", query = "SELECT f FROM Fournisseur f WHERE f.indicatifPays = :indicatifPays"),
    @NamedQuery(name = "Fournisseur.findByQuartier", query = "SELECT f FROM Fournisseur f WHERE f.quartier = :quartier"),
    @NamedQuery(name = "Fournisseur.findByRegion", query = "SELECT f FROM Fournisseur f WHERE f.region = :region"),
    @NamedQuery(name = "Fournisseur.findByTel", query = "SELECT f FROM Fournisseur f WHERE f.tel = :tel"),
    @NamedQuery(name = "Fournisseur.findByVille", query = "SELECT f FROM Fournisseur f WHERE f.ville = :ville"),
    @NamedQuery(name = "Fournisseur.findByCode", query = "SELECT f FROM Fournisseur f WHERE f.code = :code"),
    @NamedQuery(name = "Fournisseur.findByCodeSociete", query = "SELECT f FROM Fournisseur f WHERE f.codeSociete = :codeSociete"),
    @NamedQuery(name = "Fournisseur.findByNom", query = "SELECT f FROM Fournisseur f WHERE f.nom = :nom")})
public class Fournisseur implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Bp")
    private String bp;
    @Column(name = "Email")
    private String email;
    @Column(name = "Fax")
    private String fax;
    @Column(name = "indicatifPays")
    private String indicatifPays;
    @Column(name = "Quartier")
    private String quartier;
    @Column(name = "Region")
    private String region;
    @Column(name = "Tel")
    private String tel;
    @Column(name = "Ville")
    private String ville;
    @Column(name = "code")
    private String code;
    @Column(name = "codeSociete")
    private String codeSociete;
    @Column(name = "nom")
    private String nom;
   
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fournisseur")
    private Collection<MagasinFournisseur> magasinFournisseurCollection;

    // Rattachement compagnie (isolation multi-tenant) - un fournisseur est
    // propre a chaque compagnie.
    @JsonIgnore
    @JoinColumn(name = "compagnie_id")
    @ManyToOne
    private Compagnie compagnie;

    public Fournisseur() {
    }

    public Fournisseur(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getIndicatifPays() {
        return indicatifPays;
    }

    public void setIndicatifPays(String indicatifPays) {
        this.indicatifPays = indicatifPays;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSociete() {
        return codeSociete;
    }

    public void setCodeSociete(String codeSociete) {
        this.codeSociete = codeSociete;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public Collection<MagasinFournisseur> getMagasinFournisseurCollection() {
        return magasinFournisseurCollection;
    }

    public void setMagasinFournisseurCollection(Collection<MagasinFournisseur> magasinFournisseurCollection) {
        this.magasinFournisseurCollection = magasinFournisseurCollection;
    }

    public Compagnie getCompagnie() {
        return compagnie;
    }

    public void setCompagnie(Compagnie compagnie) {
        this.compagnie = compagnie;
    }

    public Long getCompagnieId() {
        return compagnie != null ? compagnie.getId() : null;
    }

    public void setCompagnieId(Long compagnieId) {
        this.compagnie = compagnieId != null ? new Compagnie(compagnieId) : null;
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
        if (!(object instanceof Fournisseur)) {
            return false;
        }
        Fournisseur other = (Fournisseur) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Fournisseur[ id=" + id + " ]";
    }
    
}
