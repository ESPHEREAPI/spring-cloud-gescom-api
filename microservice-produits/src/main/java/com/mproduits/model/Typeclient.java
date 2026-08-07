/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "typeclient")
@NamedQueries({
    @NamedQuery(name = "Typeclient.findAll", query = "SELECT t FROM Typeclient t"),
    @NamedQuery(name = "Typeclient.findById", query = "SELECT t FROM Typeclient t WHERE t.id = :id"),
    @NamedQuery(name = "Typeclient.findByCategorieClient", query = "SELECT t FROM Typeclient t WHERE t.categorieClient = :categorieClient"),
    @NamedQuery(name = "Typeclient.findByCode", query = "SELECT t FROM Typeclient t WHERE t.code = :code"),
    @NamedQuery(name = "Typeclient.findByLibelle", query = "SELECT t FROM Typeclient t WHERE t.libelle = :libelle")})
public class Typeclient implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "categorieClient")
    private String categorieClient;
    @Column(name = "Code")
    private String code;
    @Column(name = "libelle")
    private String libelle;
    @JsonIgnore
    @OneToMany(mappedBy = "typeClientid")
    private Collection<Client> clientCollection;

    // Rattachement compagnie (isolation multi-tenant) - une liste de types de
    // client est propre a chaque compagnie.
    @JsonIgnore
    @JoinColumn(name = "compagnie_id")
    @ManyToOne
    private Compagnie compagnie;

    public Typeclient() {
    }

    public Typeclient(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategorieClient() {
        return categorieClient;
    }

    public void setCategorieClient(String categorieClient) {
        this.categorieClient = categorieClient;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Collection<Client> getClientCollection() {
        return clientCollection;
    }

    public void setClientCollection(Collection<Client> clientCollection) {
        this.clientCollection = clientCollection;
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
        if (!(object instanceof Typeclient)) {
            return false;
        }
        Typeclient other = (Typeclient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Typeclient[ id=" + id + " ]";
    }
    
}
