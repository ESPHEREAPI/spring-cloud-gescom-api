package com.mproduits.model;

import com.mproduits.enums.ExceptionType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 * Mirroir en lecture seule de sid.service_admin.model.PersonnePermissionException
 * (microservice-administration) - meme table "personne_permission_exception",
 * schema MySQL partage. GRANT ajoute une action que le Profil n'accorde pas,
 * REVOKE retire une action que le Profil accorde (voir
 * EffectivePermissionService).
 */
@Entity
@Table(name = "personne_permission_exception")
@Data
public class PersonnePermissionException implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "personne_id")
    @ManyToOne
    private Personne personne;

    @JoinColumn(name = "permission_id")
    @ManyToOne
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ExceptionType type;
}
