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
import sid.service_admin.model.Menu;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Profil;
import sid.service_admin.model.ProfilPermissions;
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
                .filter(p -> p.getMenu() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getMenu().getId(),
                        Collectors.mapping(p -> p.getOperationType().name(), Collectors.toSet())));

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
     * Accorde ou retire une action (menu, operation) pour un profil. Cree la
     * Permission correspondante a la demande si elle n'existe pas encore
     * (une Permission = un couple (menu, operationType) unique).
     */
    @Transactional
    public void toggle(TogglePermissionRequest request) {
        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + request.getProfilId()));
        verifierAppartenance(profil);
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu non trouve : " + request.getMenuId()));

        Permission permission = permissionRepository.findByMenu_IdAndOperationType(menu.getId(), request.getAction())
                .orElseGet(() -> permissionRepository.save(new Permission(menu, request.getAction())));

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
        return profilPermissionsRepository.findMenusVisiblesByProfil(profilId);
    }

    /**
     * Catalogue complet Module -> Menu, independant de tout profil : vue de
     * reference pour un administrateur (ecran "Module Securite"), pas une
     * matrice de droits accordes. Les actions listees sont l'ensemble fixe
     * disponible pour tout menu (READ/WRITE/UPDATE/DELETE/PRINT), a titre
     * informatif - pas des droits accordes a qui que ce soit.
     */
    @Transactional(readOnly = true)
    public List<MenuActionsDTO> getCatalogue() {
        java.util.Set<String> toutesLesActions = java.util.Arrays.stream(
                sid.service_admin.enums.OperationType.values())
                .map(Enum::name)
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
