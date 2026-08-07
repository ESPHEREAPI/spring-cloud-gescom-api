package sid.service_admin.service;

import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.model.Roles;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.RoleRepository;
import sid.service_admin.security.RoleNames;

/**
 * Au premier demarrage (aucun SUPER_ADMIN en base), seme les roles de la
 * hierarchie et cree le super-administrateur. Remplace l'ancien
 * InitDB.getAdmin()/InitiationDb.getAdmin() (mort, jamais appele, mot de
 * passe code en dur).
 */
@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SuperAdminBootstrap.class);

    private final RoleRepository roleRepository;
    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final AdminAccountService adminAccountService;
    private final UserService userService;

    @Value("${app.superadmin.username:superadmin}")
    private String superAdminUsername;

    /** Optionnel : si non defini (APP_SUPERADMIN_PASSWORD), un mot de passe est genere et logge une seule fois. */
    @Value("${app.superadmin.password:}")
    private String configuredSuperAdminPassword;

    public SuperAdminBootstrap(RoleRepository roleRepository, CompagnieRepository compagnieRepository,
            PersonneRepository personneRepository, AdminAccountService adminAccountService, UserService userService) {
        this.roleRepository = roleRepository;
        this.compagnieRepository = compagnieRepository;
        this.personneRepository = personneRepository;
        this.adminAccountService = adminAccountService;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedHierarchyRoles();
        seedProfilsPourCompagniesExistantes();

        if (personneRepository.findByUserName(superAdminUsername).isPresent()) {
            return;
        }

        String password = (configuredSuperAdminPassword == null || configuredSuperAdminPassword.isBlank())
                ? null
                : configuredSuperAdminPassword;

        AccountCreationResult result = adminAccountService.createSuperAdmin(superAdminUsername, password);

        if (result.getGeneratedPassword() != null) {
            logger.warn("=======================================================================");
            logger.warn(" SUPER-ADMINISTRATEUR CREE - IDENTIFIANT : {}", superAdminUsername);
            logger.warn(" MOT DE PASSE (a noter maintenant, ne sera plus jamais affiche) : {}", result.getGeneratedPassword());
            logger.warn("=======================================================================");
        } else {
            logger.info("Super-administrateur '{}' cree avec le mot de passe fourni via APP_SUPERADMIN_PASSWORD.", superAdminUsername);
        }
    }

    private void seedHierarchyRoles() {
        Stream.of(RoleNames.SUPER_ADMIN, RoleNames.SYSTEM_ADMIN, RoleNames.COMPANY_ADMIN, RoleNames.EMPLOYE)
                .filter(name -> roleRepository.findByName(name).isEmpty())
                .forEach(name -> roleRepository.save(new Roles(name)));
    }

    /**
     * Rattrapage pour les compagnies deja existantes qui n'ont encore aucun
     * profil (creees avant le rattachement compagnie sur Profil, ou dont le
     * semis initial a echoue) - les compagnies creees a partir de maintenant
     * sont semees directement a la creation, voir CompagnieService. Chaque
     * compagnie garde ses propres profils, jamais partages avec une autre
     * (voir UserService.seedProfilsParDefautPourCompagnie).
     */
    private void seedProfilsPourCompagniesExistantes() {
        compagnieRepository.findAll().forEach(userService::seedProfilsParDefautPourCompagnie);
    }
}
