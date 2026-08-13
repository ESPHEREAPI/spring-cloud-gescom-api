package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 * Mirroir en lecture seule de sid.service_admin.model.Menu
 * (microservice-administration) - meme table "menu", schema MySQL partage.
 * Seuls id/code sont repris ici : suffisant pour calculer les autorites
 * PERM_&lt;MENU&gt;_&lt;ACTION&gt; (voir EffectivePermissionService).
 */
@Entity
@Table(name = "menu")
@Data
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;

    @Column(name = "code")
    private String code;
}
