package sid.service_admin.service;

import org.springframework.stereotype.Service;
import sid.service_admin.enums.LoginFormat;
import sid.service_admin.enums.TypeCommerce;
import sid.service_admin.repository.PersonneRepository;

/**
 * Genere le login d'un employe cree pour une compagnie, selon le format
 * choisi par cette compagnie (CompagnieParametres#loginFormat, voir
 * "Option Entreprise"). Ne concerne que UserService#createUser - les comptes
 * de la hierarchie admin gardent leur propre generation (AdminAccountService),
 * volontairement non touchee ici.
 */
@Service
public class LoginGeneratorService {

    private final PersonneRepository personneRepository;

    public LoginGeneratorService(PersonneRepository personneRepository) {
        this.personneRepository = personneRepository;
    }

    /** Genere un login unique en base, selon le format demande (INITIALE_NOM si non precise). */
    public String genererLogin(LoginFormat format, TypeCommerce typeCommerce, String firstName, String lastName, Long compagnieId) {
        String base = construireBase(format == null ? LoginFormat.INITIALE_NOM : format, typeCommerce, firstName, lastName, compagnieId);
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffixe = 1;
        while (personneRepository.findByUserName(candidate).isPresent()) {
            candidate = base + suffixe;
            suffixe++;
        }
        return candidate;
    }

    private String construireBase(LoginFormat format, TypeCommerce typeCommerce, String firstName, String lastName, Long compagnieId) {
        switch (format) {
            case PRENOM_POINT_NOM:
                return normalize(firstName) + "." + normalize(lastName);
            case NOM_INITIALE_PRENOM:
                return normalize(lastName) + initiale(firstName);
            case CODE_TYPE_SEQUENCE:
                long dejaExistants = compagnieId == null ? 0 : personneRepository.countByCompagnie_Id(compagnieId);
                return codeType(typeCommerce) + "-" + String.format("%04d", dejaExistants + 1);
            case INITIALE_NOM:
            default:
                return initiale(firstName) + normalize(lastName);
        }
    }

    private String codeType(TypeCommerce typeCommerce) {
        if (typeCommerce == null) {
            return "GEN";
        }
        switch (typeCommerce) {
            case LIBRAIRIE: return "LIB";
            case QUINCAILLERIE: return "QUI";
            case MINIMARCHE: return "MINI";
            case SUPERMARCHE: return "SUP";
            case BOUTIQUE: return "BTQ";
            case AUTRE:
            default: return "GEN";
        }
    }

    private String initiale(String value) {
        return (value != null && !value.isBlank()) ? normalize(value.substring(0, 1)) : "";
    }

    /** Minuscules, sans accents ni caracteres non alphanumeriques - meme normalisation que AdminAccountService. */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
