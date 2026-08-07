/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mproduits.enums.StatutVente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "vente-easy", uniqueConstraints = {
    @UniqueConstraint(name = "ticket", columnNames = {"Anneeid","Employeurid", "numerotickets"})
})
public class Vente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "datevente")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateVente;
  @JoinColumn(name = "clientid", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Client client;
    @Column(name = "totalbrut")
    private BigDecimal totalBrut;
    @Column(name = "totalremise")
    private BigDecimal totalRemise;
    @Column(name = "totalnet")
    private BigDecimal totalNet;
    private BigDecimal totalrecu;

    @Enumerated(EnumType.STRING)
    private StatutVente statut;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendeur_id", referencedColumnName = "id")
    private Personne vendeur;
   
    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LigneVente> lignes = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL)
    private List<Paiement> paiements = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL)
    private List<Remise> remises = new ArrayList<>();
    @Column(name = "numerotickets")
    private String numeroTicket;
    @Column(name = "cheminPatheTicket")
    private String cheminPatheTicket;
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "Employeurid", referencedColumnName = "compagnie_id")})
    @ManyToOne(optional = false)
    @JsonIgnore
    private Entreprise entreprise;
        @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Boutique boutique;

    @Transient
    @Temporal(TemporalType.TIME)
    private Date dateTicketPrint;
      @JoinColumn(name = "pointventeid", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Boutique pointvente;
      private String userecom;
      private long numerocommande;
      private String username_statut_annuler;
}
