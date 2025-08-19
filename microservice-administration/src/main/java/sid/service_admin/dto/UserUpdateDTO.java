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
public class UserUpdateDTO {

    private String userName;
    private String firstName;
    private String lastname;
    private String email;
    private String password;
    private String tel;
    private String address;
 
    //private String createdBy;
    private String profileImageUrl;
    private Long roleid;
    private Long profilid;
    private String profession;
    private String remarques;
    private String sexe;
    private String situationMatrimoniale;
    private String lieuNaissance;
    private String quartier;
    private String region;
    private String indicatifPays;
    private String codePays;
    private Date dateNaissance;
    private Long paysid;
    private Long religionid;
    private Long titreid;
    private String bp;
    private String ville;
    private Boolean autorisationDeletes;

    private String createdBy;
    private Boolean isActive;
}
