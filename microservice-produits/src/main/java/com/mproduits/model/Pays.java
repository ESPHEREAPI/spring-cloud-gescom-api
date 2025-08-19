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
@Table(name = "pays")
@NamedQueries({
    @NamedQuery(name = "Pays.findAll", query = "SELECT p FROM Pays p"),
    @NamedQuery(name = "Pays.findById", query = "SELECT p FROM Pays p WHERE p.id = :id"),
    @NamedQuery(name = "Pays.findByCode", query = "SELECT p FROM Pays p WHERE p.code = :code"),
    @NamedQuery(name = "Pays.findByContinent", query = "SELECT p FROM Pays p WHERE p.continent = :continent"),
    @NamedQuery(name = "Pays.findByLibelle", query = "SELECT p FROM Pays p WHERE p.libelle = :libelle"),
    @NamedQuery(name = "Pays.findByLibelleAnglais", query = "SELECT p FROM Pays p WHERE p.libelleAnglais = :libelleAnglais"),
    //@NamedQuery(name = "Pays.findByLibelleAnglais1", query = "SELECT p FROM Pays p WHERE p.libelleAnglais1 = :libelleAnglais1")
})
public class Pays implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "code")
    private String code;
    @Column(name = "continent")
    private String continent;
    @Column(name = "Libelle")
    private String libelle;
 
    @Column(name = "libelle_anglais")
    private String libelleAnglais;
    @OneToMany(mappedBy = "paysid")
    private Collection<Personne> personneCollection;

    public Pays() {
    }

    public Pays(Long id) {
        this.id = id;
    }

    public Pays(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelleAnglais() {
        return libelleAnglais;
    }

    public void setLibelleAnglais(String libelleAnglais) {
        this.libelleAnglais = libelleAnglais;
    }


    public Collection<Personne> getPersonneCollection() {
        return personneCollection;
    }

    public void setPersonneCollection(Collection<Personne> personneCollection) {
        this.personneCollection = personneCollection;
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
        if (!(object instanceof Pays)) {
            return false;
        }
        Pays other = (Pays) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Pays[ id=" + id + " ]";
    }
    
}
