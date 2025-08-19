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
import java.io.Serializable;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "indicatifpays")
@NamedQueries({
    @NamedQuery(name = "Indicatifpays.findAll", query = "SELECT i FROM Indicatifpays i"),
    @NamedQuery(name = "Indicatifpays.findById", query = "SELECT i FROM Indicatifpays i WHERE i.id = :id"),
    @NamedQuery(name = "Indicatifpays.findByIndicatif", query = "SELECT i FROM Indicatifpays i WHERE i.indicatif = :indicatif")})
public class Indicatifpays implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "Indicatif")
    private String indicatif;

    public Indicatifpays() {
    }

 

    public Indicatifpays( String indicatif) {
     
        this.indicatif = indicatif;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIndicatif() {
        return indicatif;
    }

    public void setIndicatif(String indicatif) {
        this.indicatif = indicatif;
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
        if (!(object instanceof Indicatifpays)) {
            return false;
        }
        Indicatifpays other = (Indicatifpays) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Indicatifpays[ id=" + id + " ]";
    }
    
}
