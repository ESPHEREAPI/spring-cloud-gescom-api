package com.mproduits.security;

import com.mproduits.enums.ExceptionType;
import com.mproduits.model.Permission;
import com.mproduits.model.Personne;
import com.mproduits.model.PersonnePermissionException;
import com.mproduits.repositories.PersonnePermissionExceptionRepositories;
import com.mproduits.repositories.ProfilPermissionsRepositories;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Reimplementation locale de sid.service_admin.service.PersonnePermissionService
 * (microservice-administration) : droits effectifs d'une Personne (Profil en
 * direct + exceptions posees sur cette Personne), traduits en authorities
 * Spring Security PERM_&lt;CODE_MENU&gt;_&lt;CODE_ACTION&gt; (ex.
 * PERM_FACTURE_VALIDER) - utilise par JwtAuthFilter a chaque requete, comme
 * les authorities ROLE_* deja en place.
 */
@Service
@RequiredArgsConstructor
public class EffectivePermissionService {

    private final ProfilPermissionsRepositories profilPermissionsRepository;
    private final PersonnePermissionExceptionRepositories exceptionRepository;

    public Set<GrantedAuthority> getAuthoritiesEffectives(Personne personne) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (personne.getProfilid() == null) {
            return authorities;
        }

        Map<String, Set<String>> actionsParMenu = new HashMap<>();
        for (Object[] ligne : profilPermissionsRepository.findMenuActionCodesByProfilId(personne.getProfilid().getId())) {
            String menuCode = (String) ligne[0];
            String actionCode = (String) ligne[1];
            actionsParMenu.computeIfAbsent(menuCode, k -> new HashSet<>()).add(actionCode);
        }

        for (PersonnePermissionException exception : exceptionRepository.findByPersonne(personne)) {
            Permission permission = exception.getPermission();
            if (permission == null || permission.getMenu() == null || permission.getAction() == null) {
                continue;
            }
            Set<String> actions = actionsParMenu.computeIfAbsent(permission.getMenu().getCode(), k -> new HashSet<>());
            if (exception.getType() == ExceptionType.GRANT) {
                actions.add(permission.getAction().getCode());
            } else {
                actions.remove(permission.getAction().getCode());
            }
        }

        actionsParMenu.forEach((menuCode, actions) -> actions.forEach(actionCode ->
                authorities.add(new SimpleGrantedAuthority("PERM_" + normaliser(menuCode) + "_" + normaliser(actionCode)))));
        return authorities;
    }

    private static String normaliser(String code) {
        return code.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
    }
}
