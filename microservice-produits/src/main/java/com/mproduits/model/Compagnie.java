package com.mproduits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Miroir minimal (id + nom) de sid.service_admin.model.Compagnie, meme table
 * physique partagee ("compagnie") - ce module ne possede/gere pas cette
 * entite (proprietaire : microservice-administration), il s'en sert
 * uniquement comme reference FK pour scoper les donnees metier par
 * compagnie (meme pattern que la duplication existante de Personne/Boutique
 * entre les deux modules).
 */
@Entity
@Table(name = "compagnie")
public class Compagnie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    // Identifiant public unique (URL de la page de vente publique) -
    // proprietaire reel : microservice-administration, meme table physique
    // partagee. Lecture seule ici.
    private String code;

    // Champs supplementaires en lecture seule (identite fiscale/contact de la
    // compagnie), utilises pour l'impression des tickets/factures - meme table
    // physique partagee, proprietaire reel : microservice-administration.
    private String adresse;
    private String tel;
    private String email;
    @Column(name = "numero_contribuable")
    private String numeroContribuable;
    private String nui;
    private String rccm;
    private String directeur;
    @Column(name = "logo_chemin")
    private String logoChemin;
    private String bp;
    private String quartier;
    private String ville;

    public Compagnie() {
    }

    public Compagnie(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumeroContribuable() {
        return numeroContribuable;
    }

    public void setNumeroContribuable(String numeroContribuable) {
        this.numeroContribuable = numeroContribuable;
    }

    public String getNui() {
        return nui;
    }

    public void setNui(String nui) {
        this.nui = nui;
    }

    public String getRccm() {
        return rccm;
    }

    public void setRccm(String rccm) {
        this.rccm = rccm;
    }

    public String getDirecteur() {
        return directeur;
    }

    public void setDirecteur(String directeur) {
        this.directeur = directeur;
    }

    public String getLogoChemin() {
        return logoChemin;
    }

    public void setLogoChemin(String logoChemin) {
        this.logoChemin = logoChemin;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Compagnie)) {
            return false;
        }
        Compagnie other = (Compagnie) object;
        return (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Compagnie[ id=" + id + " ]";
    }
}
