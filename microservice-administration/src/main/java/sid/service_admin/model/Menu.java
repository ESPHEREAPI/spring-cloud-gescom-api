/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "menu")
@NamedQueries({
    @NamedQuery(name = "Menu.findAll", query = "SELECT m FROM Menu m"),
    @NamedQuery(name = "Menu.findById", query = "SELECT m FROM Menu m WHERE m.id = :id"),
    @NamedQuery(name = "Menu.findByChemin", query = "SELECT m FROM Menu m WHERE m.chemin = :chemin"),
    @NamedQuery(name = "Menu.findByCode", query = "SELECT m FROM Menu m WHERE m.code = :code"),
    @NamedQuery(name = "Menu.findByDescription", query = "SELECT m FROM Menu m WHERE m.description = :description"),
    @NamedQuery(name = "Menu.findByEtat", query = "SELECT m FROM Menu m WHERE m.etat = :etat")})
//@JsonIgnoreProperties({"usermenuList"})

public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Column(name = "chemin")
    private String chemin;
    @Basic(optional = false)
    @Column(name = "code")
    private String code;
    @Column(name = "description")
    private String description;
    @Column(name = "etat")
    private Boolean etat;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "menu")
    @JsonIgnore
    private List<Usermenu> usermenuCollection;
    @JoinColumn(name = "Moduleid", referencedColumnName = "Id")
    @ManyToOne
    private Modulesecurite moduleid;

    public Menu() {
    }

    public Menu(Long id) {
        this.id = id;
    }

    public Menu(String code, String description, Modulesecurite moduleid) {
        this.code = code;
        this.description = description;
        this.moduleid = moduleid;
    }

   
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
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

    public Boolean getEtat() {
        return etat;
    }

    public void setEtat(Boolean etat) {
        this.etat = etat;
    }

   

    public Modulesecurite getModuleid() {
        return moduleid;
    }

    public void setModuleid(Modulesecurite moduleid) {
        this.moduleid = moduleid;
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
        if (!(object instanceof Menu)) {
            return false;
        }
        Menu other = (Menu) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Menu[ id=" + id + " ]";
    }
    
}
