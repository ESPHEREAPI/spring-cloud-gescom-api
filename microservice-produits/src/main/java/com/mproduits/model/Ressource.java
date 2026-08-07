package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ressource")
public class Ressource implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "date_ressource")
    private LocalDate dateRessource;
    @Column(name = "commentaire")
    private String commentaire;
    @Column(name = "username")
    private String username;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_resource_id")
    private TypeResource typeResource;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    public Ressource() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public LocalDate getDateRessource() { return dateRessource; }
    public void setDateRessource(LocalDate dateRessource) { this.dateRessource = dateRessource; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public TypeResource getTypeResource() { return typeResource; }
    public void setTypeResource(TypeResource typeResource) { this.typeResource = typeResource; }
    public Boutique getBoutique() { return boutique; }
    public void setBoutique(Boutique boutique) { this.boutique = boutique; }
}
