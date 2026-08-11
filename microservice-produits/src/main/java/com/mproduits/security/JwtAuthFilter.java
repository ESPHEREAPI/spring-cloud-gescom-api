package com.mproduits.security;

import com.mproduits.model.Personne;
import com.mproduits.repositories.PersonneRepositories;
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

/**
 * Copie independante du filtre JWT de microservice-administration : valide
 * le token, recharge le role ET le profil actuels depuis la base (pas depuis
 * le token, pour qu'une retrogradation prenne effet immediatement), et
 * memorise le compagnieId (claim du token, non rechargeable ici car Personne
 * n'a pas de colonne compagnie dans ce module) en attribut de requete pour
 * le filtre de licence place juste apres dans la chaine.
 *
 * Deux authorities distinctes sont accordees : ROLE_<code du Role> (simple
 * etiquette de hierarchie plateforme : SUPER_ADMIN/SYSTEM_ADMIN/
 * COMPANY_ADMIN/EMPLOYE - jamais ADMIN/CAISSIER/etc, voir Personne.roleid)
 * et ROLE_<code du Profil> (le vrai metier de l'utilisateur : ADMIN/
 * CAISSIER/COMMERCIAL/COMPTABLE/USER, voir Personne.profilid) si un profil
 * est assigne. @PreAuthorize peut donc distinguer un caissier d'un
 * comptable, tout en gardant COMPANY_ADMIN comme "passe-partout" pour le
 * createur de la compagnie (qui n'a jamais de profil assigne - voir
 * AdminAccountService.createCompanyAdmin, cote microservice-administration).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PersonneRepositories personneRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)) {
                String username = jwtService.getUsername(token);
                personneRepository.findByUserName(username).ifPresent(this::authenticate);
                request.setAttribute("compagnieId", jwtService.getCompagnieId(token));
                request.setAttribute("boutiqueId", jwtService.getBoutiqueId(token));
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(Personne personne) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (personne.getRoleid() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + personne.getRoleid().getCode()));
        }
        if (personne.getProfilid() != null && personne.getProfilid().getCode() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + personne.getProfilid().getCode()));
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personne.getUserName(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
