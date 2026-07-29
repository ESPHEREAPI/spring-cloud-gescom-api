package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Delegation individuelle de la consultation de l'audit : le SUPER_ADMIN
 * choisit une personne precise (pas un role entier - voir le mecanisme
 * Roles/RolePermissions existant, trop grossier pour ce besoin) a qui donner
 * acces a son perimetre d'audit.
 */
@Entity
@Table(name = "audit_access_grant")
@Data
@NoArgsConstructor
public class AuditAccessGrant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grantee_username", nullable = false)
    private String granteeUsername;

    @Column(name = "granted_by_username", nullable = false)
    private String grantedByUsername;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "granted_at")
    private Date grantedAt;

    @Column(nullable = false)
    private Boolean revoked = Boolean.FALSE;
}
