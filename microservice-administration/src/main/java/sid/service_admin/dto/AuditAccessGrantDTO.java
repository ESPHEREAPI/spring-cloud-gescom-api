package sid.service_admin.dto;

import java.util.Date;
import lombok.Data;

@Data
public class AuditAccessGrantDTO {
    private Long id;
    private String granteeUsername;
    private String grantedByUsername;
    private Date grantedAt;
    private Boolean revoked;
}
