package sid.service_admin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Roles;
import sid.service_admin.repository.AuditAccessGrantRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.RolePermissionsRepositorie;
import sid.service_admin.repository.RoleRepository;

/**
 * Valide le JWT sur chaque requete et peuple le contexte de securite a partir
 * du role/compagnie actuels en base (pas des claims du token), pour qu'un
 * changement de role/retrogradation prenne effet immediatement, sans attendre
 * l'expiration du token deja emis.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PersonneRepository personneRepository;

    @Autowired
    private RolePermissionsRepositorie rolePermissionsRepositorie;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditAccessGrantRepository auditAccessGrantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)) {
                String username = jwtService.getUsername(token);
                personneRepository.findByUserName(username).ifPresent(personne -> authenticate(personne, request));
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(Personne personne, HttpServletRequest request) {
        // compagnieId depuis la base (pas le claim du token), coherent avec le
        // reste de ce filtre : null = administrateur systeme (SUPER_ADMIN/
        // SYSTEM_ADMIN), renseigne = utilisateur/admin de cette compagnie.
        request.setAttribute("compagnieId", personne.getCompagnie() != null ? personne.getCompagnie().getId() : null);

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (personne.getRoleid() != null) {
            // getRoleid() est un proxy Hibernate lazy : la session de la requete
            // n'est pas ouverte a ce stade du filtre, donc on recharge le role
            // via son id (sur - ne declenche pas d'initialisation du proxy) plutot
            // que d'accéder directement a ses attributs (LazyInitializationException).
            Long roleId = personne.getRoleid().getId();
            Roles role = roleRepository.findById(roleId).orElse(null);
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                List<Permission> permissions = rolePermissionsRepositorie.listePermissionsByRoles(roleId);
                if (permissions != null) {
                    permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
                }
                // Delegation d'audit individuelle (voir AuditAccessGrant) : une
                // revocation prend effet a la prochaine requete, sans reconnexion,
                // comme pour un changement de role.
                if (!RoleNames.SUPER_ADMIN.equals(role.getName())
                        && auditAccessGrantRepository.existsByGranteeUsernameAndRevokedFalse(personne.getUserName())) {
                    authorities.add(new SimpleGrantedAuthority("AUDIT_VIEWER"));
                }
            }
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personne.getUserName(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
