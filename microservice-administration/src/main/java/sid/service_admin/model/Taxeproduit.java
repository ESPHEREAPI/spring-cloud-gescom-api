/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

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
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "taxeproduits")
@NamedQueries({
    @NamedQuery(name = "Taxeproduit.findAll", query = "SELECT t FROM Taxeproduit t"),
    @NamedQuery(name = "Taxeproduit.findById", query = "SELECT t FROM Taxeproduit t WHERE t.id = :id"),
    @NamedQuery(name = "Taxeproduit.findByRedevenceportiere", query = "SELECT t FROM Taxeproduit t WHERE t.redevenceportiere = :redevenceportiere"),
    @NamedQuery(name = "Taxeproduit.findByPassageDepot", query = "SELECT t FROM Taxeproduit t WHERE t.passageDepot = :passageDepot"),
    @NamedQuery(name = "Taxeproduit.findByPerequation", query = "SELECT t FROM Taxeproduit t WHERE t.perequation = :perequation"),
    @NamedQuery(name = "Taxeproduit.findByTspp", query = "SELECT t FROM Taxeproduit t WHERE t.tspp = :tspp"),
    @NamedQuery(name = "Taxeproduit.findByTva", query = "SELECT t FROM Taxeproduit t WHERE t.tva = :tva")})
public class Taxeproduit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Redevence_portiere")
    private BigDecimal redevenceportiere;
    @Column(name = "passage_Depot")
    private BigDecimal passageDepot;
    @Column(name = "perequation")
    private BigDecimal perequation;
    @Column(name = "tspp")
    private BigDecimal tspp;
    @Column(name = "tva")
    private BigDecimal tva;
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "compagnie_id", referencedColumnName = "compagnie_id")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;
    @JoinColumn(name = "Moisid", referencedColumnName = "id")
    @ManyToOne
    private Mois mois;
   
    public Taxeproduit() {
    }

    public Taxeproduit(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRedevenceportiere() {
        return redevenceportiere;
    }

    public void setRedevenceportiere(BigDecimal redevenceportiere) {
        this.redevenceportiere = redevenceportiere;
    }

    public BigDecimal getPassageDepot() {
        return passageDepot;
    }

    public void setPassageDepot(BigDecimal passageDepot) {
        this.passageDepot = passageDepot;
    }

    public BigDecimal getPerequation() {
        return perequation;
    }

    public void setPerequation(BigDecimal perequation) {
        this.perequation = perequation;
    }

    public BigDecimal getTspp() {
        return tspp;
    }

    public void setTspp(BigDecimal tspp) {
        this.tspp = tspp;
    }

    public BigDecimal getTva() {
        return tva;
    }

    public void setTva(BigDecimal tva) {
        this.tva = tva;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public Mois getMoisid() {
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
        if (!(object instanceof Taxeproduit)) {
            return false;
        }
        Taxeproduit other = (Taxeproduit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Taxeproduit[ id=" + id + " ]";
    }
    
}
