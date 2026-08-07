/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "personne")
@NamedQueries({
    @NamedQuery(name = "Personne.findAll", query = "SELECT p FROM Personne p"),
    @NamedQuery(name = "Personne.findById", query = "SELECT p FROM Personne p WHERE p.id = :id"),
    @NamedQuery(name = "Personne.findByBp", query = "SELECT p FROM Personne p WHERE p.bp = :bp"),
    @NamedQuery(name = "Personne.findByEmail", query = "SELECT p FROM Personne p WHERE p.email = :email"),
    @NamedQuery(name = "Personne.findByFax", query = "SELECT p FROM Personne p WHERE p.fax = :fax"),
    @NamedQuery(name = "Personne.findByIndicatifPays", query = "SELECT p FROM Personne p WHERE p.indicatifPays = :indicatifPays"),
    @NamedQuery(name = "Personne.findByQuartier", query = "SELECT p FROM Personne p WHERE p.quartier = :quartier"),
    @NamedQuery(name = "Personne.findByRegion", query = "SELECT p FROM Personne p WHERE p.region = :region"),
    @NamedQuery(name = "Personne.findByTel", query = "SELECT p FROM Personne p WHERE p.tel = :tel"),
    @NamedQuery(name = "Personne.findByVille", query = "SELECT p FROM Personne p WHERE p.ville = :ville"),
    @NamedQuery(name = "Personne.findByCompteActif", query = "SELECT p FROM Personne p WHERE p.compteActif = :compteActif"),
    @NamedQuery(name = "Personne.findByDateEnregistrement", query = "SELECT p FROM Personne p WHERE p.dateEnregistrement = :dateEnregistrement"),
    @NamedQuery(name = "Personne.findByDateNaissance", query = "SELECT p FROM Personne p WHERE p.dateNaissance = :dateNaissance"),
    @NamedQuery(name = "Personne.findByDeleteDate", query = "SELECT p FROM Personne p WHERE p.deleteDate = :deleteDate"),
    @NamedQuery(name = "Personne.findByLastDatePwdModif", query = "SELECT p FROM Personne p WHERE p.lastDatePwdModif = :lastDatePwdModif"),
    @NamedQuery(name = "Personne.findByLastModifDate", query = "SELECT p FROM Personne p WHERE p.lastModifDate = :lastModifDate"),
    @NamedQuery(name = "Personne.findByLastUserModif", query = "SELECT p FROM Personne p WHERE p.lastUserModif = :lastUserModif"),
    @NamedQuery(name = "Personne.findByLieuNaissance", query = "SELECT p FROM Personne p WHERE p.lieuNaissance = :lieuNaissance"),
    @NamedQuery(name = "Personne.findByMatricule", query = "SELECT p FROM Personne p WHERE p.userName = :matricule"),
    @NamedQuery(name = "Personne.findByNbreEnfant", query = "SELECT p FROM Personne p WHERE p.nbreEnfant = :nbreEnfant"),
    @NamedQuery(name = "Personne.findByNom", query = "SELECT p FROM Personne p WHERE p.nom = :nom"),
    @NamedQuery(name = "Personne.findByPrenom", query = "SELECT p FROM Personne p WHERE p.prenom = :prenom"),
    @NamedQuery(name = "Personne.findByProfession", query = "SELECT p FROM Personne p WHERE p.profession = :profession"),
    // @NamedQuery(name = "Personne.findByPwd", query = "SELECT p FROM Personne p WHERE p.pwd = :pwd"),
    @NamedQuery(name = "Personne.findByRemarques", query = "SELECT p FROM Personne p WHERE p.remarques = :remarques"),
    @NamedQuery(name = "Personne.findBySexe", query = "SELECT p FROM Personne p WHERE p.sexe = :sexe"),
    @NamedQuery(name = "Personne.findBySituationMatrimoniale", query = "SELECT p FROM Personne p WHERE p.situationMatrimoniale = :situationMatrimoniale"),
    @NamedQuery(name = "Personne.findByStatut", query = "SELECT p FROM Personne p WHERE p.statut = :statut"),
    @NamedQuery(name = "Personne.findByUserCreate", query = "SELECT p FROM Personne p WHERE p.userCreate = :userCreate"),
    @NamedQuery(name = "Personne.findByUserDelete", query = "SELECT p FROM Personne p WHERE p.userDelete = :userDelete"),
    @NamedQuery(name = "Personne.findByAutorisationDeletes", query = "SELECT p FROM Personne p WHERE p.autorisationDeletes = :autorisationDeletes"),
    // @NamedQuery(name = "Personne.findByAutorisationDeletes1", query = "SELECT p FROM Personne p WHERE p.autorisationDeletes1 = :autorisationDeletes1"),
    //@NamedQuery(name = "Personne.findByDateEnregistrement1", query = "SELECT p FROM Personne p WHERE p.dateEnregistrement1 = :dateEnregistrement1"),
    // @NamedQuery(name = "Personne.findByUserCreate1", query = "SELECT p FROM Personne p WHERE p.userCreate1 = :userCreate1"),
    // @NamedQuery(name = "Personne.findByDateNaissance1", query = "SELECT p FROM Personne p WHERE p.dateNaissance1 = :dateNaissance1"),
    // @NamedQuery(name = "Personne.findByDeleteDate1", query = "SELECT p FROM Personne p WHERE p.deleteDate1 = :deleteDate1"),
    // @NamedQuery(name = "Personne.findByIndicatifPays1", query = "SELECT p FROM Personne p WHERE p.indicatifPays1 = :indicatifPays1"),
    // @NamedQuery(name = "Personne.findByCompteActif1", query = "SELECT p FROM Personne p WHERE p.compteActif1 = :compteActif1"),
    //@NamedQuery(name = "Personne.findByLastDatePwdModif1", query = "SELECT p FROM Personne p WHERE p.lastDatePwdModif1 = :lastDatePwdModif1"),
    // @NamedQuery(name = "Personne.findByLastModifDate1", query = "SELECT p FROM Personne p WHERE p.lastModifDate1 = :lastModifDate1"),
    @NamedQuery(name = "Personne.findByLastModifiedBy", query = "SELECT p FROM Personne p WHERE p.lastModifiedBy = :lastModifiedBy"),
    @NamedQuery(name = "Personne.findByLastModifiedDate", query = "SELECT p FROM Personne p WHERE p.lastModifiedDate = :lastModifiedDate"),
    //@NamedQuery(name = "Personne.findByLastUserModif1", query = "SELECT p FROM Personne p WHERE p.lastUserModif1 = :lastUserModif1"),
    @NamedQuery(name = "Personne.findByLastlogin", query = "SELECT p FROM Personne p WHERE p.lastlogin = :lastlogin"),
    // @NamedQuery(name = "Personne.findByLieuNaissance1", query = "SELECT p FROM Personne p WHERE p.lieuNaissance1 = :lieuNaissance1"),
    //@NamedQuery(name = "Personne.findByNbreEnfant1", query = "SELECT p FROM Personne p WHERE p.nbreEnfant1 = :nbreEnfant1"),
    // @NamedQuery(name = "Personne.findBySituationMatrimoniale1", query = "SELECT p FROM Personne p WHERE p.situationMatrimoniale1 = :situationMatrimoniale1"),
    @NamedQuery(name = "Personne.findByUpdatedAt", query = "SELECT p FROM Personne p WHERE p.updatedAt = :updatedAt"), // @NamedQuery(name = "Personne.findByUserDelete1", query = "SELECT p FROM Personne p WHERE p.userDelete1 = :userDelete1")
})
public class Personne implements Serializable {

    @Lob
    @Column(name = "usermenu_list")
    private byte[] usermenuList;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Column(name = "Bp")
    private String bp;
    @Column(name = "Email")
    private String email;
    @Column(name = "Fax")
    private String fax;
    @Column(name = "indicatif_pays")
    private String indicatifPays;
    @Column(name = "Quartier")
    private String quartier;
    @Column(name = "Region")
    private String region;
    @Column(name = "Tel")
    private String tel;
    @Column(name = "Ville")
    private String ville;
    @Column(name = "compte_actif")
    private Boolean compteActif;
    @Column(name = "date_enregistrement")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnregistrement;
    @Column(name = "date_naissance")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateNaissance;
    @Column(name = "delete_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleteDate;
    @Column(name = "last_date_pwdModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDatePwdModif;
    @Column(name = "last_modif_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifDate;
    @Column(name = "last_user_modif")
    private String lastUserModif;
    @Column(name = "lieu_naissance")
    private String lieuNaissance;
    @Basic(optional = false)
    @Column(name = "Matricule")
    private String userName;
    @Column(name = "nbre_enfant")
    private Integer nbreEnfant;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @Column(name = "Profession")
    private String profession;
    //@Column(name = "pwd")
    //private String pwd;
    @Column(name = "remarques")
    private String remarques;
    @Column(name = "sexe")
    private String sexe;
    @Column(name = "situation_matrimoniale")
    private String situationMatrimoniale;
    @Column(name = "statut")
    private String statut;
    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "autorisation_deletes")
    private Boolean autorisationDeletes;

    @Basic(optional = false)

    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    @Column(name = "last_modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Column(name = "lastlogin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastlogin;
    ;

   
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(name = "user_delete")
    private String userDelete;
    @JoinColumn(name = "Pays_id", referencedColumnName = "Id")
    @ManyToOne
    private Pays paysid;
    @JoinColumn(name = "profilid", referencedColumnName = "Id")
    @ManyToOne
    private Profil profilid;
    @JoinColumn(name = "Religion_id", referencedColumnName = "Id")
    @ManyToOne
    private Religion religionid;
    @JoinColumn(name = "Role_id", referencedColumnName = "Id")
    @ManyToOne
    private Role roleid;
    @JoinColumn(name = "Titre_id", referencedColumnName = "id")
    @ManyToOne
    private Titre titreid;
    @JsonIgnore
    @OneToMany(mappedBy = "personneid")
    private Collection<Photo> photoCollection;
    @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne
    private Boutique boutique;

    public Personne() {
    }

    public Personne(Long id) {
        this.id = id;
    }

    public Personne(Long id, String matricule, boolean compteActif1) {
        this.id = id;
        this.userName = matricule;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getIndicatifPays() {
        return indicatifPays;
    }

    public void setIndicatifPays(String indicatifPays) {
        this.indicatifPays = indicatifPays;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Boolean getCompteActif() {
        return compteActif;
    }

    public void setCompteActif(Boolean compteActif) {
        this.compteActif = compteActif;
    }

    public Date getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(Date dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Date getLastDatePwdModif() {
        return lastDatePwdModif;
    }

    public void setLastDatePwdModif(Date lastDatePwdModif) {
        this.lastDatePwdModif = lastDatePwdModif;
    }

    public Date getLastModifDate() {
        return lastModifDate;
    }

    public void setLastModifDate(Date lastModifDate) {
        this.lastModifDate = lastModifDate;
    }

    public String getLastUserModif() {
        return lastUserModif;
    }

    public void setLastUserModif(String lastUserModif) {
        this.lastUserModif = lastUserModif;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNbreEnfant(Integer nbreEnfant) {
        this.nbreEnfant = nbreEnfant;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

//    public String getPwd() {
//        return pwd;
//    }
//
//    public void setPwd(String pwd) {
//        this.pwd = pwd;
//    }
    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getSituationMatrimoniale() {
        return situationMatrimoniale;
    }

    public void setSituationMatrimoniale(String situationMatrimoniale) {
        this.situationMatrimoniale = situationMatrimoniale;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getUserCreate() {
        return userCreate;
    }

    public void setUserCreate(String userCreate) {
        this.userCreate = userCreate;
    }

    public String getUserDelete() {
        return userDelete;
    }

    public void setUserDelete(String userDelete) {
        this.userDelete = userDelete;
    }

    public Boolean getAutorisationDeletes() {
        return autorisationDeletes;
    }

    public void setAutorisationDeletes(Boolean autorisationDeletes) {
        this.autorisationDeletes = autorisationDeletes;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastlogin() {
        return lastlogin;
    }

    public void setLastlogin(Date lastlogin) {
        this.lastlogin = lastlogin;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public byte[] getUsermenuList() {
        return usermenuList;
    }

    public void setUsermenuList(byte[] usermenuList) {
        this.usermenuList = usermenuList;
    }

    public Pays getPaysid() {
        return paysid;
    }

    public void setPaysid(Pays paysid) {
        this.paysid = paysid;
    }

    public Profil getProfilid() {
        return profilid;
    }

    public void setProfilid(Profil profilid) {
        this.profilid = profilid;
    }

    public Religion getReligionid() {
        return religionid;
    }

    public void setReligionid(Religion religionid) {
        this.religionid = religionid;
    }

    public Role getRoleid() {
        return roleid;
    }

    public void setRoleid(Role roleid) {
        this.roleid = roleid;
    }

    public Titre getTitreid() {
        return titreid;
    }

    public void setTitreid(Titre titreid) {
        this.titreid = titreid;
    }

    public Collection<Photo> getPhotoCollection() {
        return photoCollection;
    }

    public void setPhotoCollection(Collection<Photo> photoCollection) {
        this.photoCollection = photoCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public Boutique getBoutique() {
        return boutique;
    }

    public void setBoutique(Boutique boutique) {
        this.boutique = boutique;
    }
    
    

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Personne)) {
            return false;
        }
        Personne other = (Personne) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Personne[ id=" + id + " ]";
    }

}
