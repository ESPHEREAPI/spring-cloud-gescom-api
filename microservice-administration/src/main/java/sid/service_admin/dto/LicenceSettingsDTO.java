package sid.service_admin.dto;

import java.util.Set;
import lombok.Data;

@Data
public class LicenceSettingsDTO {
    private Boolean essaiActif;
    private Integer essaiDureeJours;
    private Integer essaiMaxUtilisateurs;
    private Integer essaiMaxBoutiques;
    private Set<String> essaiModules;
}
