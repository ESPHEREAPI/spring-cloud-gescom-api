package sid.service_admin.dto;

import java.util.Date;
import lombok.Data;

@Data
public class AuditLogDTO {
    private Long id;
    private String actorUsername;
    private String actorRole;
    private String action;
    private String targetType;
    private Long targetId;
    private String details;
    private Date timestamp;
}
