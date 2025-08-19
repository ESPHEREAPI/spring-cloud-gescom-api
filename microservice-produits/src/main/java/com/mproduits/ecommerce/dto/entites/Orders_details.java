/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.entites;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data

public class Orders_details implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_details;

    private long id_prix_articles;
    BigDecimal prix_Unitaire;
    String libelle;
    BigDecimal quantite_caddy;
    @JoinColumn(name = "Orders_id", referencedColumnName = "id")
    @ManyToOne
    private Orders orders;

    @Override
    public String toString() {
        return "sid.bookshop.ecom.entites.Orders_details[ id=" + id_details + " ]";
    }

}
