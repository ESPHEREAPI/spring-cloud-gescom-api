package sid.service_admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.ChampImportStock;

/**
 * Format du fichier de restauration de stock d'une compagnie : liste
 * ordonnee de colonnes que son administrateur a choisies (ex: REFERENCE,
 * PRODUIT, CATEGORIE, PRIX_VENTE, PRIX_ACHAT, QUANTITE - une autre compagnie
 * peut choisir un ordre et un sous-ensemble differents). Une seule ligne par
 * compagnie (voir StockImportFormatService#getOrCreate, meme pattern que
 * CompagnieParametres). Consomme en lecture seule par microservice-produits
 * (seul detenteur de Produit/Boutique/PointVente) pour generer le modele
 * Excel et lire les fichiers de restauration envoyes.
 */
@Entity
@Table(name = "stock_import_format")
@Data
@NoArgsConstructor
public class StockImportFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "compagnie_id", nullable = false, unique = true)
    private Compagnie compagnie;

    @ElementCollection
    @CollectionTable(name = "stock_import_format_colonne", joinColumns = @JoinColumn(name = "stock_import_format_id"))
    @OrderColumn(name = "position")
    @Enumerated(EnumType.STRING)
    @Column(name = "champ")
    private List<ChampImportStock> colonnes = new ArrayList<>();

    public StockImportFormat(Compagnie compagnie) {
        this.compagnie = compagnie;
    }
}
