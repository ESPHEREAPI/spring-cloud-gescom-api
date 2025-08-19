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
    @NamedQuery(name = "Profil.findByUserdelete", query = "SELECT p FROM Profil p WHERE p.userdelete = :userdelete"),
    //@NamedQuery(name = "Profil.findByAddDate1", query = "SELECT p FROM Profil p WHERE p.addDate1 = :addDate1"),
    //@NamedQuery(name = "Profil.findByLastDateModif1", query = "SELECT p FROM Profil p WHERE p.lastDateModif1 = :lastDateModif1"),
    //@NamedQuery(name = "Profil.findByLastUserModif1", query = "SELECT p FROM Profil p WHERE p.lastUserModif1 = :lastUserModif1"),
   // @NamedQuery(name = "Profil.findByLockDuration1", query = "SELECT p FROM Profil p WHERE p.lockDuration1 = :lockDuration1"),
    //@NamedQuery(name = "Profil.findByNberEchecCnx1", query = "SELECT p FROM Profil p WHERE p.nberEchecCnx1 = :nberEchecCnx1"),
   // @NamedQuery(name = "Profil.findByPwdDuration1", query = "SELECT p FROM Profil p WHERE p.pwdDuration1 = :pwdDuration1"),
   // @NamedQuery(name = "Profil.findByUserAdd1", query = "SELECT p FROM Profil p WHERE p.userAdd1 = :userAdd1")
})
public class Profil implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Column(name = "add_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addDate;
    @Basic(optional = false)
    @Column(name = "code")
    private String code;
    @Column(name = "description")
    private String description;
    @Column(name = "last_dateModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDateModif;
    @Column(name = "lastuser_modif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUserModif;
    @Column(name = "lock_duration")
    private Integer lockDuration;
    @Column(name = "nber_echeccnx")
    private Integer nberEchecCnx;
    @Column(name = "pwd_duration")
    private Integer pwdDuration;
    @Column(name = "statut")
    private String statut;
    @Column(name = "user_add")
    private String userAdd;
    @Column(name = "userdelete")
    private String userdelete;
   
  

    @OneToMany(mappedBy = "profilid")
    private Collection<Personne> personneCollection;

    public Profil() {
    }

    public Profil(Long id) {
        this.id = id;
    }

    public Profil(Long id, String code) {
        this.id = id;
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
        return "com.mproduits.model.Profil[ id=" + id + " ]";
    }
    
}
