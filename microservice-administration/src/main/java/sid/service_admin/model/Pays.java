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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@AllArgsConstructor
@Table(name = "pays")
@NamedQueries({
    @NamedQuery(name = "Pays.findAll", query = "SELECT p FROM Pays p"),
    @NamedQuery(name = "Pays.findById", query = "SELECT p FROM Pays p WHERE p.id = :id"),
    @NamedQuery(name = "Pays.findByCode", query = "SELECT p FROM Pays p WHERE p.code = :code"),
    @NamedQuery(name = "Pays.findByContinent", query = "SELECT p FROM Pays p WHERE p.continent = :continent"),
    @NamedQuery(name = "Pays.findByLibelle", query = "SELECT p FROM Pays p WHERE p.libelle = :libelle"),
    @NamedQuery(name = "Pays.findByLibelleAnglais", query = "SELECT p FROM Pays p WHERE p.libelleAnglais = :libelleAnglais")})
public class Pays implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "Code")
    private String code;
    @Column(name = "Continent")
    private String continent;
    @Column(name = "Libelle")
    private String libelle;
    @Column(name = "LibelleAnglais")
    private String libelleAnglais;
    @OneToMany(mappedBy = "paysid")
    private List<Personne> personneList;

    public Pays() {
    }

    public Pays(String code, String libelle, String libelleAnglais, String continent) {
        this.code = code;
        this.continent = continent;
        this.libelle = libelle;
        this.libelleAnglais = libelleAnglais;
    }

    public Pays(Long id) {
        this.id = id;
    }

    public Pays(Long id, String code) {
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

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelleAnglais() {
        return libelleAnglais;
    }

    public void setLibelleAnglais(String libelleAnglais) {
        this.libelleAnglais = libelleAnglais;
    }

    public List<Personne> getPersonneList() {
        return personneList;
    }

    public void setPersonneList(List<Personne> personneList) {
        this.personneList = personneList;
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
        if (!(object instanceof Pays)) {
            return false;
        }
        Pays other = (Pays) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Pays[ id=" + id + " ]";
    }
    
}
