package sid.service_admin.security;

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
 * Emission/validation des JWT, signes avec la cle configuree (app.jwtSecret)
 * plutot qu'une cle aleatoire regeneree a chaque demarrage.
 */
@Component
public class JwtService {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Date expiresAt(long jwtExpirationMs) {
        return new Date(System.currentTimeMillis() + jwtExpirationMs);
    }

    /**
     * boutiqueId : boutique assignee au compte (null pour un admin/gerant
     * non rattache a une boutique precise) - permet cote microservice-produits
     * de restreindre un compte CAISSIER a sa propre boutique (voir
     * BoutiqueAccessGuard.verifierBoutiqueUtilisateur).
     */
    public String generateToken(String username, Long compagnieId, String roleName, Long boutiqueId, Date expiryDate) {
        return Jwts.builder()
                .setSubject(username)
                .claim("compagnieId", compagnieId)
                .claim("role", roleName)
                .claim("boutiqueId", boutiqueId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key())
                .compact();
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    }
}
