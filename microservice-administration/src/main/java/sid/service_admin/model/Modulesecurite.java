/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "modulesecurite")
@NamedQueries({
    @NamedQuery(name = "Modulesecurite.findAll", query = "SELECT m FROM Modulesecurite m"),
    @NamedQuery(name = "Modulesecurite.findById", query = "SELECT m FROM Modulesecurite m WHERE m.id = :id"),
    @NamedQuery(name = "Modulesecurite.findByCode", query = "SELECT m FROM Modulesecurite m WHERE m.code = :code"),
    @NamedQuery(name = "Modulesecurite.findByDescription", query = "SELECT m FROM Modulesecurite m WHERE m.description = :description")})
@JsonIgnoreProperties({"usermoduleList", "menuList"})
public class Modulesecurite implements Serializable {

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "modulesecurite")
    //@JsonIgnore
    private List<Usermodule> usermoduleCollection;
    @OneToMany(mappedBy = "moduleid")
    
    private List<Menu> menuList;

    public Modulesecurite() {
    }

    public Modulesecurite(Long id) {
        this.id = id;
    }

    public Modulesecurite(Long id, String code) {
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

  

    public List<Menu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuList = menuList;
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
        if (!(object instanceof Modulesecurite)) {
            return false;
        }
        Modulesecurite other = (Modulesecurite) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Modulesecurite[ id=" + id + " ]";
    }
    
}
