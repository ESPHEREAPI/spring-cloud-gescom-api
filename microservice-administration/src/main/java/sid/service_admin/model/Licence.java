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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "licence")
@NamedQueries({
    @NamedQuery(name = "Licence.findAll", query = "SELECT l FROM Licence l"),
    @NamedQuery(name = "Licence.findById", query = "SELECT l FROM Licence l WHERE l.id = :id"),
    @NamedQuery(name = "Licence.findByLicenseType", query = "SELECT l FROM Licence l WHERE l.licenseType = :licenseType"),
    @NamedQuery(name = "Licence.findByDateCreation", query = "SELECT l FROM Licence l WHERE l.dateCreation = :dateCreation"),
    @NamedQuery(name = "Licence.findByDateFinLicense", query = "SELECT l FROM Licence l WHERE l.dateFinLicense = :dateFinLicense"),
    @NamedQuery(name = "Licence.findByDuree", query = "SELECT l FROM Licence l WHERE l.duree = :duree"),
    @NamedQuery(name = "Licence.findByLicenseNumber", query = "SELECT l FROM Licence l WHERE l.licenseNumber = :licenseNumber"),
    @NamedQuery(name = "Licence.findByAdresseMac", query = "SELECT l FROM Licence l WHERE l.adresseMac = :adresseMac"),
    @NamedQuery(name = "Licence.findByVersion", query = "SELECT l FROM Licence l WHERE l.version = :version")})
public class Licence implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "LicenseType")
    private String licenseType;
    @Column(name = "dateCreation")
    @Temporal(TemporalType.DATE)
    private Date dateCreation;
    @Column(name = "dateFinLicense")
    @Temporal(TemporalType.DATE)
    private Date dateFinLicense;
    @Column(name = "duree")
    private Integer duree;
    @Column(name = "licenseNumber")
    private String licenseNumber;
    @Column(name = "adresseMac")
    private String adresseMac;
    @Column(name = "version")
    private String version;

    public Licence() {
    }

    public Licence(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateFinLicense() {
        return dateFinLicense;
    }

    public void setDateFinLicense(Date dateFinLicense) {
        this.dateFinLicense = dateFinLicense;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAdresseMac() {
        return adresseMac;
    }

    public void setAdresseMac(String adresseMac) {
        this.adresseMac = adresseMac;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
        if (!(object instanceof Licence)) {
            return false;
        }
        Licence other = (Licence) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Licence[ id=" + id + " ]";
    }
    
}
