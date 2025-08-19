/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "annee")
@NamedQueries({
    @NamedQuery(name = "Annee.findAll", query = "SELECT a FROM Annee a"),
    @NamedQuery(name = "Annee.findById", query = "SELECT a FROM Annee a WHERE a.id = :id"),
    @NamedQuery(name = "Annee.findByCode", query = "SELECT a FROM Annee a WHERE a.code = :code"),
    @NamedQuery(name = "Annee.findByLibelle", query = "SELECT a FROM Annee a WHERE a.libelle = :libelle")})
public class Annee implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "Code")
    private String code;
    @Column(name = "Libelle")
    private String libelle;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "annee")
    private Collection<Entreprise> entrepriseCollection;
    @OneToMany(mappedBy = "annee")
    private Collection<Mois> moisCollection;

    public Annee() {
    }

    public Annee(Integer id) {
        this.id = id;
    }

    public Annee(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Collection<Entreprise> getEntrepriseCollection() {
        return entrepriseCollection;
    }

    public void setEntrepriseCollection(Collection<Entreprise> entrepriseCollection) {
        this.entrepriseCollection = entrepriseCollection;
    }

    public Collection<Mois> getMoisCollection() {
        return moisCollection;
    }

    public void setMoisCollection(Collection<Mois> moisCollection) {
        this.moisCollection = moisCollection;
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
        if (!(object instanceof Annee)) {
            return false;
        }
        Annee other = (Annee) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Annee[ id=" + id + " ]";
    }
    
}
