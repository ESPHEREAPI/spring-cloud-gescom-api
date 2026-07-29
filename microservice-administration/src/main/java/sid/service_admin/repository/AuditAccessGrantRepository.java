package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.AuditAccessGrant;

public interface AuditAccessGrantRepository extends JpaRepository<AuditAccessGrant, Long> {
    boolean existsByGranteeUsernameAndRevokedFalse(String granteeUsername);
    Optional<AuditAccessGrant> findByGranteeUsernameAndRevokedFalse(String granteeUsername);
    List<AuditAccessGrant> findAllByRevokedFalse();
}
