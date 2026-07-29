package sid.service_admin.enums;

/**
 * Codes de modules fonctionnels pouvant etre actives par licence. Liste
 * fermee (plutot que des chaines libres) pour eviter qu'une licence
 * reference un module qui n'existe pas.
 */
public enum ModuleLicence {
    FACTURATION, STOCK, VENTE, CLIENTS, RAPPORTS, CAISSE
}
