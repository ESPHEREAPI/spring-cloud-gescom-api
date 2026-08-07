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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "profil")
@NamedQueries({
    @NamedQuery(name = "Profil.findAll", query = "SELECT p FROM Profil p"),
    @NamedQuery(name = "Profil.findById", query = "SELECT p FROM Profil p WHERE p.id = :id"),
    @NamedQuery(name = "Profil.findByAddDate", query = "SELECT p FROM Profil p WHERE p.addDate = :addDate"),
    @NamedQuery(name = "Profil.findByCode", query = "SELECT p FROM Profil p WHERE p.code = :code"),
    @NamedQuery(name = "Profil.findByDescription", query = "SELECT p FROM Profil p WHERE p.description = :description"),
    @NamedQuery(name = "Profil.findByLastDateModif", query = "SELECT p FROM Profil p WHERE p.lastDateModif = :lastDateModif"),
    @NamedQuery(name = "Profil.findByLastUserModif", query = "SELECT p FROM Profil p WHERE p.lastUserModif = :lastUserModif"),
    @NamedQuery(name = "Profil.findByLockDuration", query = "SELECT p FROM Profil p WHERE p.lockDuration = :lockDuration"),
    @NamedQuery(name = "Profil.findByNberEchecCnx", query = "SELECT p FROM Profil p WHERE p.nberEchecCnx = :nberEchecCnx"),
    @NamedQuery(name = "Profil.findByPwdDuration", query = "SELECT p FROM Profil p WHERE p.pwdDuration = :pwdDuration"),
    @NamedQuery(name = "Profil.findByStatut", query = "SELECT p FROM Profil p WHERE p.statut = :statut"),
    @NamedQuery(name = "Profil.findByUserAdd", query = "SELECT p FROM Profil p WHERE p.userAdd = :userAdd"),
    @NamedQuery(name = "Profil.findByUserdelete", query = "SELECT p FROM Profil p WHERE p.userdelete = :userdelete")})
public class Profil implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Column(name = "addDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addDate;
    @Basic(optional = false)
    @Column(name = "Code")
    private String code;
    @Column(name = "description")
    private String description;
    @Column(name = "lastDateModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDateModif;
    @Column(name = "lastUserModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUserModif;
    @Column(name = "lockDuration")
    private Integer lockDuration;
    @Column(name = "nberEchecCnx")
    private Integer nberEchecCnx;
    @Column(name = "pwdDuration")
    private Integer pwdDuration;
    @Column(name = "statut")
    private String statut;
    @Column(name = "userAdd")
    private String userAdd;
    @Column(name = "userdelete")
    private String userdelete;
    @OneToMany(mappedBy = "profilid")
    private List<Personne> personneList;
    @JoinColumn(name = "compagnie_id")
    @ManyToOne
    private Compagnie compagnie;

    public Profil() {
    }

  
    public Profil( String code) {
       
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastDateModif() {
        return lastDateModif;
    }

    public void setLastDateModif(Date lastDateModif) {
        this.lastDateModif = lastDateModif;
    }

    public Date getLastUserModif() {
        return lastUserModif;
    }

    public void setLastUserModif(Date lastUserModif) {
        this.lastUserModif = lastUserModif;
    }

    public Integer getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(Integer lockDuration) {
        this.lockDuration = lockDuration;
    }

    public Integer getNberEchecCnx() {
        return nberEchecCnx;
    }

    public void setNberEchecCnx(Integer nberEchecCnx) {
        this.nberEchecCnx = nberEchecCnx;
    }

    public Integer getPwdDuration() {
        return pwdDuration;
    }

    public void setPwdDuration(Integer pwdDuration) {
        this.pwdDuration = pwdDuration;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getUserAdd() {
        return userAdd;
    }

    public void setUserAdd(String userAdd) {
        this.userAdd = userAdd;
    }

    public String getUserdelete() {
        return userdelete;
    }

    public void setUserdelete(String userdelete) {
        this.userdelete = userdelete;
    }

    public List<Personne> getPersonneList() {
        return personneList;
    }

    public void setPersonneList(List<Personne> personneList) {
        this.personneList = personneList;
    }

    public Compagnie getCompagnie() {
        return compagnie;
    }

    public void setCompagnie(Compagnie compagnie) {
        this.compagnie = compagnie;
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
        if (!(object instanceof Profil)) {
            return false;
        }
        Profil other = (Profil) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Profil[ id=" + id + " ]";
    }
    
}
