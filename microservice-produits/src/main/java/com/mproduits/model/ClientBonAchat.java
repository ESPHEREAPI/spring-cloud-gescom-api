/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "clientbonachat")
public class ClientBonAchat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nom;
    private String telephone;
    private String email;
    private boolean fidelite;

    // Isolation multi-tenant : un client bon d'achat est propre a chaque compagnie.
    @JsonIgnore
    @JoinColumn(name = "compagnie_id")
    @ManyToOne
    private Compagnie compagnie;

    public Long getCompagnieId() {
        return compagnie != null ? compagnie.getId() : null;
    }

    public void setCompagnieId(Long compagnieId) {
        this.compagnie = compagnieId != null ? new Compagnie(compagnieId) : null;
    }
}
