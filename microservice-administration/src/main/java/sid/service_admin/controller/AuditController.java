package sid.service_admin.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.AuditAccessGrantDTO;
import sid.service_admin.dto.AuditLogDTO;
import sid.service_admin.dto.CreateAuditAccessGrantDTO;
import sid.service_admin.service.AuditAccessService;
import sid.service_admin.service.AuditLogService;

/**
 * Consultation de l'audit (SUPER_ADMIN par defaut, ou toute personne a qui il
 * a delegue l'acces) et gestion des delegations (SUPER_ADMIN uniquement).
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogService auditLogService;
    private final AuditAccessService auditAccessService;

    public AuditController(AuditLogService auditLogService, AuditAccessService auditAccessService) {
        this.auditLogService = auditLogService;
        this.auditAccessService = auditAccessService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('AUDIT_VIEWER')")
    public ResponseEntity<List<AuditLogDTO>> logs(Authentication authentication) {
        return ResponseEntity.ok(auditLogService.getScopedLogs(authentication.getName()));
    }

    @GetMapping("/access-grants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditAccessGrantDTO>> listAccessGrants() {
        return ResponseEntity.ok(auditAccessService.listActive());
    }

    @PostMapping("/access-grants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AuditAccessGrantDTO> grantAccess(@RequestBody CreateAuditAccessGrantDTO dto, Authentication authentication) {
        AuditAccessGrantDTO result = auditAccessService.grant(dto.getGranteeUsername(), authentication.getName());
        auditLogService.log("AUDIT_ACCESS_ACCORDE", "Personne", null, "Acces audit accorde a " + dto.getGranteeUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/access-grants/{id}/revoquer")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> revokeAccess(@PathVariable Long id) {
        auditAccessService.revoke(id);
        auditLogService.log("AUDIT_ACCESS_REVOQUE", "AuditAccessGrant", id, "Delegation d'acces audit revoquee");
        return ResponseEntity.noContent().build();
    }
}
