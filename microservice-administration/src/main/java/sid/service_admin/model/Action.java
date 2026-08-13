package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Action metier grantable sur un menu (Voir/Ajouter/Modifier/Supprimer,
 * Valider/Annuler/Imprimer...). Remplace l'ancien enum Java OperationType
 * (READ/WRITE/UPDATE/DELETE/PRINT), qui etait fige et dont la colonne MySQL
 * (ENUM natif) etait desynchronisee de l'enum (PRINT y etait injoignable -
 * "Data truncated for column 'operation_type'"). Catalogue global (aucune
 * colonne compagnie) au meme titre que Menu/Modulesecurite - une compagnie
 * choisit parmi les Actions existantes pour configurer ses Profils, mais ne
 * peut pas en creer de nouvelles (reserve SUPER_ADMIN/SYSTEM_ADMIN, voir
 * ActionController).
 */
@Entity
@Table(name = "action", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column
    private String description;

    public Action(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }
}
