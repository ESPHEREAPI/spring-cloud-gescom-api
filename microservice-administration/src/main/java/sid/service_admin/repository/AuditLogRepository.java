package sid.service_admin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUsernameInOrderByTimestampDesc(List<String> actorUsernames);
}
