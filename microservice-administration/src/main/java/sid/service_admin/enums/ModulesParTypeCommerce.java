package sid.service_admin.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Modules fonctionnels applicables a chaque type de commerce. Aujourd'hui, un
 * seul module est restreint (PHOTOCOPHIE, specifique aux librairies) ; tous
 * les autres modules sont universels. Pour ajouter une restriction future,
 * ajouter un cas dans pour() - ne pas construire de mapping configurable en
 * base tant qu'un seul cas d'usage existe.
 */
public final class ModulesParTypeCommerce {

    private static final Set<ModuleLicence> MODULES_UNIVERSELS = EnumSet.complementOf(
            EnumSet.of(ModuleLicence.PHOTOCOPHIE));

    private ModulesParTypeCommerce() {
    }

    public static Set<ModuleLicence> pour(TypeCommerce typeCommerce) {
        if (typeCommerce == TypeCommerce.LIBRAIRIE) {
            return EnumSet.allOf(ModuleLicence.class);
        }
        return MODULES_UNIVERSELS;
    }

    public static boolean estAutorise(TypeCommerce typeCommerce, ModuleLicence module) {
        return pour(typeCommerce).contains(module);
    }
}
