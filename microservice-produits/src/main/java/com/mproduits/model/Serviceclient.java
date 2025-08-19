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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "serviceclient")
@NamedQueries({
    @NamedQuery(name = "Serviceclient.findAll", query = "SELECT s FROM Serviceclient s"),
    @NamedQuery(name = "Serviceclient.findById", query = "SELECT s FROM Serviceclient s WHERE s.id = :id"),
    @NamedQuery(name = "Serviceclient.findByCodePoste", query = "SELECT s FROM Serviceclient s WHERE s.codePoste = :codePoste"),
    @NamedQuery(name = "Serviceclient.findByBp", query = "SELECT s FROM Serviceclient s WHERE s.bp = :bp"),
    @NamedQuery(name = "Serviceclient.findByEmail", query = "SELECT s FROM Serviceclient s WHERE s.email = :email"),
    @NamedQuery(name = "Serviceclient.findByFax", query = "SELECT s FROM Serviceclient s WHERE s.fax = :fax"),
    @NamedQuery(name = "Serviceclient.findByIndicatifPays", query = "SELECT s FROM Serviceclient s WHERE s.indicatifPays = :indicatifPays"),
    @NamedQuery(name = "Serviceclient.findByQuartier", query = "SELECT s FROM Serviceclient s WHERE s.quartier = :quartier"),
    @NamedQuery(name = "Serviceclient.findByRegion", query = "SELECT s FROM Serviceclient s WHERE s.region = :region"),
    @NamedQuery(name = "Serviceclient.findByTel", query = "SELECT s FROM Serviceclient s WHERE s.tel = :tel"),
    @NamedQuery(name = "Serviceclient.findByVille", query = "SELECT s FROM Serviceclient s WHERE s.ville = :ville"),
    @NamedQuery(name = "Serviceclient.findByReglement", query = "SELECT s FROM Serviceclient s WHERE s.reglement = :reglement"),
    @NamedQuery(name = "Serviceclient.findByNom", query = "SELECT s FROM Serviceclient s WHERE s.nom = :nom"),
    @NamedQuery(name = "Serviceclient.findByTaux", query = "SELECT s FROM Serviceclient s WHERE s.taux = :taux")})
public class Serviceclient implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "CodePoste")
    private String codePoste;
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
    @Column(name = "Reglement")
    private String reglement;
    @Column(name = "nom")
    private String nom;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "taux")
    private BigDecimal taux;
    @OneToMany(mappedBy = "serviceid")
    private Collection<Client> clientCollection;
    @JoinColumn(name = "Villeid", referencedColumnName = "id")
    @ManyToOne
    private Ville villeid;

    public Serviceclient() {
    }

    public Serviceclient(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePoste() {
        return codePoste;
    }

    public void setCodePoste(String codePoste) {
        this.codePoste = codePoste;
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

    public String getReglement() {
        return reglement;
    }

    public void setReglement(String reglement) {
        this.reglement = reglement;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public Collection<Client> getClientCollection() {
        return clientCollection;
    }

    public void setClientCollection(Collection<Client> clientCollection) {
        this.clientCollection = clientCollection;
    }

    public Ville getVilleid() {
        return villeid;
    }

    public void setVilleid(Ville villeid) {
        this.villeid = villeid;
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
        if (!(object instanceof Serviceclient)) {
            return false;
        }
        Serviceclient other = (Serviceclient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Serviceclient[ id=" + id + " ]";
    }
    
}
