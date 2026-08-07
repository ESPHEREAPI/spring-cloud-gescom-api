package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "marge_cible")
public class MargeCible implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "taux_cible")
    private BigDecimal tauxCible;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private Categories categorie;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "compagnie_id")
    private Compagnie compagnie;

    public MargeCible() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getTauxCible() { return tauxCible; }
    public void setTauxCible(BigDecimal tauxCible) { this.tauxCible = tauxCible; }
    public Categories getCategorie() { return categorie; }
    public void setCategorie(Categories categorie) { this.categorie = categorie; }
    public Compagnie getCompagnie() { return compagnie; }
    public void setCompagnie(Compagnie compagnie) { this.compagnie = compagnie; }
    public Long getCompagnieId() { return compagnie != null ? compagnie.getId() : null; }
    public void setCompagnieId(Long compagnieId) { this.compagnie = compagnieId != null ? new Compagnie(compagnieId) : null; }
}
