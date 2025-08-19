/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class UserCreateDTO {

    private Long id;
    private String userName;
    private String firstName;
    private String lastname;

    private String password;

    private String tel;

    private String email;

    private String indicatifPays;
    private String codepays;
    private String createdBy;

    private Long roleid;

    private Boolean autorisationDeletes;

    private Boolean isActive;

    private Long profilid;

}
