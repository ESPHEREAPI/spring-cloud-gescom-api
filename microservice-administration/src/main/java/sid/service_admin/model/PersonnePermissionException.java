package sid.service_admin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.ExceptionType;

/**
 * Exception posee sur un utilisateur precis (Personne), par-dessus les
 * droits herites en direct de son Profil : GRANT ajoute une action que le
 * Profil n'accorde pas, REVOKE retire une action que le Profil accorde.
 * Le Profil reste la seule source de verite vivante - toute evolution
 * ulterieure du Profil continue de s'appliquer automatiquement, sauf sur les
 * couples (menu, action) explicitement exceptes ici (voir
 * PersonnePermissionService.getPermissionsEffectives).
 */
@Entity
@Table(name = "personne_permission_exception", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"personne_id", "permission_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonnePermissionException implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn(name = "personne_id")
    @ManyToOne
    private Personne personne;

    @JoinColumn(name = "permission_id")
    @ManyToOne
    private Permission permission;

    @Enumerated(EnumType.STRING)
    private ExceptionType type;

    private String createdBy;

    private Date createdAt;

    public PersonnePermissionException(Personne personne, Permission permission, ExceptionType type, String createdBy) {
        this.personne = personne;
        this.permission = permission;
        this.type = type;
        this.createdBy = createdBy;
        this.createdAt = new Date();
    }
}
