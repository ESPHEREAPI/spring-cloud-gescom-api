/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "boutique")
@JsonIgnoreProperties({"magasinCollection", "inventaireCollection", "pointventeCollection"})
@NamedQueries({
    @NamedQuery(name = "Boutique.findAll", query = "SELECT b FROM Boutique b"),
    @NamedQuery(name = "Boutique.findById", query = "SELECT b FROM Boutique b WHERE b.id = :id"),
    @NamedQuery(name = "Boutique.findByDateCreation", query = "SELECT b FROM Boutique b WHERE b.dateCreation = :dateCreation"),
    @NamedQuery(name = "Boutique.findByCode", query = "SELECT b FROM Boutique b WHERE b.code = :code"),
    @NamedQuery(name = "Boutique.findByHeurFin", query = "SELECT b FROM Boutique b WHERE b.heurFin = :heurFin"),
    @NamedQuery(name = "Boutique.findByHeureDebut", query = "SELECT b FROM Boutique b WHERE b.heureDebut = :heureDebut"),
    @NamedQuery(name = "Boutique.findByNom", query = "SELECT b FROM Boutique b WHERE b.nom = :nom"),
    @NamedQuery(name = "Boutique.findByQuartier", query = "SELECT b FROM Boutique b WHERE b.quartier = :quartier")})
public class Boutique implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "dateCreation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @Column(name = "code")
    private String code;
    @Column(name = "heurFin")
    private String heurFin;
    @Column(name = "heureDebut")
    private String heureDebut;
    @Column(name = "nom")
    private String nom;
    @Column(name = "quartier")
    private String quartier;

    @JoinColumn(name = "Villeid", referencedColumnName = "id")
    @ManyToOne
    private Ville ville;
    

    public Boutique() {
    }

    public Boutique(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHeurFin() {
        return heurFin;
    }

    public void setHeurFin(String heurFin) {
        this.heurFin = heurFin;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    

    public Ville getVilleid() {
        return ville;
    }

    public void setVilleid(Ville villeid) {
        this.ville = villeid;
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
        if (!(object instanceof Boutique)) {
            return false;
        }
        Boutique other = (Boutique) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Boutique[ id=" + id + " ]";
    }

}
