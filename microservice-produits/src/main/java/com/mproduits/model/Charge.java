package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "charge")
public class Charge implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "date_charge")
    private LocalDate dateCharge;
    @Column(name = "commentaire")
    private String commentaire;
    @Column(name = "username")
    private String username;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_depense_id")
    private TypeDepense typeDepense;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    public Charge() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public LocalDate getDateCharge() { return dateCharge; }
    public void setDateCharge(LocalDate dateCharge) { this.dateCharge = dateCharge; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public TypeDepense getTypeDepense() { return typeDepense; }
    public void setTypeDepense(TypeDepense typeDepense) { this.typeDepense = typeDepense; }
    public Boutique getBoutique() { return boutique; }
    public void setBoutique(Boutique boutique) { this.boutique = boutique; }
}
