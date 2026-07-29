package sid.service_admin.service;

import java.util.Date;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.CreateLicenceDTO;
import sid.service_admin.dto.LicenceDTO;
import sid.service_admin.dto.LicenceStatutDTO;
import sid.service_admin.enums.LicenceStatut;
import sid.service_admin.enums.ModuleLicence;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Licence;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.LicenceRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.security.LicenceKeyService;
import sid.service_admin.security.RoleNames;

/**
 * Gestion des licences par compagnie. Une licence active par compagnie
 * (findByCompagnie_Id). La cle signee (LicenceKeyService) est un artefact
 * d'affichage/preuve, pas le mecanisme d'application reel : l'application
 * se fait via le statut en base, lu par microservice-produits (voir
 * LicenceInternalController).
 */
@Service
public class LicenceService {

    private final LicenceRepository licenceRepository;
    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final LicenceKeyService licenceKeyService;
    private final AuditLogService auditLogService;

    public LicenceService(LicenceRepository licenceRepository, CompagnieRepository compagnieRepository,
            PersonneRepository personneRepository, LicenceKeyService licenceKeyService, AuditLogService auditLogService) {
        this.licenceRepository = licenceRepository;
        this.compagnieRepository = compagnieRepository;
        this.personneRepository = personneRepository;
        this.licenceKeyService = licenceKeyService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public LicenceDTO genererLicence(CreateLicenceDTO dto, String actorUsername) {
        if (dto.getCompagnieId() == null) {
            throw new BadRequestException("La compagnie est obligatoire");
        }
        if (dto.getDateExpiration() == null || dto.getDateExpiration().before(new Date())) {
            throw new BadRequestException("La date d'expiration doit etre dans le futur");
        }
        if (dto.getMaxUtilisateurs() == null || dto.getMaxUtilisateurs() < 1) {
            throw new BadRequestException("Le nombre maximum d'utilisateurs doit etre superieur a 0");
        }
        if (dto.getMaxBoutiques() == null || dto.getMaxBoutiques() < 1) {
            throw new BadRequestException("Le nombre maximum de boutiques doit etre superieur a 0");
        }
        if (dto.getModules() != null) {
            dto.getModules().forEach(m -> {
                try {
                    ModuleLicence.valueOf(m);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Module de licence inconnu : " + m);
                }
            });
        }

        Compagnie compagnie = compagnieRepository.findById(dto.getCompagnieId())
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + dto.getCompagnieId()));

        Personne actor = personneRepository.findByUserName(actorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + actorUsername));
        boolean isSuperAdmin = actor.getRoleid() != null && RoleNames.SUPER_ADMIN.equals(actor.getRoleid().getName());
        if (!isSuperAdmin && (compagnie.getCreatedBy() == null || !compagnie.getCreatedBy().equals(actorUsername))) {
            throw new AccessDeniedException("Vous ne pouvez generer une licence que pour une compagnie que vous avez creee");
        }

        Licence licence = licenceRepository.findByCompagnie_Id(compagnie.getId()).orElseGet(Licence::new);
        licence.setCompagnie(compagnie);
        licence.setStatut(LicenceStatut.ACTIVE);
        licence.setDateDebut(new Date());
        licence.setDateExpiration(dto.getDateExpiration());
        licence.setMaxUtilisateurs(dto.getMaxUtilisateurs());
        licence.setMaxBoutiques(dto.getMaxBoutiques());
        licence.setModulesActifs(dto.getModules());
        licence.setCreatedBy(actorUsername);
        licence.setCreatedAt(new Date());
        licence.setRevokedBy(null);
        licence.setDateRevocation(null);
        Licence saved = licenceRepository.save(licence);

        saved.setCle(licenceKeyService.genererCle(saved));
        saved = licenceRepository.save(saved);

        auditLogService.log("LICENCE_GENEREE", "Compagnie", compagnie.getId(),
                "Licence generee/renouvelee pour " + compagnie.getNom());

        return toDTO(saved);
    }

    @Transactional
    public LicenceDTO revoquer(Long compagnieId, String actorUsername) {
        Licence licence = getActive(compagnieId);
        licence.setStatut(LicenceStatut.REVOQUEE);
        licence.setRevokedBy(actorUsername);
        licence.setDateRevocation(new Date());
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_REVOQUEE", "Compagnie", compagnieId, "Licence revoquee");
        return dto;
    }

    @Transactional
    public LicenceDTO suspendre(Long compagnieId, String actorUsername) {
        Licence licence = getActive(compagnieId);
        licence.setStatut(LicenceStatut.SUSPENDUE);
        licence.setRevokedBy(actorUsername);
        licence.setDateRevocation(new Date());
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_SUSPENDUE", "Compagnie", compagnieId, "Licence suspendue");
        return dto;
    }

    @Transactional
    public LicenceDTO reactiver(Long compagnieId) {
        Licence licence = getActive(compagnieId);
        licence.setStatut(LicenceStatut.ACTIVE);
        licence.setRevokedBy(null);
        licence.setDateRevocation(null);
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_REACTIVEE", "Compagnie", compagnieId, "Licence reactivee");
        return dto;
    }

    @Transactional(readOnly = true)
    public LicenceDTO getByCompagnie(Long compagnieId) {
        return toDTO(getActive(compagnieId));
    }

    @Transactional(readOnly = true)
    public LicenceStatutDTO getStatutInterne(Long compagnieId) {
        Licence licence = getActive(compagnieId);
        LicenceStatutDTO dto = new LicenceStatutDTO();
        dto.setStatut(licence.getStatut().name());
        dto.setDateExpiration(licence.getDateExpiration());
        dto.setMaxUtilisateurs(licence.getMaxUtilisateurs());
        dto.setMaxBoutiques(licence.getMaxBoutiques());
        dto.setModules(licence.getModulesActifs());
        return dto;
    }

    /** Appele localement par UserService avant de creer un utilisateur rattache a une compagnie. */
    @Transactional(readOnly = true)
    public void verifierQuotaUtilisateurs(Long compagnieId) {
        if (compagnieId == null) {
            return;
        }
        Licence licence = getActive(compagnieId);
        if (licence.getStatut() != LicenceStatut.ACTIVE) {
            throw new ConflictException("La licence de cette compagnie n'est pas active");
        }
        long actuel = personneRepository.countByCompagnie_Id(compagnieId);
        if (licence.getMaxUtilisateurs() != null && actuel >= licence.getMaxUtilisateurs()) {
            throw new ConflictException("Quota d'utilisateurs atteint pour cette compagnie (max " + licence.getMaxUtilisateurs() + ")");
        }
    }

    private Licence getActive(Long compagnieId) {
        return licenceRepository.findByCompagnie_Id(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune licence pour cette compagnie : " + compagnieId));
    }

    private LicenceDTO toDTO(Licence licence) {
        LicenceDTO dto = new LicenceDTO();
        dto.setId(licence.getId());
        dto.setCompagnieId(licence.getCompagnie().getId());
        dto.setCompagnieNom(licence.getCompagnie().getNom());
        dto.setStatut(licence.getStatut().name());
        dto.setDateDebut(licence.getDateDebut());
        dto.setDateExpiration(licence.getDateExpiration());
        dto.setMaxUtilisateurs(licence.getMaxUtilisateurs());
        dto.setMaxBoutiques(licence.getMaxBoutiques());
        dto.setModules(licence.getModulesActifs());
        dto.setCle(licence.getCle());
        dto.setCreatedBy(licence.getCreatedBy());
        dto.setCreatedAt(licence.getCreatedAt());
        dto.setRevokedBy(licence.getRevokedBy());
        dto.setDateRevocation(licence.getDateRevocation());
        return dto;
    }
}
