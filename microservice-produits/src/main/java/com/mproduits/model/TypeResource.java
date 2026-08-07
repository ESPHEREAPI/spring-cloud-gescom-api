package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "type_resource")
public class TypeResource implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;
    @Column(name = "libelle")
    private String libelle;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "compagnie_id")
    private Compagnie compagnie;

    public TypeResource() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Compagnie getCompagnie() { return compagnie; }
    public void setCompagnie(Compagnie compagnie) { this.compagnie = compagnie; }
    public Long getCompagnieId() { return compagnie != null ? compagnie.getId() : null; }
    public void setCompagnieId(Long compagnieId) { this.compagnie = compagnieId != null ? new Compagnie(compagnieId) : null; }
}
