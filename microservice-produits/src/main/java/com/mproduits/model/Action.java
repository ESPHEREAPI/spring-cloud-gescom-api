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
 * Mirroir en lecture seule de sid.service_admin.model.Action
 * (microservice-administration) - meme table "action", schema MySQL partage.
 * Catalogue global des actions grantables sur un menu, gere exclusivement
 * cote microservice-administration.
 */
@Entity
@Table(name = "action")
@Data
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "libelle")
    private String libelle;
}
