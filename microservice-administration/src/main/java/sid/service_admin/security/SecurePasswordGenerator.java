package sid.service_admin.security;

import java.security.SecureRandom;

/**
 * Genere un mot de passe temporaire aleatoire quand aucun n'est fourni a la
 * creation d'un compte (jamais de mot de passe par defaut code en dur).
 */
public final class SecurePasswordGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {
    }

    public static String generate() {
        return generate(20);
    }

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
