package sid.service_admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Association Profil <-> Permission(menu, action) : porte la matrice
 * Profil x Menu x Action assignee ensuite aux utilisateurs (Personne.profilid).
 * Le Role reste une simple etiquette/categorie, sans lien direct aux menus
 * (voir RolePermissions, qui garde son role d'origine : permissions globales
 * READ/WRITE/UPDATE/DELETE utilisees par JwtAuthFilter pour les
 * GrantedAuthority, sans rapport avec les menus).
 */
@Entity
@Table(name = "profil_permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfilPermissions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JoinColumn(name = "profil_id", referencedColumnName = "Id")
    @ManyToOne
    private Profil profil;
    @JoinColumn(name = "permission_id", referencedColumnName = "id")
    @ManyToOne
    private Permission permission;

    public ProfilPermissions(Profil profil, Permission permission) {
        this.profil = profil;
        this.permission = permission;
    }
}
