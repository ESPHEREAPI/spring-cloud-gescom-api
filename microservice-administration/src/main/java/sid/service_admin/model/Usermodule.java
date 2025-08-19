/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "usermodule")
@NamedQueries({
    @NamedQuery(name = "Usermodule.findAll", query = "SELECT u FROM Usermodule u"),
    @NamedQuery(name = "Usermodule.findByModSecuid", query = "SELECT u FROM Usermodule u WHERE u.usermodulePK.modsecuid = :modSecuid"),
    @NamedQuery(name = "Usermodule.findByUserId", query = "SELECT u FROM Usermodule u WHERE u.usermodulePK.userid = :user_id"),
    @NamedQuery(name = "Usermodule.findByAddDate", query = "SELECT u FROM Usermodule u WHERE u.addDate = :addDate"),
    @NamedQuery(name = "Usermodule.findByUserAdd", query = "SELECT u FROM Usermodule u WHERE u.userAdd = :userAdd")})
//@JsonIgnoreProperties({"usermenuPK"})
public class Usermodule implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @JsonIgnore
    protected UsermodulePK usermodulePK;
    @Column(name = "add_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addDate;
    @Column(name = "user_add")
    private String userAdd;
    @JoinColumn(name = "mod_secuid", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Modulesecurite modulesecurite;
    @JoinColumn(name = "user_id", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Personne personne;

    public Usermodule() {
    }

    public Usermodule(UsermodulePK usermodulePK) {
        this.usermodulePK = usermodulePK;
    }

    public Usermodule(long modSecuid, long userId) {
        this.usermodulePK = new UsermodulePK(modSecuid, userId);
    }

    public UsermodulePK getUsermodulePK() {
        return usermodulePK;
    }

    public void setUsermodulePK(UsermodulePK usermodulePK) {
        this.usermodulePK = usermodulePK;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getUserAdd() {
        return userAdd;
    }

    public void setUserAdd(String userAdd) {
        this.userAdd = userAdd;
    }

    public Modulesecurite getModulesecurite() {
        return modulesecurite;
    }

    public void setModulesecurite(Modulesecurite modulesecurite) {
        this.modulesecurite = modulesecurite;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (usermodulePK != null ? usermodulePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usermodule)) {
            return false;
        }
        Usermodule other = (Usermodule) object;
        if ((this.usermodulePK == null && other.usermodulePK != null) || (this.usermodulePK != null && !this.usermodulePK.equals(other.usermodulePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Usermodule[ usermodulePK=" + usermodulePK + " ]";
    }
    
}
