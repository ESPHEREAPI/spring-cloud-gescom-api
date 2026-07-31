package sid.service_admin.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String ancienMotDePasse;
    private String nouveauMotDePasse;
}
