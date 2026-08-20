package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Photo d'un {@link Produit}, stockee en BLOB MySQL. Deliberement SANS
 * association JPA vers Produit (ni @OneToOne ni FK mappee) : Produit est deja
 * charge EAGER a de nombreux endroits de ce code (cause racine des bugs
 * "Found shared references to a collection" deja corriges plusieurs fois) -
 * une association ferait charger les octets de l'image a chaque chargement
 * de Produit, meme quand la photo n'est pas necessaire. La photo n'est lue
 * que via une requete separee et explicite (voir ProduitPhotoRepository).
 */
@Entity
@Data
@Table(name = "produit_photo")
public class ProduitPhoto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Pas de @GeneratedValue : cet id EST le produitId (relation logique 1-1).
    @Id
    @Column(name = "produit_id")
    private Long produitId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "date_maj")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateMaj;
}
