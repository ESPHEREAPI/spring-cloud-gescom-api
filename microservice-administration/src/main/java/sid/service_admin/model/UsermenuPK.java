/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 *
 * @author USER01
 */
@Embeddable
public class UsermenuPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "Menu_id")
    private long menuid;
    @Basic(optional = false)
    @Column(name = "User_id")
    private long userid;

    public UsermenuPK() {
    }

    public UsermenuPK(long menuid, long userid) {
        this.menuid = menuid;
        this.userid = userid;
    }

    public long getMenuid() {
        return menuid;
    }

    public void setMenuid(long menuid) {
        this.menuid = menuid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) menuid;
        hash += (int) userid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UsermenuPK)) {
            return false;
        }
        UsermenuPK other = (UsermenuPK) object;
        if (this.menuid != other.menuid) {
            return false;
        }
        if (this.userid != other.userid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sid.service_admin.model.UsermenuPK[ menuid=" + menuid + ", userid=" + userid + " ]";
    }
    
}
