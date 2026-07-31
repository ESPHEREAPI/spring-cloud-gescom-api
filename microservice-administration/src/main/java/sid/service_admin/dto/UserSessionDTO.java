/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class UserSessionDTO {

    private UserDTO usersDTO;
    private String token;
    private List<String> permissions;
    private Date expiresAt;
    private int anneeid;
    private boolean hasAuditAccess;
    private boolean mustChangePassword;
}
