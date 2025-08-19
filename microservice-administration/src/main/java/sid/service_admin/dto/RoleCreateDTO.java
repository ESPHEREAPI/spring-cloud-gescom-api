/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class RoleCreateDTO {
    private String name;
        private String description;
        private Set<Long> permissionIds = new HashSet<>();
}
