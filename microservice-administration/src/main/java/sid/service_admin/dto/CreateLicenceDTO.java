package sid.service_admin.dto;

import java.util.Date;
import java.util.Set;
import lombok.Data;

@Data
public class CreateLicenceDTO {
    private Long compagnieId;
    private Date dateExpiration;
    private Integer maxUtilisateurs;
    private Integer maxBoutiques;
    private Set<String> modules;
}
