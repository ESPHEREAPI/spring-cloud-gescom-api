package sid.service_admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordResultDTO {
    private String generatedPassword;
}
