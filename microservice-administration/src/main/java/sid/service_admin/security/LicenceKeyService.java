package sid.service_admin.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sid.service_admin.model.Licence;

/**
 * Signe/verifie la "cle de licence" remise une fois au SYSTEM_ADMIN/SUPER_ADMIN
 * (artefact professionnel, preuve infalsifiable des termes de la licence) —
 * n'est PAS ce qui est revalide a chaque requete (voir LicenceStatusService
 * cote produits pour l'application reelle, base sur le statut en base).
 *
 * Cle privee RSA-2048 chargee au demarrage depuis APP_LICENCE_RSA_PRIVATE_KEY
 * (variable d'environnement uniquement, jamais dans cloud-config-gescom) —
 * accepte soit le contenu PEM PKCS8 directement, soit le chemin d'un fichier
 * .pem contenant ce contenu (plus pratique a renseigner qu'un bloc
 * multi-lignes dans un champ de variable d'environnement). Generation :
 * openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out cle.pem
 */
@Component
public class LicenceKeyService {

    @Value("${app.licence.rsaPrivateKey:}")
    private String rsaPrivateKeyPem;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() {
        if (rsaPrivateKeyPem == null || rsaPrivateKeyPem.isBlank()) {
            return;
        }
        try {
            String pemContent = resolvePemContent(rsaPrivateKeyPem);
            String cleaned = pemContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(cleaned);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) this.privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
            this.publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Cle privee de licence invalide (APP_LICENCE_RSA_PRIVATE_KEY)", e);
        }
    }

    /** Si la valeur pointe vers un fichier existant, lit son contenu ; sinon la traite comme du contenu PEM direct. */
    private String resolvePemContent(String value) throws IOException {
        try {
            Path path = Path.of(value.trim());
            if (Files.isRegularFile(path)) {
                return Files.readString(path);
            }
        } catch (java.nio.file.InvalidPathException e) {
            // La valeur n'est pas un chemin valide (ex: contenu PEM multi-lignes) - on la traite comme du contenu direct.
        }
        return value;
    }

    private void requireConfigured() {
        if (privateKey == null) {
            throw new IllegalStateException(
                    "APP_LICENCE_RSA_PRIVATE_KEY n'est pas configure : impossible de generer/verifier une cle de licence");
        }
    }

    public String genererCle(Licence licence) {
        requireConfigured();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .setSubject(licence.getCompagnie().getId().toString())
                .claim("nom", licence.getCompagnie().getNom())
                .claim("maxUtilisateurs", licence.getMaxUtilisateurs())
                .claim("maxBoutiques", licence.getMaxBoutiques())
                .claim("modules", licence.getModulesActifs())
                .setIssuedAt(licence.getDateDebut())
                .setExpiration(licence.getDateExpiration())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /** Verifie l'authenticite d'une cle de licence deja emise (affordance UI "verifier"). */
    public Map<String, Object> verifierCle(String cle) {
        requireConfigured();
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(cle).getBody();
    }
}
