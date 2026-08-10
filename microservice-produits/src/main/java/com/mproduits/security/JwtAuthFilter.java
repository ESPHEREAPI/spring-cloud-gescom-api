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
 * le token, recharge le role actuel depuis la base (pas depuis le token, pour
 * qu'une retrogradation prenne effet immediatement), et memorise le
 * compagnieId (claim du token, non rechargeable ici car Personne n'a pas de
 * colonne compagnie dans ce module) en attribut de requete pour le filtre de
 * licence place juste apres dans la chaine.
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

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(personne.getUserName(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
