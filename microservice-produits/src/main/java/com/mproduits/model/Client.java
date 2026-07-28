/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "client")
@Data
@NamedQueries({
    @NamedQuery(name = "Client.findAll", query = "SELECT c FROM Client c"),
    @NamedQuery(name = "Client.findById", query = "SELECT c FROM Client c WHERE c.id = :id"),
    @NamedQuery(name = "Client.findByBp", query = "SELECT c FROM Client c WHERE c.bp = :bp"),
    @NamedQuery(name = "Client.findByEmail", query = "SELECT c FROM Client c WHERE c.email = :email"),
    @NamedQuery(name = "Client.findByFax", query = "SELECT c FROM Client c WHERE c.fax = :fax"),
    @NamedQuery(name = "Client.findByIndicatifPays", query = "SELECT c FROM Client c WHERE c.indicatifPays = :indicatifPays"),
    @NamedQuery(name = "Client.findByQuartier", query = "SELECT c FROM Client c WHERE c.quartier = :quartier"),
    @NamedQuery(name = "Client.findByRegion", query = "SELECT c FROM Client c WHERE c.region = :region"),
    @NamedQuery(name = "Client.findByTelephone", query = "SELECT c FROM Client c WHERE c.telephone = :telephone"),
    @NamedQuery(name = "Client.findByVille", query = "SELECT c FROM Client c WHERE c.ville = :ville"),
    @NamedQuery(name = "Client.findByCode", query = "SELECT c FROM Client c WHERE c.code = :code"),
    @NamedQuery(name = "Client.findByNom", query = "SELECT c FROM Client c WHERE c.nom = :nom")})
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Bp")
    private String bp;
    @Column(name = "Email")
    private String email;
    @Column(name = "Fax")
    private String fax;
    @Column(name = "indicatifPays")
    private String indicatifPays;
    @Column(name = "Quartier")
    private String quartier;
    @Column(name = "Region")
    private String region;
    @Column(name = "Tel")
    private String telephone;
    @Column(name = "Ville")
    private String ville;
    @Column(name = "code")
    private String code;
    @Column(name = "nom")
    private String nom;
    private String statut; // ACTIF, INACTIF, EN_ATTENTE
    private String adresse;
    @JsonIgnore
    @JoinColumn(name = "serviceid", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Serviceclient serviceid;
    @JoinColumn(name = "TypeClientid", referencedColumnName = "id")
    @JsonIgnore
    @ManyToOne(optional = true)
    private Typeclient typeClientid;
    @JoinColumn(name = "Villeid", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Ville villeid;

    private boolean fidelite;
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<Devis> devis;
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<Facture> factures;
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<VersementClient> versements;
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<NotificationClient> notifications;

}
