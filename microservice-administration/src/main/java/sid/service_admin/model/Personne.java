/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "personne")
@NamedQueries({
    @NamedQuery(name = "Personne.findAll", query = "SELECT p FROM Personne p"),
    @NamedQuery(name = "Personne.findById", query = "SELECT p FROM Personne p WHERE p.id = :id"),
    @NamedQuery(name = "Personne.findByBp", query = "SELECT p FROM Personne p WHERE p.bp = :bp"),
    @NamedQuery(name = "Personne.findByEmail", query = "SELECT p FROM Personne p WHERE p.email = :email"),
    @NamedQuery(name = "Personne.findByFax", query = "SELECT p FROM Personne p WHERE p.fax = :fax"),
    @NamedQuery(name = "Personne.findByIndicatifPays", query = "SELECT p FROM Personne p WHERE p.codePays= :indicatifPays"),
    @NamedQuery(name = "Personne.findByQuartier", query = "SELECT p FROM Personne p WHERE p.quartier = :quartier"),
    @NamedQuery(name = "Personne.findByRegion", query = "SELECT p FROM Personne p WHERE p.region = :region"),
    @NamedQuery(name = "Personne.findByTel", query = "SELECT p FROM Personne p WHERE p.tel = :phoneNumber"),
    @NamedQuery(name = "Personne.findByVille", query = "SELECT p FROM Personne p WHERE p.ville = :ville"),
    //@NamedQuery(name = "Personne.findByCompteActif", query = "SELECT p FROM Personne p WHERE p.compteActif = :compteActif"),
    @NamedQuery(name = "Personne.findByCreatedAt", query = "SELECT p FROM Personne p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Personne.findByDateNaissance", query = "SELECT p FROM Personne p WHERE p.dateNaissance = :dateNaissance"),
    @NamedQuery(name = "Personne.findByDeleteDate", query = "SELECT p FROM Personne p WHERE p.deleteDate = :deleteDate"),
    @NamedQuery(name = "Personne.findByLastDatePwdModif", query = "SELECT p FROM Personne p WHERE p.lastDatePwdModif = :lastDatePwdModif"),
    @NamedQuery(name = "Personne.findByLastModifDate", query = "SELECT p FROM Personne p WHERE p.lastModifDate = :lastModifDate"),
    @NamedQuery(name = "Personne.findByLastUserModif", query = "SELECT p FROM Personne p WHERE p.lastUserModif = :lastUserModif"),
    @NamedQuery(name = "Personne.findByLieuNaissance", query = "SELECT p FROM Personne p WHERE p.lieuNaissance = :lieuNaissance"),
    @NamedQuery(name = "Personne.findByUserName", query = "SELECT p FROM Personne p WHERE p.userName = :userName"),
    @NamedQuery(name = "Personne.findByNbreEnfant", query = "SELECT p FROM Personne p WHERE p.nbreEnfant = :nbreEnfant"),
    @NamedQuery(name = "Personne.findByFirstName", query = "SELECT p FROM Personne p WHERE p.firstName = :firstName"),
    @NamedQuery(name = "Personne.findByLastName", query = "SELECT p FROM Personne p WHERE p.lastname = :lastName"),
    @NamedQuery(name = "Personne.findByProfession", query = "SELECT p FROM Personne p WHERE p.profession = :profession"),
    @NamedQuery(name = "Personne.findByPassword", query = "SELECT p FROM Personne p WHERE p.password = :password"),
    @NamedQuery(name = "Personne.findByRemarques", query = "SELECT p FROM Personne p WHERE p.remarques = :remarques"),
    @NamedQuery(name = "Personne.findBySexe", query = "SELECT p FROM Personne p WHERE p.sexe = :sexe"),
    @NamedQuery(name = "Personne.findBySituationMatrimoniale", query = "SELECT p FROM Personne p WHERE p.situationMatrimoniale = :situationMatrimoniale"),
    @NamedQuery(name = "Personne.findByStatut", query = "SELECT p FROM Personne p WHERE p.statut = :statut"),
    @NamedQuery(name = "Personne.findByCreatedBy", query = "SELECT p FROM Personne p WHERE p.createdBy = :createdBy"),
    @NamedQuery(name = "Personne.findByUserDelete", query = "SELECT p FROM Personne p WHERE p.userDelete = :userDelete"),
    @NamedQuery(name = "Personne.findByAutorisationDeletes", query = "SELECT p FROM Personne p WHERE p.autorisationDeletes = :autorisationDeletes")})
public class Personne implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Long id;
    @Column(name = "Bp")
    private String bp;
    @Column(name = "Email", nullable = true, unique = true)
    private String email;
    @Column(name = "Fax")
    private String fax;
    @Column(name = "indicatifPays")
    private String codePays;
    @Column(name = "Quartier")
    private String quartier;
    @Column(name = "Region")
    private String region;
    @Column(name = "Tel")
    private String tel;
    @Column(name = "Ville")
    private String ville;
    @Column(name = "CompteActif", nullable = false)
    private Boolean isActive = Boolean.TRUE;
    @Column(name = "DateEnregistrement")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "DateNaissance")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateNaissance;
    @Column(name = "deleteDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleteDate;
    @Column(name = "lastDatePwdModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDatePwdModif;
    @Column(name = "lastModifDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifDate;
    @Column(name = "lastUserModif")
    private String lastUserModif;
    @Column(name = "LieuNaissance")
    private String lieuNaissance;
    @Basic(optional = false)
    @Column(name = "Matricule")
    private String userName;
    @Column(name = "NbreEnfant")
    private Integer nbreEnfant;
    @Column(name = "Nom")
    private String firstName;
    @Column(name = "Prenom")
    private String lastname;
    @Column(name = "Profession")
    private String profession;
    @Column(name = "pwd")
    private String password;
    @Column(name = "Remarques")
    private String remarques;
    @Column(name = "Sexe")
    private String sexe;
    @Column(name = "SituationMatrimoniale")
    private String situationMatrimoniale;
    @Column(name = "statut")
    private String statut;
    @Column(name = "userCreate")
    private String createdBy;
    @Column(name = "userDelete")
    private String userDelete;
    @Column(name = "autorisationDeletes")
    private Boolean autorisationDeletes;
    // Vrai si le mot de passe actuel a ete assigne par un admin (creation ou
    // reinitialisation), pas choisi par le titulaire du compte - force un
    // changement a la prochaine connexion (voir MustChangePasswordFilter).
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = Boolean.FALSE;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personne", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("personne")
    private List<Usermodule> usermoduleList;

    @JsonIgnoreProperties("personne")
    private List<Usermenu> usermenuList;
    @JoinColumn(name = "Pays_id", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Pays paysid;
    @JoinColumn(name = "profilid", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Profil profilid;
    @JoinColumn(name = "Religion_id", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Religion religionid;
    @JoinColumn(name = "Role_id", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Roles roleid;
    @JoinColumn(name = "Titre_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Titre titreid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personne", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("personne")
    private List<Photo> photoList;
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "personne")
//    private Employeur employeur;
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastlogin;

    @Column
    private String lastModifiedBy;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

      @JoinColumn(name = "Boutiqueid", referencedColumnName = "Id")
    @ManyToOne(optional = true)
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Boutique boutique;

    // null = administrateur systeme (super-admin ou admin systeme), renseigne = administrateur/utilisateur de compagnie
    @JoinColumn(name = "Compagnieid", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "personneList"})
    private Compagnie compagnie;


    public Personne(String userName) {

        this.userName = userName;
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

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
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

    public Integer getNbreEnfant() {
        return nbreEnfant;
    }

    public void setNbreEnfant(Integer nbreEnfant) {
        this.nbreEnfant = nbreEnfant;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

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

    public List<Usermodule> getUsermoduleList() {
        return usermoduleList;
    }

    public void setUsermoduleList(List<Usermodule> usermoduleList) {
        this.usermoduleList = usermoduleList;
    }

    public List<Usermenu> getUsermenuList() {
        return usermenuList;
    }

    public void setUsermenuList(List<Usermenu> usermenuList) {
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

    public Roles getRoleid() {
        return roleid;
    }

    public void setRoleid(Roles roleid) {
        this.roleid = roleid;
    }

    public Titre getTitreid() {
        return titreid;
    }

    public void setTitreid(Titre titreid) {
        this.titreid = titreid;
    }

    public List<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
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
        return "sid.service_admin.model.Personne[ id=" + id + " ]";
    }

}
