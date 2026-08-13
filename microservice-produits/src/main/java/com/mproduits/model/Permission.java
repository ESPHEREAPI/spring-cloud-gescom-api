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
 * Mirroir en lecture seule de sid.service_admin.model.Permission
 * (microservice-administration) - meme table "permission", schema MySQL
 * partage. Seuls menu/action sont repris ici (pas operationType/name/
 * description, non utilises cote microservice-produits).
 */
@Entity
@Table(name = "permission")
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "menu_id")
    @ManyToOne
    private Menu menu;

    @JoinColumn(name = "action_id")
    @ManyToOne
    private Action action;
}
