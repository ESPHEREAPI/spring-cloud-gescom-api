/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

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
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "usermenu")
@NamedQueries({
    @NamedQuery(name = "Usermenu.findAll", query = "SELECT u FROM Usermenu u"),
    @NamedQuery(name = "Usermenu.findByMenuid", query = "SELECT u FROM Usermenu u WHERE u.usermenuPK.menuid = :menuid"),
    @NamedQuery(name = "Usermenu.findByUserid", query = "SELECT u FROM Usermenu u WHERE u.usermenuPK.userid = :userid"),
    @NamedQuery(name = "Usermenu.findByAddpriv", query = "SELECT u FROM Usermenu u WHERE u.addpriv = :addpriv"),
    @NamedQuery(name = "Usermenu.findByAffichpriv", query = "SELECT u FROM Usermenu u WHERE u.affichpriv = :affichpriv"),
    @NamedQuery(name = "Usermenu.findByAutorisation", query = "SELECT u FROM Usermenu u WHERE u.autorisation = :autorisation"),
    @NamedQuery(name = "Usermenu.findByDeletepriv", query = "SELECT u FROM Usermenu u WHERE u.deletepriv = :deletepriv"),
    @NamedQuery(name = "Usermenu.findByPrintpriv", query = "SELECT u FROM Usermenu u WHERE u.printpriv = :printpriv"),
    @NamedQuery(name = "Usermenu.findByUpdatepriv", query = "SELECT u FROM Usermenu u WHERE u.updatepriv = :updatepriv")})
@JsonIgnoreProperties({"usermenuPK"})
public class Usermenu implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UsermenuPK usermenuPK;
    @Column(name = "addpriv")
    private Boolean addpriv;
    @Column(name = "affichpriv")
    private Boolean affichpriv;
    @Column(name = "autorisation")
    private Boolean autorisation;
    @Column(name = "deletepriv")
    private Boolean deletepriv;
    @Column(name = "printpriv")
    private Boolean printpriv;
    @Column(name = "updatepriv")
    private Boolean updatepriv;
    @JoinColumn(name = "Menu_id", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Menu menu;
    @JoinColumn(name = "User_id", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Personne personne;
 @Column(name = "user_add")
    private String userAdd;
  @Temporal(TemporalType.TIMESTAMP)
    private Date addDate;
    public Usermenu() {
    }

    public Usermenu(UsermenuPK usermenuPK) {
        this.usermenuPK = usermenuPK;
    }

    public Usermenu(long menuid, long userid) {
        this.usermenuPK = new UsermenuPK(menuid, userid);
    }

    public UsermenuPK getUsermenuPK() {
        return usermenuPK;
    }

    public void setUsermenuPK(UsermenuPK usermenuPK) {
        this.usermenuPK = usermenuPK;
    }

    public Boolean getAddpriv() {
        return addpriv;
    }

    public void setAddpriv(Boolean addpriv) {
        this.addpriv = addpriv;
    }

    public Boolean getAffichpriv() {
        return affichpriv;
    }

    public void setAffichpriv(Boolean affichpriv) {
        this.affichpriv = affichpriv;
    }

    public Boolean getAutorisation() {
        return autorisation;
    }

    public void setAutorisation(Boolean autorisation) {
        this.autorisation = autorisation;
    }

    public Boolean getDeletepriv() {
        return deletepriv;
    }

    public void setDeletepriv(Boolean deletepriv) {
        this.deletepriv = deletepriv;
    }

    public Boolean getPrintpriv() {
        return printpriv;
    }

    public void setPrintpriv(Boolean printpriv) {
        this.printpriv = printpriv;
    }

    public Boolean getUpdatepriv() {
        return updatepriv;
    }

    public void setUpdatepriv(Boolean updatepriv) {
        this.updatepriv = updatepriv;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
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
        hash += (usermenuPK != null ? usermenuPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usermenu)) {
            return false;
        }
        Usermenu other = (Usermenu) object;
        if ((this.usermenuPK == null && other.usermenuPK != null) || (this.usermenuPK != null && !this.usermenuPK.equals(other.usermenuPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.Usermenu[ usermenuPK=" + usermenuPK + " ]";
    }
    
}
