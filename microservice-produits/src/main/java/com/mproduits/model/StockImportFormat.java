package com.mproduits.model;

import com.mproduits.enums.ChampImportStock;
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

/**
 * Mirroir en lecture seule de sid.service_admin.model.StockImportFormat
 * (microservice-administration) - meme table "stock_import_format" (et
 * "stock_import_format_colonne"), schema MySQL partage. Proprietaire reel :
 * microservice-administration (l'admin compagnie y definit son format).
 * Lu ici pour generer le modele Excel et parser les fichiers de
 * restauration envoyes (voir StockRestaurationService), seul module qui
 * connait Produit/Boutique/PointVente.
 */
@Entity
@Table(name = "stock_import_format")
@Data
public class StockImportFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "compagnie_id")
    private Compagnie compagnie;

    @ElementCollection
    @CollectionTable(name = "stock_import_format_colonne", joinColumns = @JoinColumn(name = "stock_import_format_id"))
    @OrderColumn(name = "position")
    @Enumerated(EnumType.STRING)
    @Column(name = "champ")
    private List<ChampImportStock> colonnes = new ArrayList<>();
}
