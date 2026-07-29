package sid.service_admin.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.AuditAccessGrantDTO;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.AuditAccessGrant;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.AuditAccessGrantRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.security.RoleNames;

/**
 * Delegation individuelle de la consultation de l'audit : par defaut seul le
 * SUPER_ADMIN a acces, il choisit qui d'autre en a (voir AuditAccessGrant).
 */
@Service
public class AuditAccessService {

    // Pas de dependance vers AuditLogService ici : AuditLogService depend deja
    // de ce service (resolveScopeOwner) - le journal des actions de delegation
    // est ecrit par l'appelant (AuditController), pas ici, pour eviter un cycle.
    private final PersonneRepository personneRepository;
    private final AuditAccessGrantRepository grantRepository;

    public AuditAccessService(PersonneRepository personneRepository, AuditAccessGrantRepository grantRepository) {
        this.personneRepository = personneRepository;
        this.grantRepository = grantRepository;
    }

    public boolean isSuperAdmin(String username) {
        return personneRepository.findByUserName(username)
                .map(Personne::getRoleid)
                .filter(role -> role != null && RoleNames.SUPER_ADMIN.equals(role.getName()))
                .isPresent();
    }

    @Transactional(readOnly = true)
    public boolean hasAuditAccess(String username) {
        if (isSuperAdmin(username)) {
            return true;
        }
        return grantRepository.existsByGranteeUsernameAndRevokedFalse(username);
    }

    /** Compte dont le perimetre d'audit s'applique a ce demandeur (lui-meme si SUPER_ADMIN, sinon le SUPER_ADMIN qui lui a delegue l'acces). */
    @Transactional(readOnly = true)
    public String resolveScopeOwner(String username) {
        if (isSuperAdmin(username)) {
            return username;
        }
        return grantRepository.findByGranteeUsernameAndRevokedFalse(username)
                .map(AuditAccessGrant::getGrantedByUsername)
                .orElse(username);
    }

    @Transactional
    public AuditAccessGrantDTO grant(String granteeUsername, String grantedByUsername) {
        AuditAccessGrant grant = grantRepository.findByGranteeUsernameAndRevokedFalse(granteeUsername)
                .orElseGet(AuditAccessGrant::new);
        grant.setGranteeUsername(granteeUsername);
        grant.setGrantedByUsername(grantedByUsername);
        grant.setGrantedAt(new Date());
        grant.setRevoked(Boolean.FALSE);
        return toDTO(grantRepository.save(grant));
    }

    @Transactional
    public void revoke(Long grantId) {
        AuditAccessGrant grant = grantRepository.findById(grantId)
                .orElseThrow(() -> new ResourceNotFoundException("Delegation non trouvee : " + grantId));
        grant.setRevoked(Boolean.TRUE);
        grantRepository.save(grant);
    }

    @Transactional(readOnly = true)
    public List<AuditAccessGrantDTO> listActive() {
        return grantRepository.findAllByRevokedFalse().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditAccessGrantDTO toDTO(AuditAccessGrant grant) {
        AuditAccessGrantDTO dto = new AuditAccessGrantDTO();
        dto.setId(grant.getId());
        dto.setGranteeUsername(grant.getGranteeUsername());
        dto.setGrantedByUsername(grant.getGrantedByUsername());
        dto.setGrantedAt(grant.getGrantedAt());
        dto.setRevoked(grant.getRevoked());
        return dto;
    }
}
