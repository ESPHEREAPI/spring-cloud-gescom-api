package sid.service_admin.dto;

import lombok.Data;

@Data
public class CreateSystemAdminDTO {
    private String userName;
    private String firstName;
    private String lastname;
    private String email;
    private String tel;
    /** Optionnel : si absent, un mot de passe est genere aleatoirement. */
    private String password;
}
