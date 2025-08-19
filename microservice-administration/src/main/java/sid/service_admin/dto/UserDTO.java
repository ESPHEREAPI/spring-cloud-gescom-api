/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastname;
    private String userName;
    private String email;
    private String tel;
    private String indicatifPays;
    private String codePays;
    private String address;
    private String profileImageUrl;
    private Date createdAt;
    private Date updatedAt;
    private Date lastlogin;
    private Boolean isActive;
    private Boolean echeck_connection = Boolean.FALSE;
    private RoleDTO role;
    private ProfilDTO profil;
    private Long roleid;
    private String createdBy;
    private String updateByUserName;
    private String messageEcheck;

    private Boolean autorisationDeletes;

}
