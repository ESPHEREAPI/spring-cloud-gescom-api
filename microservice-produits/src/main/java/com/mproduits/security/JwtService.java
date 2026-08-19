package com.mproduits.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validation des JWT emis par microservice-administration (tokens staff),
 * ET emission des JWT clients pour le site e-commerce public (voir
 * EcomAuthController) - microservice-produits est le seul des deux services
 * a connaitre le modele Client, d'ou l'emission faite ici plutot que cote
 * admin. Les deux types de token partagent la MEME cle app.jwtSecret
 * (configuree separement ici car pas de POM parent entre les modules) mais
 * portent un claim "typ" distinct ("customer" vs absent/staff) pour ne
 * jamais etre confondus - voir JwtAuthFilter.
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

    // ========== Emission des tokens clients (e-commerce) ==========

    public String generateCustomerToken(Long clientId, Long compagnieId, Date expiryDate) {
        return Jwts.builder()
                .setSubject("customer:" + clientId)
                .claim("typ", "customer")
                .claim("clientId", clientId)
                .claim("compagnieId", compagnieId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key())
                .compact();
    }

    /**
     * "customer" pour un token client, null pour un token staff (jamais
     * pose par microservice-administration) - c'est cette distinction, pas
     * juste la signature valide, qui empeche un token client d'agir comme
     * un compte staff. Voir JwtAuthFilter.
     */
    public String getTyp(String token) {
        Object typ = parse(token).getBody().get("typ");
        return typ != null ? typ.toString() : null;
    }

    public Long getClientId(String token) {
        Object clientId = parse(token).getBody().get("clientId");
        if (clientId == null) {
            return null;
        }
        return Long.valueOf(clientId.toString());
    }
}
