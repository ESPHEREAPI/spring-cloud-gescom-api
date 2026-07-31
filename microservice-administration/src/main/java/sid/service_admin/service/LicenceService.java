package sid.service_admin.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.ActiverLicenceDTO;
import sid.service_admin.dto.CreateLicenceDTO;
import sid.service_admin.dto.LicenceDTO;
import sid.service_admin.dto.LicenceMineDTO;
import sid.service_admin.dto.LicenceStatutDTO;
import sid.service_admin.enums.LicenceStatut;
import sid.service_admin.enums.ModuleLicence;
import sid.service_admin.enums.ModulesParTypeCommerce;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Licence;
import sid.service_admin.model.LicenceSettings;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.LicenceRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.security.LicenceKeyService;
import sid.service_admin.security.RoleNames;

/**
 * Gestion des licences par compagnie. Une compagnie accumule un historique de
 * lignes (une par generation/renouvellement) : la ligne "courante" depend du
 * contexte, voir getLatest (gestion SUPER_ADMIN/SYSTEM_ADMIN) et
 * getCurrentActive (application reelle, lue par microservice-produits).
 *
 * Generer une licence (SYSTEM_ADMIN/SUPER_ADMIN) et l'activer (compagnie, en
 * collant la cle recue) sont deux etapes distinctes : une ligne nouvellement
 * generee est GENEREE, pas ACTIVE, et ne donne aucun acces tant qu'elle n'a
 * pas ete activee. Ceci evite qu'un renouvellement pre-genere ne casse
 * l'acces en cours avant meme d'etre active (voir getCurrentActive).
 */
@Service
public class LicenceService {

    private final LicenceRepository licenceRepository;
    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final LicenceKeyService licenceKeyService;
    private final AuditLogService auditLogService;
    private final LicenceSettingsService licenceSettingsService;

    public LicenceService(LicenceRepository licenceRepository, CompagnieRepository compagnieRepository,
            PersonneRepository personneRepository, LicenceKeyService licenceKeyService, AuditLogService auditLogService,
            LicenceSettingsService licenceSettingsService) {
        this.licenceRepository = licenceRepository;
        this.compagnieRepository = compagnieRepository;
        this.personneRepository = personneRepository;
        this.licenceKeyService = licenceKeyService;
        this.auditLogService = auditLogService;
        this.licenceSettingsService = licenceSettingsService;
    }

    // ===================== SUPER_ADMIN / SYSTEM_ADMIN =====================

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

        Compagnie compagnie = compagnieRepository.findById(dto.getCompagnieId())
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + dto.getCompagnieId()));
        checkOwnership(compagnie, actorUsername);
        validerModules(dto.getModules(), compagnie);

        Licence licence = new Licence();
        licence.setCompagnie(compagnie);
        licence.setStatut(LicenceStatut.GENEREE);
        licence.setDateDebut(null);
        licence.setDateExpiration(dto.getDateExpiration());
        licence.setMaxUtilisateurs(dto.getMaxUtilisateurs());
        licence.setMaxBoutiques(dto.getMaxBoutiques());
        licence.setModulesActifs(dto.getModules());
        licence.setEssai(Boolean.FALSE);
        licence.setCreatedBy(actorUsername);
        licence.setCreatedAt(new Date());
        Licence saved = licenceRepository.save(licence);

        saved.setCle(licenceKeyService.genererCle(saved));
        saved = licenceRepository.save(saved);

        auditLogService.log("LICENCE_GENEREE", "Compagnie", compagnie.getId(),
                "Licence generee pour " + compagnie.getNom() + " - en attente d'activation par la compagnie");

        return toDTO(saved);
    }

    @Transactional
    public LicenceDTO revoquer(Long compagnieId, String actorUsername) {
        Licence licence = getLatest(compagnieId);
        licence.setStatut(LicenceStatut.REVOQUEE);
        licence.setRevokedBy(actorUsername);
        licence.setDateRevocation(new Date());
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_REVOQUEE", "Compagnie", compagnieId, "Licence revoquee");
        return dto;
    }

    @Transactional
    public LicenceDTO suspendre(Long compagnieId, String actorUsername) {
        Licence licence = getLatest(compagnieId);
        licence.setStatut(LicenceStatut.SUSPENDUE);
        licence.setRevokedBy(actorUsername);
        licence.setDateRevocation(new Date());
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_SUSPENDUE", "Compagnie", compagnieId, "Licence suspendue");
        return dto;
    }

    @Transactional
    public LicenceDTO reactiver(Long compagnieId) {
        Licence licence = getLatest(compagnieId);
        if (licence.getStatut() != LicenceStatut.SUSPENDUE) {
            throw new BadRequestException(
                    "Seule une licence suspendue peut etre reactivee ainsi (une licence generee doit etre activee par la compagnie via sa cle)");
        }
        licence.setStatut(LicenceStatut.ACTIVE);
        licence.setRevokedBy(null);
        licence.setDateRevocation(null);
        LicenceDTO dto = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_REACTIVEE", "Compagnie", compagnieId, "Licence reactivee");
        return dto;
    }

    /** Vue de gestion (SUPER_ADMIN/SYSTEM_ADMIN) : la ligne la plus recente, quel que soit son statut. */
    @Transactional(readOnly = true)
    public LicenceDTO getByCompagnie(Long compagnieId) {
        return toDTO(getLatest(compagnieId));
    }

    // ===================== Application (microservice-produits) =====================

    /** Consomme par LicenceInternalController : uniquement la licence ACTIVE la plus recente, pas la derniere generee. */
    @Transactional(readOnly = true)
    public LicenceStatutDTO getStatutInterne(Long compagnieId) {
        Licence licence = getCurrentActive(compagnieId);
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
        Licence licence = getCurrentActive(compagnieId);
        long actuel = personneRepository.countByCompagnie_Id(compagnieId);
        if (licence.getMaxUtilisateurs() != null && actuel >= licence.getMaxUtilisateurs()) {
            throw new ConflictException("Quota d'utilisateurs atteint pour cette compagnie (max " + licence.getMaxUtilisateurs() + ")");
        }
    }

    // ===================== Self-service COMPANY_ADMIN =====================

    @Transactional
    public LicenceDTO activerParCle(String username, ActiverLicenceDTO dto) {
        if (dto.getCle() == null || dto.getCle().isBlank()) {
            throw new BadRequestException("La cle de licence est obligatoire");
        }
        Compagnie compagnie = compagnieDeLutilisateur(username);

        Map<String, Object> claims;
        try {
            claims = licenceKeyService.verifierCle(dto.getCle());
        } catch (Exception e) {
            throw new BadRequestException("Cle de licence invalide");
        }

        Object licenceIdClaim = claims.get("licenceId");
        if (licenceIdClaim == null) {
            throw new BadRequestException("Cle de licence invalide");
        }
        Long licenceId = Long.valueOf(licenceIdClaim.toString());
        Licence licence = licenceRepository.findById(licenceId)
                .orElseThrow(() -> new BadRequestException("Cle de licence invalide"));

        if (!licence.getCompagnie().getId().equals(compagnie.getId())) {
            throw new BadRequestException("Cette cle ne correspond pas a votre compagnie");
        }
        if (licence.getStatut() != LicenceStatut.GENEREE) {
            throw new BadRequestException("Cette cle a deja ete utilisee ou n'est plus valide");
        }

        licence.setStatut(LicenceStatut.ACTIVE);
        licence.setDateDebut(new Date());
        LicenceDTO result = toDTO(licenceRepository.save(licence));
        auditLogService.log("LICENCE_ACTIVEE", "Compagnie", compagnie.getId(),
                "Licence activee par " + username + " pour " + compagnie.getNom());
        return result;
    }

    @Transactional
    public LicenceDTO demarrerEssai(String username) {
        Compagnie compagnie = compagnieDeLutilisateur(username);

        LicenceSettings settings = licenceSettingsService.getOrCreate();
        if (!Boolean.TRUE.equals(settings.getEssaiActif())) {
            throw new BadRequestException("L'essai gratuit n'est pas disponible actuellement");
        }
        if (licenceRepository.existsByCompagnie_IdAndEssaiTrue(compagnie.getId())) {
            throw new ConflictException("Un essai gratuit a deja ete utilise pour cette compagnie");
        }

        Date now = new Date();
        Date expiration = java.util.Date.from(
                now.toInstant().plus(settings.getEssaiDureeJours(), java.time.temporal.ChronoUnit.DAYS));

        Licence licence = new Licence();
        licence.setCompagnie(compagnie);
        licence.setStatut(LicenceStatut.ACTIVE);
        licence.setDateDebut(now);
        licence.setDateExpiration(expiration);
        licence.setMaxUtilisateurs(settings.getEssaiMaxUtilisateurs());
        licence.setMaxBoutiques(settings.getEssaiMaxBoutiques());
        licence.setModulesActifs(filtrerModulesApplicables(settings.getEssaiModules(), compagnie));
        licence.setEssai(Boolean.TRUE);
        licence.setCreatedBy(username);
        licence.setCreatedAt(now);
        Licence saved = licenceRepository.save(licence);
        saved.setCle(licenceKeyService.genererCle(saved));
        saved = licenceRepository.save(saved);

        auditLogService.log("LICENCE_ESSAI_DEMARRE", "Compagnie", compagnie.getId(),
                "Essai gratuit demarre par " + username);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public LicenceMineDTO getMine(String username) {
        Compagnie compagnie = compagnieDeLutilisateur(username);

        LicenceMineDTO result = new LicenceMineDTO();
        licenceRepository.findFirstByCompagnie_IdAndStatutOrderByCreatedAtDesc(compagnie.getId(), LicenceStatut.ACTIVE)
                .ifPresent(l -> result.setActive(toDTO(l)));
        licenceRepository.findFirstByCompagnie_IdAndStatutOrderByCreatedAtDesc(compagnie.getId(), LicenceStatut.GENEREE)
                .ifPresent(l -> result.setPending(toDTO(l)));

        LicenceSettings settings = licenceSettingsService.getOrCreate();
        boolean essaiDejaUtilise = licenceRepository.existsByCompagnie_IdAndEssaiTrue(compagnie.getId());
        result.setEssaiDisponible(Boolean.TRUE.equals(settings.getEssaiActif()) && !essaiDejaUtilise);

        return result;
    }

    @Transactional(readOnly = true)
    public List<LicenceDTO> getHistorique(String username) {
        Compagnie compagnie = compagnieDeLutilisateur(username);
        return licenceRepository.findAllByCompagnie_IdOrderByCreatedAtDesc(compagnie.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ===================== Aides internes =====================

    private Compagnie compagnieDeLutilisateur(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        return personne.getCompagnie();
    }

    private void validerModules(java.util.Set<String> modules, Compagnie compagnie) {
        if (modules == null) {
            return;
        }
        modules.forEach(m -> {
            ModuleLicence module;
            try {
                module = ModuleLicence.fromCode(m);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Module de licence inconnu : " + m);
            }
            if (!ModulesParTypeCommerce.estAutorise(compagnie.getTypeCommerce(), module)) {
                throw new BadRequestException("Le module '" + m
                        + "' n'est pas applicable au type de commerce de cette compagnie ("
                        + compagnie.getTypeCommerce() + ")");
            }
        });
    }

    /** Filtre silencieusement (sans exception) les modules d'essai non applicables au type de commerce. */
    private java.util.Set<String> filtrerModulesApplicables(java.util.Set<String> modules, Compagnie compagnie) {
        if (modules == null) {
            return new java.util.HashSet<>();
        }
        return modules.stream()
                .filter(m -> {
                    try {
                        return ModulesParTypeCommerce.estAutorise(
                                compagnie.getTypeCommerce(), ModuleLicence.fromCode(m));
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

    /** Catalogue de modules applicable au type de commerce d'une compagnie (formulaire de licence). */
    @Transactional(readOnly = true)
    public List<String> getModulesDisponibles(Long compagnieId) {
        Compagnie compagnie = compagnieRepository.findById(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + compagnieId));
        return ModulesParTypeCommerce.pour(compagnie.getTypeCommerce()).stream()
                .map(ModuleLicence::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Modules actifs de la licence de la compagnie de l'utilisateur courant, pour le gating du
     * sidebar. Retourne null si l'utilisateur n'est rattache a aucune compagnie (SUPER_ADMIN/
     * SYSTEM_ADMIN) - a interpreter cote frontend comme "aucune restriction de licence".
     */
    @Transactional(readOnly = true)
    public java.util.Set<String> getModulesActifsPourUtilisateurCourant(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            return null;
        }
        return licenceRepository.findFirstByCompagnie_IdAndStatutOrderByCreatedAtDesc(
                personne.getCompagnie().getId(), LicenceStatut.ACTIVE)
                .map(Licence::getModulesActifs)
                .orElse(java.util.Collections.emptySet());
    }

    /** SYSTEM_ADMIN ne peut agir que sur les compagnies qu'il a lui-meme creees ; SUPER_ADMIN sans restriction. */
    private void checkOwnership(Compagnie compagnie, String actorUsername) {
        Personne actor = personneRepository.findByUserName(actorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + actorUsername));
        boolean isSuperAdmin = actor.getRoleid() != null && RoleNames.SUPER_ADMIN.equals(actor.getRoleid().getName());
        if (!isSuperAdmin && (compagnie.getCreatedBy() == null || !compagnie.getCreatedBy().equals(actorUsername))) {
            throw new AccessDeniedException("Vous ne pouvez generer une licence que pour une compagnie que vous avez creee");
        }
    }

    /** Gestion admin : la ligne la plus recente, quel que soit son statut. */
    private Licence getLatest(Long compagnieId) {
        return licenceRepository.findFirstByCompagnie_IdOrderByCreatedAtDesc(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune licence pour cette compagnie : " + compagnieId));
    }

    /** Application reelle : la licence ACTIVE la plus recente uniquement. */
    private Licence getCurrentActive(Long compagnieId) {
        return licenceRepository.findFirstByCompagnie_IdAndStatutOrderByCreatedAtDesc(compagnieId, LicenceStatut.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune licence active pour cette compagnie : " + compagnieId));
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
        dto.setEssai(licence.getEssai());
        return dto;
    }
}
