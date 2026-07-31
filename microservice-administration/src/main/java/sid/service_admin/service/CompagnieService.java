package sid.service_admin.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.CompagnieDTO;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateCompagnieResultDTO;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.dto.ResetPasswordResultDTO;
import sid.service_admin.dto.UpdateCompagnieDTO;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.enums.TypeCommerce;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.security.RoleNames;

/**
 * Creation/gestion des compagnies. La creation d'une compagnie cree aussi
 * son administrateur compagnie, dans la meme transaction (pas de compagnie
 * orpheline sans admin).
 */
@Service
public class CompagnieService {

    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final AdminAccountService adminAccountService;
    private final AuditLogService auditLogService;
    private final SecuriteService securiteService;

    public CompagnieService(CompagnieRepository compagnieRepository, PersonneRepository personneRepository,
            AdminAccountService adminAccountService, AuditLogService auditLogService,
            SecuriteService securiteService) {
        this.compagnieRepository = compagnieRepository;
        this.personneRepository = personneRepository;
        this.adminAccountService = adminAccountService;
        this.auditLogService = auditLogService;
        this.securiteService = securiteService;
    }

    @Transactional
    public CreateCompagnieResultDTO createCompagnieWithAdmin(CreateCompagnieDTO dto, String createdBy) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BadRequestException("Le nom de la compagnie est obligatoire");
        }
        if (compagnieRepository.existsByNom(dto.getNom())) {
            throw new ConflictException("Une compagnie avec ce nom existe deja : " + dto.getNom());
        }

        Compagnie compagnie = new Compagnie(dto.getNom(), dto.getTypeCommerce());
        compagnie.setAdresse(dto.getAdresse());
        compagnie.setTel(dto.getTel());
        compagnie.setEmail(dto.getEmail());
        compagnie.setCreatedBy(createdBy);
        Compagnie saved = compagnieRepository.save(compagnie);

        // Cree l'admin compagnie dans la meme transaction : si ca echoue, la
        // compagnie ne doit pas rester orpheline sans administrateur.
        AccountCreationResult adminResult = adminAccountService.createCompanyAdmin(saved, dto, createdBy);

        // Sans ceci, l'admin fraichement cree n'a aucun module/menu assigne et son
        // sidebar reste vide (voir ModulesParTypeCommerce pour la liste applicable
        // au type de commerce de la compagnie).
        Personne admin = personneRepository.findByUserName(adminResult.getUser().getUserName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrateur compagnie introuvable juste apres sa creation : " + adminResult.getUser().getUserName()));
        provisionerModulesEtMenus(admin, saved.getTypeCommerce());

        auditLogService.log("COMPAGNIE_CREEE", "Compagnie", saved.getId(), "Compagnie creee : " + saved.getNom());

        CreateCompagnieResultDTO result = new CreateCompagnieResultDTO();
        result.setCompagnie(toDTO(saved));
        result.setAdmin(adminResult.getUser());
        result.setGeneratedAdminPassword(adminResult.getGeneratedPassword());
        return result;
    }

    @Transactional(readOnly = true)
    public List<CompagnieDTO> listAll() {
        return compagnieRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompagnieDTO getById(Long id) {
        return toDTO(compagnieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + id)));
    }

    @Transactional(readOnly = true)
    public CompagnieDTO getOwn(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        return toDTO(personne.getCompagnie());
    }

    /**
     * Reserve SUPER_ADMIN/SYSTEM_ADMIN : peut aussi changer le type de
     * commerce. Le statut actif/inactif ne passe plus par ici : voir
     * setActive() (motif obligatoire, journalise). Meme regle de propriete
     * que pour les licences : SYSTEM_ADMIN uniquement sur les compagnies
     * qu'il a lui-meme creees, SUPER_ADMIN sans restriction.
     */
    @Transactional
    public CompagnieDTO update(Long id, UpdateCompagnieDTO dto, String actorUsername) {
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + id));
        checkOwnership(compagnie, actorUsername);

        if (dto.getTypeCommerce() != null) {
            compagnie.setTypeCommerce(dto.getTypeCommerce());
        }
        applySelfServiceFields(compagnie, dto);

        return toDTO(compagnieRepository.save(compagnie));
    }

    /**
     * Active/desactive une compagnie - motif obligatoire (trace d'audit),
     * meme regle de propriete que les autres actions sur compagnie.
     */
    @Transactional
    public CompagnieDTO setActive(Long id, boolean actif, String motif, String actorUsername) {
        if (motif == null || motif.isBlank()) {
            throw new BadRequestException("Un motif est obligatoire pour activer/desactiver une compagnie");
        }
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + id));
        checkOwnership(compagnie, actorUsername);

        compagnie.setActif(actif);
        Compagnie saved = compagnieRepository.save(compagnie);
        auditLogService.log(actif ? "COMPAGNIE_ACTIVEE" : "COMPAGNIE_DESACTIVEE", "Compagnie", saved.getId(),
                "Compagnie " + saved.getNom() + " - motif : " + motif);
        return toDTO(saved);
    }

    /**
     * Assigne a l'administrateur nouvellement cree tous les modules/menus
     * applicables au type de commerce de sa compagnie (voir ModulesParTypeCommerce),
     * pour que son sidebar ne soit pas vide des la creation.
     */
    private void provisionerModulesEtMenus(Personne admin, TypeCommerce typeCommerce) {
        securiteService.provisionerModulesEtMenusPourTypeCommerce(admin, typeCommerce);
    }

    /** SYSTEM_ADMIN ne peut agir que sur les compagnies qu'il a lui-meme creees ; SUPER_ADMIN sans restriction. */
    private void checkOwnership(Compagnie compagnie, String actorUsername) {
        Personne actor = personneRepository.findByUserName(actorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + actorUsername));
        boolean isSuperAdmin = actor.getRoleid() != null && RoleNames.SUPER_ADMIN.equals(actor.getRoleid().getName());
        if (!isSuperAdmin && (compagnie.getCreatedBy() == null || !compagnie.getCreatedBy().equals(actorUsername))) {
            throw new AccessDeniedException("Vous ne pouvez agir que sur une compagnie que vous avez creee");
        }
    }

    /**
     * Libre-service : l'administrateur de la compagnie corrige/complete les
     * informations que l'administrateur systeme qui a cree la compagnie ne
     * connaissait pas (raison sociale exacte, mentions legales, coordonnees
     * utilisees sur les tickets/factures...). Volontairement plus restreint
     * que update() : ni le type de commerce ni le statut actif/inactif ne
     * sont modifiables ici, ce sont des decisions administratives.
     */
    @Transactional
    public CompagnieDTO updateOwn(String username, UpdateCompagnieDTO dto) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        Compagnie compagnie = compagnieRepository.findById(personne.getCompagnie().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee"));

        applySelfServiceFields(compagnie, dto);

        return toDTO(compagnieRepository.save(compagnie));
    }

    private void applySelfServiceFields(Compagnie compagnie, UpdateCompagnieDTO dto) {
        if (dto.getNom() != null) {
            compagnie.setNom(dto.getNom());
        }
        if (dto.getAdresse() != null) {
            compagnie.setAdresse(dto.getAdresse());
        }
        if (dto.getTel() != null) {
            compagnie.setTel(dto.getTel());
        }
        if (dto.getEmail() != null) {
            compagnie.setEmail(dto.getEmail());
        }
        if (dto.getCapital() != null) {
            compagnie.setCapital(dto.getCapital());
        }
        if (dto.getNumeroContribuable() != null) {
            compagnie.setNumeroContribuable(dto.getNumeroContribuable());
        }
        if (dto.getNui() != null) {
            compagnie.setNui(dto.getNui());
        }
        if (dto.getRccm() != null) {
            compagnie.setRccm(dto.getRccm());
        }
        if (dto.getSiteWeb() != null) {
            compagnie.setSiteWeb(dto.getSiteWeb());
        }
        if (dto.getDirecteur() != null) {
            compagnie.setDirecteur(dto.getDirecteur());
        }
        if (dto.getLogoChemin() != null) {
            compagnie.setLogoChemin(dto.getLogoChemin());
        }
        if (dto.getBp() != null) {
            compagnie.setBp(dto.getBp());
        }
        if (dto.getQuartier() != null) {
            compagnie.setQuartier(dto.getQuartier());
        }
        if (dto.getVille() != null) {
            compagnie.setVille(dto.getVille());
        }
    }

    /**
     * Genere un nouveau mot de passe pour l'admin d'une compagnie (cas ou le
     * mot de passe genere a la creation n'a pas ete note a temps). Meme regle
     * de propriete que pour les licences : SYSTEM_ADMIN uniquement sur les
     * compagnies qu'il a lui-meme creees, SUPER_ADMIN sans restriction.
     */
    @Transactional
    public ResetPasswordResultDTO resetAdminPassword(Long compagnieId, String actorUsername) {
        Compagnie compagnie = compagnieRepository.findById(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + compagnieId));
        checkOwnership(compagnie, actorUsername);

        Personne admin = personneRepository.findByCompagnie_Id(compagnieId).stream()
                .filter(p -> p.getRoleid() != null && RoleNames.COMPANY_ADMIN.equals(p.getRoleid().getName()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Aucun administrateur trouve pour cette compagnie"));

        String generatedPassword = adminAccountService.resetPassword(admin);
        auditLogService.log("COMPAGNIE_ADMIN_MOT_DE_PASSE_REINITIALISE", "Personne", admin.getId(),
                "Mot de passe reinitialise pour l'admin de " + compagnie.getNom());
        return new ResetPasswordResultDTO(generatedPassword);
    }

    /**
     * (Re)accorde a l'administrateur d'une compagnie tous les modules/menus
     * applicables a son type de commerce - utile pour rattraper une compagnie
     * creee avant l'ajout de nouveaux menus au catalogue (voir InitiationDb),
     * sans devoir connaitre l'identifiant de l'administrateur.
     */
    @Transactional
    public void resynchroniserModulesAdmin(Long compagnieId, String actorUsername) {
        Compagnie compagnie = compagnieRepository.findById(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + compagnieId));
        checkOwnership(compagnie, actorUsername);

        Personne admin = personneRepository.findByCompagnie_Id(compagnieId).stream()
                .filter(p -> p.getRoleid() != null && RoleNames.COMPANY_ADMIN.equals(p.getRoleid().getName()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Aucun administrateur trouve pour cette compagnie"));

        securiteService.provisionerModulesEtMenusPourTypeCommerce(admin, compagnie.getTypeCommerce());
        auditLogService.log("COMPAGNIE_MODULES_RESYNCHRONISES", "Compagnie", compagnie.getId(),
                "Modules/menus resynchronises pour l'admin de " + compagnie.getNom());
    }

    private CompagnieDTO toDTO(Compagnie compagnie) {
        CompagnieDTO dto = new CompagnieDTO();
        BeanUtils.copyProperties(compagnie, dto);
        personneRepository.findByCompagnie_Id(compagnie.getId()).stream()
                .filter(p -> p.getRoleid() != null && RoleNames.COMPANY_ADMIN.equals(p.getRoleid().getName()))
                .findFirst()
                .ifPresent(admin -> {
                    dto.setAdminUserName(admin.getUserName());
                    dto.setAdminNom((nullToEmpty(admin.getFirstName()) + " " + nullToEmpty(admin.getLastname())).trim());
                    dto.setAdminEmail(admin.getEmail());
                });
        return dto;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
