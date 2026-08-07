/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "entreprise")
@NamedQueries({
    @NamedQuery(name = "Entreprise.findAll", query = "SELECT e FROM Entreprise e"),
    @NamedQuery(name = "Entreprise.findByAnneeId", query = "SELECT e FROM Entreprise e WHERE e.entreprisePK.anneeId = :anneeId"),
    @NamedQuery(name = "Entreprise.findByCompagnieId", query = "SELECT e FROM Entreprise e WHERE e.entreprisePK.compagnieId = :compagnieId"),
    @NamedQuery(name = "Entreprise.findByActif", query = "SELECT e FROM Entreprise e WHERE e.actif = :actif"),
    @NamedQuery(name = "Entreprise.findByActivite", query = "SELECT e FROM Entreprise e WHERE e.activite = :activite"),
    @NamedQuery(name = "Entreprise.findByConventionCollective", query = "SELECT e FROM Entreprise e WHERE e.conventionCollective = :conventionCollective"),
    @NamedQuery(name = "Entreprise.findByDateCreation", query = "SELECT e FROM Entreprise e WHERE e.dateCreation = :dateCreation"),
    @NamedQuery(name = "Entreprise.findByDirecteur", query = "SELECT e FROM Entreprise e WHERE e.directeur = :directeur"),
    @NamedQuery(name = "Entreprise.findBySiteWeb", query = "SELECT e FROM Entreprise e WHERE e.siteWeb = :siteWeb"),
    @NamedQuery(name = "Entreprise.findByTypeResponsable", query = "SELECT e FROM Entreprise e WHERE e.typeResponsable = :typeResponsable"),
    @NamedQuery(name = "Entreprise.findByDateFinLicense", query = "SELECT e FROM Entreprise e WHERE e.dateFinLicense = :dateFinLicense"),
    //@NamedQuery(name = "Entreprise.findByAnneeId1", query = "SELECT e FROM Entreprise e WHERE e.anneeId1 = :anneeId1"),
    //@NamedQuery(name = "Entreprise.findByEmployeurId1", query = "SELECT e FROM Entreprise e WHERE e.employeurId1 = :employeurId1"),
   // @NamedQuery(name = "Entreprise.findByConventionCollective1", query = "SELECT e FROM Entreprise e WHERE e.conventionCollective1 = :conventionCollective1"),
   // @NamedQuery(name = "Entreprise.findByDateCreation1", query = "SELECT e FROM Entreprise e WHERE e.dateCreation1 = :dateCreation1"),
    //@NamedQuery(name = "Entreprise.findByDateFinLicense1", query = "SELECT e FROM Entreprise e WHERE e.dateFinLicense1 = :dateFinLicense1"),
    //@NamedQuery(name = "Entreprise.findBySiteWeb1", query = "SELECT e FROM Entreprise e WHERE e.siteWeb1 = :siteWeb1"),
   // @NamedQuery(name = "Entreprise.findByTypeResponsable1", query = "SELECT e FROM Entreprise e WHERE e.typeResponsable1 = :typeResponsable1")
})
public class Entreprise implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entreprise")
    private Collection<Taxeproduit> taxproduitCollection;

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected EntreprisePK entreprisePK;
    @Column(name = "Actif")
    private Boolean actif;
    @Column(name = "activite")
    private String activite;
    @Column(name = "convention_collective")
    private String conventionCollective;
    @Column(name = "Date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @Column(name = "directeur")
    private String directeur;
    @Column(name = "Site_web")
    private String siteWeb;
    @Column(name = "Type_responsable")
    private String typeResponsable;
    @Column(name = "date_fin_license")
    @Temporal(TemporalType.DATE)
    private Date dateFinLicense;
    @Basic(optional = false)
  
 
    
   
   
    @JoinColumn(name = "Anneeid", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Annee annee;
   

    public Entreprise() {
    }

    public Entreprise(EntreprisePK entreprisePK) {
        this.entreprisePK = entreprisePK;
    }

   
    

    public EntreprisePK getEntreprisePK() {
        return entreprisePK;
    }

    public void setEntreprisePK(EntreprisePK entreprisePK) {
        this.entreprisePK = entreprisePK;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getActivite() {
        return activite;
    }

    public void setActivite(String activite) {
        this.activite = activite;
    }

    public String getConventionCollective() {
        return conventionCollective;
    }

    public void setConventionCollective(String conventionCollective) {
        this.conventionCollective = conventionCollective;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDirecteur() {
        return directeur;
    }

    public void setDirecteur(String directeur) {
        this.directeur = directeur;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getTypeResponsable() {
        return typeResponsable;
    }

    public void setTypeResponsable(String typeResponsable) {
        this.typeResponsable = typeResponsable;
    }

    public Date getDateFinLicense() {
        return dateFinLicense;
    }

    public void setDateFinLicense(Date dateFinLicense) {
        this.dateFinLicense = dateFinLicense;
    }

   

   

    

   
  
    public Annee getAnnee() {
        return annee;
    }

    public void setAnnee(Annee annee) {
        this.annee = annee;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (entreprisePK != null ? entreprisePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Entreprise)) {
            return false;
        }
        Entreprise other = (Entreprise) object;
        if ((this.entreprisePK == null && other.entreprisePK != null) || (this.entreprisePK != null && !this.entreprisePK.equals(other.entreprisePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Entreprise[ entreprisePK=" + entreprisePK + " ]";
    }

    public Collection<Taxeproduit> getTaxproduitCollection() {
        return taxproduitCollection;
    }

    public void setTaxproduitCollection(Collection<Taxeproduit> taxproduitCollection) {
        this.taxproduitCollection = taxproduitCollection;
    }
    
}
