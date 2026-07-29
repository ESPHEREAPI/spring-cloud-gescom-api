package sid.service_admin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.AuditLogDTO;
import sid.service_admin.model.AuditLog;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.AuditLogRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.security.RoleNames;

/**
 * Journalise les actions sensibles et permet au SUPER_ADMIN (ou a une
 * personne a qui il a delegue l'acces, voir AuditAccessService) de consulter
 * les actions des administrateurs systeme qu'il a lui-meme crees.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final PersonneRepository personneRepository;
    private final AuditAccessService auditAccessService;

    public AuditLogService(AuditLogRepository auditLogRepository, PersonneRepository personneRepository,
            AuditAccessService auditAccessService) {
        this.auditLogRepository = auditLogRepository;
        this.personneRepository = personneRepository;
        this.auditAccessService = auditAccessService;
    }

    public void log(String action, String targetType, Long targetId, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actorUsername = authentication != null ? authentication.getName() : "systeme";
        String actorRole = authentication == null ? null : authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring("ROLE_".length()))
                .orElse(null);

        AuditLog entry = new AuditLog();
        entry.setActorUsername(actorUsername);
        entry.setActorRole(actorRole);
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetails(details);
        entry.setTimestamp(new Date());
        auditLogRepository.save(entry);
    }

    /** Le demandeur voit ses propres actions et celles des SYSTEM_ADMIN qu'il (ou le compte a qui il a delegue) a crees. */
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getScopedLogs(String requesterUsername) {
        String scopeOwner = auditAccessService.resolveScopeOwner(requesterUsername);

        List<String> usernames = personneRepository.findByRoleid_NameAndCreatedBy(RoleNames.SYSTEM_ADMIN, scopeOwner)
                .stream().map(Personne::getUserName).collect(Collectors.toCollection(ArrayList::new));
        usernames.add(scopeOwner);

        return auditLogRepository.findByActorUsernameInOrderByTimestampDesc(usernames)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog entry) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(entry.getId());
        dto.setActorUsername(entry.getActorUsername());
        dto.setActorRole(entry.getActorRole());
        dto.setAction(entry.getAction());
        dto.setTargetType(entry.getTargetType());
        dto.setTargetId(entry.getTargetId());
        dto.setDetails(entry.getDetails());
        dto.setTimestamp(entry.getTimestamp());
        return dto;
    }
}
