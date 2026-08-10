package com.mproduits.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validation des JWT emis par microservice-administration. Doit lire la MEME
 * cle app.jwtSecret que l'admin (configuree separement ici car pas de POM
 * parent entre les modules) pour pouvoir verifier la signature localement,
 * sans appel reseau a chaque requete.
 */
@Component
public class JwtService {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    /**
     * Le claim compagnieId n'est jamais modifiable apres creation du compte
     * (contrairement au role) : on lui fait confiance directement depuis le
     * token plutot que de le recharger depuis une base ou Personne n'a pas de
     * colonne compagnie.
     */
    public Long getCompagnieId(String token) {
        Object compagnieId = parse(token).getBody().get("compagnieId");
        if (compagnieId == null) {
            return null;
        }
        return Long.valueOf(compagnieId.toString());
    }

    /**
     * Boutique assignee au compte (claim optionnel - absent pour un admin ou
     * un token emis avant l'ajout de ce claim). Utilise par BoutiqueAccessGuard
     * pour restreindre un compte CAISSIER a sa propre boutique.
     */
    public Long getBoutiqueId(String token) {
        Object boutiqueId = parse(token).getBody().get("boutiqueId");
        if (boutiqueId == null) {
            return null;
        }
        return Long.valueOf(boutiqueId.toString());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    }
}
