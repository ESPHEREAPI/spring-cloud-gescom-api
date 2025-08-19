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
@Table(name = "role")
@NamedQueries({
    @NamedQuery(name = "Role.findAll", query = "SELECT r FROM Role r"),
    @NamedQuery(name = "Role.findById", query = "SELECT r FROM Role r WHERE r.id = :id"),
    @NamedQuery(name = "Role.findByCode", query = "SELECT r FROM Role r WHERE r.code = :code"),
    @NamedQuery(name = "Role.findByDescription", query = "SELECT r FROM Role r WHERE r.description = :description"),
    @NamedQuery(name = "Role.findByLastUserModif", query = "SELECT r FROM Role r WHERE r.lastUserModif = :lastUserModif"),
    @NamedQuery(name = "Role.findByLastdateModif", query = "SELECT r FROM Role r WHERE r.lastdateModif = :lastdateModif"),
    @NamedQuery(name = "Role.findByStatut", query = "SELECT r FROM Role r WHERE r.statut = :statut"),
    @NamedQuery(name = "Role.findByUserDelete", query = "SELECT r FROM Role r WHERE r.userDelete = :userDelete"),
    //@NamedQuery(name = "Role.findByLastUserModif1", query = "SELECT r FROM Role r WHERE r.lastUserModif1 = :lastUserModif1"),
    //@NamedQuery(name = "Role.findByLastdateModif1", query = "SELECT r FROM Role r WHERE r.lastdateModif1 = :lastdateModif1"),
   // @NamedQuery(name = "Role.findByUserDelete1", query = "SELECT r FROM Role r WHERE r.userDelete1 = :userDelete1")
})
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "code")
    private String code;
    @Column(name = "description")
    private String description;
    @Column(name = "last_user_modif")
    private String lastUserModif;
    @Column(name = "lastdate_modif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastdateModif;
    @Column(name = "statut")
    private String statut;
    @Column(name = "user_delete")
    private String userDelete;
    
   
    @OneToMany(mappedBy = "roleid")
    private Collection<Personne> personneCollection;

    public Role() {
    }

    public Role(Long id) {
        this.id = id;
    }

    public Role(Long id, String code) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastUserModif() {
        return lastUserModif;
    }

    public void setLastUserModif(String lastUserModif) {
        this.lastUserModif = lastUserModif;
    }

    public Date getLastdateModif() {
        return lastdateModif;
    }

    public void setLastdateModif(Date lastdateModif) {
        this.lastdateModif = lastdateModif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getUserDelete() {
        return userDelete;
    }

    public void setUserDelete(String userDelete) {
        this.userDelete = userDelete;
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
        if (!(object instanceof Role)) {
            return false;
        }
        Role other = (Role) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Role[ id=" + id + " ]";
    }
    
}
