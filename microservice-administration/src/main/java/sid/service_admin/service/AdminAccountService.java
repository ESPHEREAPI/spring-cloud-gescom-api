package sid.service_admin.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateSystemAdminDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
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

    private AccountCreationResult createAccount(String userName, String firstName, String lastname, String email,
            String tel, String rawPassword, String roleName, Compagnie compagnie, String createdBy) {
        if (userName == null || userName.isBlank()) {
            throw new BadRequestException("Le nom d'utilisateur est obligatoire");
        }
        if (personneRepository.findByUserName(userName).isPresent()) {
            throw new ConflictException("Ce nom d'utilisateur est deja utilise : " + userName);
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
        personne.setUserName(userName);
        personne.setFirstName(firstName);
        personne.setLastname(lastname);
        personne.setEmail(email);
        personne.setTel(tel);
        personne.setPassword(passwordEncoder.encode(passwordToUse));
        personne.setRoleid(role);
        personne.setCompagnie(compagnie);
        personne.setBoutique(null);
        personne.setIsActive(Boolean.TRUE);
        personne.setCreatedBy(createdBy == null ? "Systeme" : createdBy);
        personne.setCreatedAt(new Date());

        Personne saved = personneRepository.save(personne);
        return new AccountCreationResult(mapper.mapToDTO(saved), generatedPassword);
    }
}
