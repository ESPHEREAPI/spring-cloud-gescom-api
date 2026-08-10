/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "bonachat")
public class BonAchat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "codebon", unique = true)
    private String codeBon;
    @Column(name = "montanttotal")
    private BigDecimal montantTotal;
    @Column(name = "montantutiliser")
    private BigDecimal montantUtilise;
    @Column(name = "dateexpiration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExpiration;

    @ManyToOne(optional = false)
    private ClientBonAchat clientBonAchat;
    private boolean actif;

    // Trace d'impression - un bon deja imprime ne doit plus pouvoir l'etre a
    // nouveau (evite qu'un meme bon circule en plusieurs exemplaires papier).
    private boolean imprime;
    @Column(name = "date_impression")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateImpression;

}
