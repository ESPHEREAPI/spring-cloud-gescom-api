package sid.service_admin.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.MenuActionsDTO;
import sid.service_admin.dto.PersonneMenuActionsDTO;
import sid.service_admin.enums.ExceptionType;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Action;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Personne;
import sid.service_admin.model.PersonnePermissionException;
import sid.service_admin.repository.ActionRepository;
import sid.service_admin.repository.MenuRepository;
import sid.service_admin.repository.PermissionRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.PersonnePermissionExceptionRepository;
import sid.service_admin.security.TenantContext;

/**
 * Droits effectifs d'un utilisateur precis : ceux herites en direct de son
 * Profil (voir ProfilPermissionMatrixService), ajustes par les exceptions
 * posees sur cette Personne (voir PersonnePermissionException). Le Profil
 * n'est jamais copie/fige a l'assignation - tout recalcul relit le Profil en
 * vigueur, donc une evolution ulterieure du Profil continue de s'appliquer
 * automatiquement a tous ses utilisateurs, sauf sur les couples (menu,
 * action) explicitement exceptes pour cette Personne.
 */
@Service
@RequiredArgsConstructor
public class PersonnePermissionService {

    private final PersonneRepository personneRepository;
    private final MenuRepository menuRepository;
    private final ActionRepository actionRepository;
    private final PermissionRepository permissionRepository;
    private final PersonnePermissionExceptionRepository exceptionRepository;
    private final ProfilPermissionMatrixService profilPermissionMatrixService;
    private final TenantContext tenantContext;

    /**
     * Un COMPANY_ADMIN ne peut jamais consulter/modifier les droits d'une
     * Personne d'une autre compagnie. Sans effet pour un compte sans
     * compagnie (SUPER_ADMIN/SYSTEM_ADMIN, supervision globale).
     */
    private void verifierAppartenance(Personne personne) {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId != null
                && (personne.getCompagnie() == null || !compagnieId.equals(personne.getCompagnie().getId()))) {
            throw new AccessDeniedException("Cet utilisateur n'appartient pas a votre compagnie");
        }
    }

    /**
     * Actions effectives par menu (code de menu -> ensemble de codes
     * d'action), Profil + exceptions confondus - utilise par SecuriteService
     * pour que le menu lateral et les controles fins refletent les
     * exceptions, pas seulement la matrice brute du Profil.
     */
    @Transactional(readOnly = true)
    public Map<Long, Set<String>> getActionsEffectivesParMenuId(Personne personne) {
        Map<Long, Set<String>> resultat = new HashMap<>();
        if (personne == null || personne.getProfilid() == null) {
            return resultat;
        }
        for (MenuActionsDTO ligne : profilPermissionMatrixService.getMatrice(personne.getProfilid().getId())) {
            resultat.put(ligne.getMenuId(), new HashSet<>(ligne.getActions()));
        }
        for (PersonnePermissionException exception : exceptionRepository.findByPersonne(personne)) {
            Permission permission = exception.getPermission();
            if (permission.getMenu() == null || permission.getAction() == null) {
                continue;
            }
            Set<String> actions = resultat.computeIfAbsent(permission.getMenu().getId(), k -> new HashSet<>());
            if (exception.getType() == ExceptionType.GRANT) {
                actions.add(permission.getAction().getCode());
            } else {
                actions.remove(permission.getAction().getCode());
            }
        }
        return resultat;
    }

    /** Menus visibles pour cette Personne (au moins une action effective accordee). */
    @Transactional(readOnly = true)
    public List<Menu> getMenusVisibles(Personne personne) {
        Set<Long> menuIdsVisibles = getActionsEffectivesParMenuId(personne).entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (menuIdsVisibles.isEmpty()) {
            return List.of();
        }
        return menuRepository.findAllById(menuIdsVisibles);
    }

    /**
     * Matrice complete du Profil de cette Personne, chaque cellule marquee
     * herite/exception-ajoutee/exception-retiree - alimente l'ecran de
     * creation/edition d'un utilisateur (voir PersonnePermissionController).
     */
    @Transactional(readOnly = true)
    public List<PersonneMenuActionsDTO> getPermissionsEffectives(Long personneId) {
        Personne personne = personneRepository.findById(personneId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + personneId));
        verifierAppartenance(personne);
        if (personne.getProfilid() == null) {
            return List.of();
        }

        List<MenuActionsDTO> matriceProfil = profilPermissionMatrixService.getMatrice(personne.getProfilid().getId());
        Map<Long, List<PersonnePermissionException>> exceptionsParMenu = exceptionRepository.findByPersonne(personne)
                .stream()
                .filter(e -> e.getPermission().getMenu() != null && e.getPermission().getAction() != null)
                .collect(Collectors.groupingBy(e -> e.getPermission().getMenu().getId()));

        return matriceProfil.stream().map(ligne -> {
            Set<String> herite = ligne.getActions();
            Set<String> effectif = new HashSet<>(herite);
            Set<String> ajoutees = new HashSet<>();
            Set<String> retirees = new HashSet<>();
            for (PersonnePermissionException exception : exceptionsParMenu.getOrDefault(ligne.getMenuId(), List.of())) {
                String code = exception.getPermission().getAction().getCode();
                if (exception.getType() == ExceptionType.GRANT) {
                    effectif.add(code);
                    ajoutees.add(code);
                } else {
                    effectif.remove(code);
                    retirees.add(code);
                }
            }
            return new PersonneMenuActionsDTO(ligne.getMenuId(), ligne.getMenuCode(), ligne.getMenuDescription(),
                    ligne.getModuleId(), ligne.getModuleCode(), ligne.getModuleDescription(),
                    effectif, herite, ajoutees, retirees);
        }).collect(Collectors.toList());
    }

    /**
     * Pose (ou remplace) une exception GRANT/REVOKE pour cette Personne sur
     * le couple (menu, action) - cree la Permission correspondante a la
     * demande si elle n'existe pas encore, comme
     * ProfilPermissionMatrixService.toggle.
     */
    @Transactional
    public void definirException(Long personneId, Long menuId, String actionCode, ExceptionType type, String createdBy) {
        Personne personne = personneRepository.findById(personneId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + personneId));
        verifierAppartenance(personne);
        if (personne.getProfilid() == null) {
            throw new BadRequestException("Cet utilisateur n'a pas de profil : impossible de poser une exception");
        }
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu non trouve : " + menuId));
        Action action = actionRepository.findByCode(actionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Action non trouvee : " + actionCode));

        Permission permission = permissionRepository.findByMenu_IdAndAction_Id(menu.getId(), action.getId())
                .orElseGet(() -> permissionRepository.save(new Permission(menu, action)));

        PersonnePermissionException exception = exceptionRepository.findByPersonneAndPermission(personne, permission)
                .orElse(null);
        if (exception == null) {
            exceptionRepository.save(new PersonnePermissionException(personne, permission, type, createdBy));
        } else {
            exception.setType(type);
            exception.setCreatedBy(createdBy);
            exception.setCreatedAt(new java.util.Date());
            exceptionRepository.save(exception);
        }
    }

    /** Retire une exception : l'utilisateur revient au comportement du Profil pour cette cellule. */
    @Transactional
    public void supprimerException(Long personneId, Long exceptionId) {
        PersonnePermissionException exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Exception non trouvee : " + exceptionId));
        if (!exception.getPersonne().getId().equals(personneId)) {
            throw new BadRequestException("Cette exception n'appartient pas a cet utilisateur");
        }
        verifierAppartenance(exception.getPersonne());
        exceptionRepository.delete(exception);
    }
}
