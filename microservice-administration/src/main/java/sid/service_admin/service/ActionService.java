package sid.service_admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Action;
import sid.service_admin.model.Permission;
import sid.service_admin.repository.ActionRepository;
import sid.service_admin.repository.PermissionRepository;

/**
 * Catalogue global des Actions grantables sur un menu (voir Action.java).
 * Reserve SUPER_ADMIN/SYSTEM_ADMIN (verifie au niveau du controleur) - une
 * compagnie choisit parmi les Actions existantes, n'en cree pas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {

    private final ActionRepository repository;
    private final PermissionRepository permissionRepository;

    /**
     * Libelles des 5 actions historiques (anciennement l'enum Java
     * OperationType) - identiques a LIBELLE_ACTION cote frontend
     * (user-profil.component.ts).
     */
    private static final java.util.Map<String, String> LIBELLES_PAR_DEFAUT = java.util.Map.of(
            "READ", "Voir",
            "WRITE", "Ajouter",
            "UPDATE", "Modifier",
            "DELETE", "Supprimer",
            "PRINT", "Imprimer");

    @Transactional(readOnly = true)
    public List<Action> findAll() {
        return repository.findAllByOrderByLibelleAsc();
    }

    @Transactional(readOnly = true)
    public List<Action> findByCodes(List<String> codes) {
        return codes.stream()
                .map(code -> repository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Action non trouvee : " + code)))
                .toList();
    }

    /**
     * Au demarrage (voir SuperAdminBootstrap) : seme les 5 Actions
     * historiques si absentes (PRINT y compris - une vraie table, plus un
     * ENUM MySQL natif, le bug de desynchronisation ne peut plus se
     * reproduire), puis relie chaque Permission de menu deja creee via
     * l'ancien enum OperationType a l'Action correspondante. Idempotent :
     * ne modifie jamais une Permission deja reliee a une Action.
     */
    @Transactional
    public void seedActionsEtBackfillPermissions() {
        LIBELLES_PAR_DEFAUT.forEach((code, libelle) -> {
            if (repository.findByCode(code).isEmpty()) {
                repository.save(new Action(code, libelle));
            }
        });

        List<Permission> aRelier = permissionRepository.findByMenuIsNotNullAndActionIsNullAndOperationTypeIsNotNull();
        if (aRelier.isEmpty()) {
            return;
        }
        log.info("Rattachement de {} permissions de menu (ancien enum OperationType) a leur Action.", aRelier.size());
        aRelier.forEach(permission -> {
            Action action = repository.findByCode(permission.getOperationType().name()).orElse(null);
            if (action != null) {
                permission.setAction(action);
                permissionRepository.save(permission);
            }
        });
    }

    @Transactional
    public Action create(String code, String libelle, String description) {
        if (code == null || code.isBlank() || libelle == null || libelle.isBlank()) {
            throw new BadRequestException("Code et libelle sont obligatoires");
        }
        String codeNormalise = code.trim().toUpperCase();
        if (repository.findByCode(codeNormalise).isPresent()) {
            throw new BadRequestException("Une action avec ce code existe deja : " + codeNormalise);
        }
        Action action = new Action(codeNormalise, libelle.trim());
        action.setDescription(description);
        return repository.save(action);
    }

    @Transactional
    public void deleteById(Long id) {
        Action action = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action non trouvee : " + id));
        repository.delete(action);
    }
}
