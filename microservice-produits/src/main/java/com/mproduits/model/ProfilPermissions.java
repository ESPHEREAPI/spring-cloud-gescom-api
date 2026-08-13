package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 * Mirroir en lecture seule de sid.service_admin.model.ProfilPermissions
 * (microservice-administration) - meme table "profil_permissions", schema
 * MySQL partage.
 */
@Entity
@Table(name = "profil_permissions")
@Data
public class ProfilPermissions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "profil_id", referencedColumnName = "Id")
    @ManyToOne
    private Profil profil;

    @JoinColumn(name = "permission_id", referencedColumnName = "id")
    @ManyToOne
    private Permission permission;
}
