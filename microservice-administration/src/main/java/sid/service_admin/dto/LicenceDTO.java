package sid.service_admin.dto;

import java.util.Date;
import java.util.Set;
import lombok.Data;

@Data
public class LicenceDTO {
    private Long id;
    private Long compagnieId;
    private String compagnieNom;
    private String statut;
    private Date dateDebut;
    private Date dateExpiration;
    private Integer maxUtilisateurs;
    private Integer maxBoutiques;
    private Set<String> modules;
    private String cle;
    private String createdBy;
    private Date createdAt;
    private String revokedBy;
    private Date dateRevocation;
}
