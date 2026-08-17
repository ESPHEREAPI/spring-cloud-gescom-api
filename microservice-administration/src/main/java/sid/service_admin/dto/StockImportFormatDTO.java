package sid.service_admin.dto;

import java.util.List;
import lombok.Data;
import sid.service_admin.enums.ChampImportStock;

/**
 * Format de restauration de stock d'une compagnie - liste ordonnee de
 * champs (voir StockImportFormat). L'ordre de la liste EST l'ordre des
 * colonnes du fichier Excel genere/attendu.
 */
@Data
public class StockImportFormatDTO {
    private List<ChampImportStock> colonnes;
}
