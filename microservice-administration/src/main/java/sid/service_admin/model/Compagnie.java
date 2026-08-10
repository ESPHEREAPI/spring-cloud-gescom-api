package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.TypeCommerce;

/**
 * Racine multi-tenant : une compagnie (librairie, quincaillerie, minimarché...)
 * gérée par un administrateur compagnie et créée par un administrateur système.
 *
 * Volontairement distincte de l'entité legacy {@link Entreprise} (clé composite
 * année+employeur) : clé simple pour rester reutilisable telle quelle par
 * d'autres services plus tard.
 */
@Entity
@Table(name = "compagnie")
@Data
@NoArgsConstructor
public class Compagnie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    // Identifiant public unique de la compagnie, utilise dans l'URL de sa
    // page de vente publique (ex. /lipadi/...). Nullable pour les compagnies
    // existantes tant qu'elles n'ont pas ete migrees - genere automatiquement
    // a la creation d'une nouvelle compagnie (voir CompagnieService).
    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_commerce")
    private TypeCommerce typeCommerce;

    @Column(nullable = false)
    private Boolean actif = Boolean.TRUE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation")
    private Date dateCreation;

    private String adresse;

    private String tel;

    private String email;

    // Informations legales/facturation - volontairement absentes a la creation
    // (l'administrateur systeme qui cree la compagnie ne les connait pas) et
    // remplies ensuite en libre-service par l'administrateur de la compagnie
    // lui-meme (voir CompagnieController#updateOwn), pour alimenter l'en-tete
    // des tickets/factures A4 cote microservice-produits.
    private String capital;

    @Column(name = "numero_contribuable")
    private String numeroContribuable;

    private String nui;

    private String rccm;

    @Column(name = "site_web")
    private String siteWeb;

    private String directeur;

    @Column(name = "logo_chemin")
    private String logoChemin;

    private String bp;

    private String quartier;

    private String ville;

    @Column(name = "created_by")
    private String createdBy;

    @OneToMany(mappedBy = "compagnie")
    private List<Personne> personneList;

    public Compagnie(Long id) {
        this.id = id;
    }

    public Compagnie(String nom, TypeCommerce typeCommerce) {
        this.nom = nom;
        this.typeCommerce = typeCommerce;
        this.actif = Boolean.TRUE;
        this.dateCreation = new Date();
    }
}
