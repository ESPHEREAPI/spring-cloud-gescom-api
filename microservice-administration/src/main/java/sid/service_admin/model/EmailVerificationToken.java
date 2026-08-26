package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Jeton de verification d'adresse email, utilise par l'inscription
 * autonome d'une compagnie (voir CompagnieService.inscriptionAutonome) -
 * l'administrateur cree via ce chemin reste isActive=false tant que le
 * lien recu par email n'a pas ete clique. Validite courte (24h) et usage
 * unique (used passe a true a la premiere validation reussie).
 */
@Entity
@Table(name = "email_verification_token")
@Data
@NoArgsConstructor
public class EmailVerificationToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "personne_id", nullable = false)
    private Personne personne;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExpiration;

    @Column(nullable = false)
    private Boolean used = Boolean.FALSE;

    public boolean isValide() {
        return Boolean.FALSE.equals(used) && dateExpiration.after(new Date());
    }
}
