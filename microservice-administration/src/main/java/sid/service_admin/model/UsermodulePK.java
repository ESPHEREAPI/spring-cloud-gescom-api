/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Embeddable
@Data
 @AllArgsConstructor
public class UsermodulePK implements Serializable {

    @Basic(optional = false)
    @Column(name = "mod_secuid")
    private long modsecuid;
    @Basic(optional = false)
    @Column(name = "user_id")
    private long userid;

    public UsermodulePK() {
    }

    

    
    
}
