package sid.service_admin.enums;

/**
 * Codes de modules fonctionnels pouvant etre actives par licence. Miroir 1:1
 * des codes reels de Modulesecurite (voir InitiationDb.getAllModuleSecurite())
 * - PHOTOCOPHIE reflete une coquille historique presente en base ("photocophie",
 * pas "photocopie"), conservee ici intentionnellement pour rester alignee avec
 * la BD et le sidebar Angular (renommage global hors perimetre).
 */
public enum ModuleLicence {
    SECURITE("securite"),
    ADMINISTRATION("administration"),
    STOCK("stock"),
    VENTE("vente"),
    FACTURATION("facturation"),
    COMPTABILITE("comptabilite"),
    PARAMETRAGE("parametrage"),
    PHOTOCOPHIE("photocophie");

    private final String code;

    ModuleLicence(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ModuleLicence fromCode(String code) {
        for (ModuleLicence m : values()) {
            if (m.code.equalsIgnoreCase(code)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Code module inconnu : " + code);
    }
}
