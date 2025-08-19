/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class JwtResponse {
     private String token;
        private String type = "Bearer";
        private Long id;
        private String email;
        private Set<String> roles = new HashSet<>();

    public JwtResponse(String token,Long id,String email,Set roles) {
        this.token=token;
        this.id=id;
        this.email=email;
        this.roles=roles;
    }
        
}
