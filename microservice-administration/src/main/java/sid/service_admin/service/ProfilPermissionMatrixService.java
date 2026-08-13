package sid.service_admin.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.MenuActionsDTO;
import sid.service_admin.dto.TogglePermissionRequest;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Action;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Profil;
import sid.service_admin.model.ProfilPermissions;
import sid.service_admin.repository.ActionRepository;
import sid.service_admin.repository.MenuRepository;
import sid.service_admin.repository.PermissionRepository;
import sid.service_admin.repository.ProfilPermissionsRepository;
import sid.service_admin.repository.ProfilRepository;
import sid.service_admin.security.TenantContext;

/**
 * Administration de la matrice Profil x Menu x Action : c'est le Profil qui
 * porte les droits d'acces (le Role reste une simple etiquette/categorie).
 * Un utilisateur herite des droits du profil qui lui est assigne
 * (Personne.profilid) - voir CLAUDE.md / synthese du module securite.
 */
@Service
@RequiredArgsConstructor
public class ProfilPermissionMatrixService {

    private final ProfilRepository profilRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final ProfilPermissionsRepository profilPermissionsRepository;
    private final ActionRepository actionRepository;
    private final TenantContext tenantContext;

    /**
     * Un profil est une politique de droits propre a une compagnie : un
     * COMPANY_ADMIN ne peut jamais consulter/modifier la matrice d'un profil
     * appartenant a une autre compagnie. Sans effet pour un compte sans
     * compagnie (SUPER_ADMIN/SYSTEM_ADMIN, supervision globale).
     */
    private void verifierAppartenance(Profil profil) {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId != null
                && (profil.getCompagnie() == null || !compagnieId.equals(profil.getCompagnie().getId()))) {
            throw new AccessDeniedException("Ce profil n'appartient pas a votre compagnie");
        }
    }

    /**
     * Matrice complete : tous les menus du catalogue, avec les actions
     * actuellement accordees au profil (ensemble vide si aucune).
     */
    @Transactional(readOnly = true)
    public List<MenuActionsDTO> getMatrice(Long profilId) {
        Profil profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + profilId));
        verifierAppartenance(profil);

        Map<Long, Set<String>> actionsParMenu = profilPermissionsRepository.findByProfil(profil).stream()
                .map(ProfilPermissions::getPermission)
                .filter(p -> p.getMenu() != null && p.getAction() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getMenu().getId(),
                        Collectors.mapping(p -> p.getAction().getCode(), Collectors.toSet())));

        return menuRepository.findAll().stream()
                .filter(m -> m.getModuleid() != null)
                .sorted(Comparator.comparing((Menu m) -> m.getModuleid().getCode())
                        .thenComparing(Menu::getCode))
                .map(m -> new MenuActionsDTO(
                        m.getId(), m.getCode(), m.getDescription(),
                        m.getModuleid().getId(), m.getModuleid().getCode(), m.getModuleid().getDescription(),
                        actionsParMenu.getOrDefault(m.getId(), Set.of())))
                .collect(Collectors.toList());
    }

    /**
     * Accorde ou retire une action (menu, action) pour un profil. Cree la
     * Permission correspondante a la demande si elle n'existe pas encore
     * (une Permission = un couple (menu, action) unique) - c'est ce qui
     * permet d'"attacher une action a un menu" simplement en la cochant
     * dans la matrice, une fois l'Action elle-meme creee (voir ActionService,
     * reserve SUPER_ADMIN/SYSTEM_ADMIN).
     */
    @Transactional
    public void toggle(TogglePermissionRequest request) {
        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + request.getProfilId()));
        verifierAppartenance(profil);
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu non trouve : " + request.getMenuId()));
        Action action = actionRepository.findByCode(request.getAction())
                .orElseThrow(() -> new ResourceNotFoundException("Action non trouvee : " + request.getAction()));

        Permission permission = permissionRepository.findByMenu_IdAndAction_Id(menu.getId(), action.getId())
                .orElseGet(() -> permissionRepository.save(new Permission(menu, action)));

        boolean dejaAccorde = profilPermissionsRepository.existsByProfilAndPermission(profil, permission);

        if (request.isGranted() && !dejaAccorde) {
            profilPermissionsRepository.save(new ProfilPermissions(profil, permission));
        } else if (!request.isGranted() && dejaAccorde) {
            profilPermissionsRepository.deleteByProfilAndPermission(profil, permission);
        }
    }

    /** Menus visibles pour un profil (union des menus ou il a au moins une action). */
    @Transactional(readOnly = true)
    public List<Menu> getMenusVisibles(Long profilId) {
        Profil profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + profilId));
        verifierAppartenance(profil);
        return profilPermissionsRepository.findMenusVisiblesByProfil(profilId);
    }

    /** true si ce profil a deja au moins une permission accordee (configuree manuellement ou deja semee). */
    @Transactional(readOnly = true)
    public boolean aDejaDesPermissions(Profil profil) {
        return !profilPermissionsRepository.findByProfil(profil).isEmpty();
    }

    /**
     * Accorde d'un coup un ensemble de menus/actions par defaut a un profil -
     * utilise a la creation d'une compagnie (voir
     * UserService#seedProfilsParDefautPourCompagnie) pour qu'un profil
     * fraichement cree ne soit pas totalement vide dans la sidebar tant
     * qu'un administrateur n'a pas configure la matrice manuellement (voir
     * ecran Permission). Idempotent : ignore les couples deja accordes, et
     * ignore silencieusement un code menu inconnu (ne doit jamais faire
     * echouer la creation de la compagnie).
     */
    @Transactional
    public void seedPermissionsParDefaut(Profil profil, List<String> menuCodes, List<Action> actions) {
        for (String code : menuCodes) {
            Menu menu = menuRepository.findByCode(code);
            if (menu == null) {
                continue;
            }
            for (Action action : actions) {
                Permission permission = permissionRepository.findByMenu_IdAndAction_Id(menu.getId(), action.getId())
                        .orElseGet(() -> permissionRepository.save(new Permission(menu, action)));
                if (!profilPermissionsRepository.existsByProfilAndPermission(profil, permission)) {
                    profilPermissionsRepository.save(new ProfilPermissions(profil, permission));
                }
            }
        }
    }

    /**
     * Duplique un profil existant (meme compagnie) : nouveau Profil avec le
     * meme jeu de droits (copie de toutes les lignes ProfilPermissions) -
     * permet de partir d'un profil deja configure (ex: "CAISSIER") pour en
     * creer une variante ("CAISSIER_CODEBARRE") sans reconfigurer la matrice
     * a zero.
     */
    @Transactional
    public Profil dupliquer(Long profilSourceId, String nouveauCode, String nouvelleDescription) {
        Profil source = profilRepository.findById(profilSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + profilSourceId));
        verifierAppartenance(source);
        if (nouveauCode == null || nouveauCode.isBlank()) {
            throw new sid.service_admin.exceptions.BadRequestException("Le code du nouveau profil est obligatoire");
        }
        if (source.getCompagnie() != null
                && profilRepository.findByCodeAndCompagnie_Id(nouveauCode, source.getCompagnie().getId()) != null) {
            throw new sid.service_admin.exceptions.ConflictException(
                    "Un profil avec le code '" + nouveauCode + "' existe deja pour votre compagnie");
        }

        Profil copie = new Profil(nouveauCode);
        copie.setDescription(nouvelleDescription != null ? nouvelleDescription : source.getDescription());
        copie.setStatut("ACTIF");
        copie.setAddDate(new java.util.Date());
        copie.setCompagnie(source.getCompagnie());
        Profil copieSauvee = profilRepository.save(copie);

        for (ProfilPermissions droit : profilPermissionsRepository.findByProfil(source)) {
            profilPermissionsRepository.save(new ProfilPermissions(copieSauvee, droit.getPermission()));
        }

        return copieSauvee;
    }

    /**
     * Catalogue complet Module -> Menu, independant de tout profil : vue de
     * reference pour un administrateur (ecran "Module Securite"), pas une
     * matrice de droits accordes. Les actions listees sont l'ensemble
     * complet du catalogue Action (voir Action.java), a titre informatif -
     * pas des droits accordes a qui que ce soit.
     */
    @Transactional(readOnly = true)
    public List<MenuActionsDTO> getCatalogue() {
        java.util.Set<String> toutesLesActions = actionRepository.findAll().stream()
                .map(Action::getCode)
                .collect(Collectors.toSet());

        return menuRepository.findAll().stream()
                .filter(m -> m.getModuleid() != null)
                .sorted(Comparator.comparing((Menu m) -> m.getModuleid().getCode())
                        .thenComparing(Menu::getCode))
                .map(m -> new MenuActionsDTO(
                        m.getId(), m.getCode(), m.getDescription(),
                        m.getModuleid().getId(), m.getModuleid().getCode(), m.getModuleid().getDescription(),
                        toutesLesActions))
                .collect(Collectors.toList());
    }
}
