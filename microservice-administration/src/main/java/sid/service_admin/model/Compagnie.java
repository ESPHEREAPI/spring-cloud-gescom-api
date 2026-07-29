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

    @Column(name = "created_by")
    private String createdBy;

    @OneToMany(mappedBy = "compagnie")
    private List<Personne> personneList;

    public Compagnie(String nom, TypeCommerce typeCommerce) {
        this.nom = nom;
        this.typeCommerce = typeCommerce;
        this.actif = Boolean.TRUE;
        this.dateCreation = new Date();
    }
}
