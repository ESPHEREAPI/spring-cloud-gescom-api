package sid.service_admin.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateSystemAdminDTO;
import sid.service_admin.dto.ResetPasswordResultDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.mapper.MapperDtoImpl;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Roles;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.RoleRepository;
import sid.service_admin.security.RoleNames;
import sid.service_admin.security.SecurePasswordGenerator;

/**
 * Creation des comptes de la hierarchie multi-compagnies (admin systeme,
 * admin compagnie). Utilise par SuperAdminBootstrap, SystemAdminController
 * et CompagnieService.
 */
@Service
public class AdminAccountService {

    private final PersonneRepository personneRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperDtoImpl mapper;
    private final AuditLogService auditLogService;

    public AdminAccountService(PersonneRepository personneRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, MapperDtoImpl mapper, AuditLogService auditLogService) {
        this.personneRepository = personneRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    public AccountCreationResult createSuperAdmin(String userName, String password) {
        return createAccount(userName, "Super", "Administrateur", null, null, password, RoleNames.SUPER_ADMIN, null, "Bootstrap");
    }

    public AccountCreationResult createSystemAdmin(CreateSystemAdminDTO dto, String createdBy) {
        AccountCreationResult result = createAccount(dto.getUserName(), dto.getFirstName(), dto.getLastname(), dto.getEmail(),
                dto.getTel(), dto.getPassword(), RoleNames.SYSTEM_ADMIN, null, createdBy);
        auditLogService.log("SYSTEM_ADMIN_CREE", "Personne", result.getUser().getId(),
                "Admin systeme cree : " + dto.getUserName());
        return result;
    }

    public AccountCreationResult createCompanyAdmin(Compagnie compagnie, CreateCompagnieDTO dto, String createdBy) {
        return createAccount(dto.getAdminUserName(), dto.getAdminFirstName(), dto.getAdminLastname(),
                dto.getAdminEmail(), null, dto.getAdminPassword(), RoleNames.COMPANY_ADMIN, compagnie, createdBy);
    }

    public List<UserDTO> listByRole(String roleName) {
        return personneRepository.findByRoleid_Name(roleName).stream()
                .map(mapper::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genere un nouveau mot de passe pour un compte existant et le retourne
     * en clair une seule fois (jamais persiste ni logge ailleurs) - pour le
     * cas ou le mot de passe genere a la creation n'a pas ete note a temps.
     */
    public String resetPassword(Personne personne) {
        String generatedPassword = SecurePasswordGenerator.generate();
        personne.setPassword(passwordEncoder.encode(generatedPassword));
        personne.setMustChangePassword(Boolean.TRUE);
        personneRepository.save(personne);
        return generatedPassword;
    }

    public ResetPasswordResultDTO resetSystemAdminPassword(Long personneId) {
        Personne personne = findSystemAdmin(personneId);
        String generatedPassword = resetPassword(personne);
        auditLogService.log("SYSTEM_ADMIN_MOT_DE_PASSE_REINITIALISE", "Personne", personne.getId(),
                "Mot de passe reinitialise pour l'admin systeme " + personne.getUserName());
        return new ResetPasswordResultDTO(generatedPassword);
    }

    /** Activation/desactivation reservee au SUPER_ADMIN (deja impose par SystemAdminController) - motif obligatoire, journalise. */
    public UserDTO setSystemAdminActive(Long personneId, boolean actif, String motif) {
        if (motif == null || motif.isBlank()) {
            throw new BadRequestException("Un motif est obligatoire pour activer/desactiver un administrateur systeme");
        }
        Personne personne = findSystemAdmin(personneId);
        personne.setIsActive(actif);
        Personne saved = personneRepository.save(personne);
        auditLogService.log(actif ? "SYSTEM_ADMIN_ACTIVE" : "SYSTEM_ADMIN_DESACTIVE", "Personne", personne.getId(),
                "Admin systeme " + personne.getUserName() + " - motif : " + motif);
        return mapper.mapToDTO(saved);
    }

    private Personne findSystemAdmin(Long personneId) {
        Personne personne = personneRepository.findById(personneId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur systeme non trouve : " + personneId));
        if (personne.getRoleid() == null || !RoleNames.SYSTEM_ADMIN.equals(personne.getRoleid().getName())) {
            throw new BadRequestException("Ce compte n'est pas un administrateur systeme");
        }
        return personne;
    }

    /** Genere un identifiant lisible (initiale + nom) a partir de l'identite, unique en base (suffixe numerique si besoin). */
    private String generateUsername(String firstName, String lastname) {
        String base = normalizeForUsername(
                (firstName != null && !firstName.isBlank() ? firstName.substring(0, 1) : "") + (lastname != null ? lastname : ""));
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffix = 1;
        while (personneRepository.findByUserName(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String normalizeForUsername(String value) {
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private AccountCreationResult createAccount(String userName, String firstName, String lastname, String email,
            String tel, String rawPassword, String roleName, Compagnie compagnie, String createdBy) {
        String userNameToUse = (userName == null || userName.isBlank())
                ? generateUsername(firstName, lastname)
                : userName;
        if (personneRepository.findByUserName(userNameToUse).isPresent()) {
            throw new ConflictException("Ce nom d'utilisateur est deja utilise : " + userNameToUse);
        }
        if (email != null && !email.isBlank() && personneRepository.existsByEmail(email)) {
            throw new ConflictException("Cet email est deja utilise : " + email);
        }

        Roles role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + roleName + " introuvable - le bootstrap de demarrage ne s'est pas execute correctement"));

        String generatedPassword = null;
        String passwordToUse = rawPassword;
        if (passwordToUse == null || passwordToUse.isBlank()) {
            generatedPassword = SecurePasswordGenerator.generate();
            passwordToUse = generatedPassword;
        }

        Personne personne = new Personne();
        personne.setUserName(userNameToUse);
        personne.setFirstName(firstName);
        personne.setLastname(lastname);
        personne.setEmail(email);
        personne.setTel(tel);
        personne.setPassword(passwordEncoder.encode(passwordToUse));
        personne.setRoleid(role);
        personne.setCompagnie(compagnie);
        personne.setBoutique(null);
        personne.setIsActive(Boolean.TRUE);
        // Mot de passe assigne (fourni par l'admin ou genere), pas choisi par le
        // titulaire du compte - changement force a la premiere connexion.
        personne.setMustChangePassword(Boolean.TRUE);
        personne.setCreatedBy(createdBy == null ? "Systeme" : createdBy);
        personne.setCreatedAt(new Date());

        Personne saved = personneRepository.save(personne);
        return new AccountCreationResult(mapper.mapToDTO(saved), generatedPassword);
    }
}
