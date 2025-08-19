/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "property")
@NamedQueries({
    @NamedQuery(name = "Property.findAll", query = "SELECT p FROM Property p"),
    @NamedQuery(name = "Property.findByPropertyId", query = "SELECT p FROM Property p WHERE p.propertyId = :propertyId"),
    @NamedQuery(name = "Property.findByResponsableSignature", query = "SELECT p FROM Property p WHERE p.responsableSignature = :responsableSignature"),
    @NamedQuery(name = "Property.findByActifMajDepense", query = "SELECT p FROM Property p WHERE p.actifMajDepense = :actifMajDepense"),
    @NamedQuery(name = "Property.findByActifMajResource", query = "SELECT p FROM Property p WHERE p.actifMajResource = :actifMajResource"),
    @NamedQuery(name = "Property.findByActifNombre", query = "SELECT p FROM Property p WHERE p.actifNombre = :actifNombre"),
    @NamedQuery(name = "Property.findByActifSuppressionCommande", query = "SELECT p FROM Property p WHERE p.actifSuppressionCommande = :actifSuppressionCommande"),
    @NamedQuery(name = "Property.findByActifSuppressionDepense", query = "SELECT p FROM Property p WHERE p.actifSuppressionDepense = :actifSuppressionDepense"),
    @NamedQuery(name = "Property.findByActifSuppressionLivraison", query = "SELECT p FROM Property p WHERE p.actifSuppressionLivraison = :actifSuppressionLivraison"),
    @NamedQuery(name = "Property.findByActifSuppressionRessource", query = "SELECT p FROM Property p WHERE p.actifSuppressionRessource = :actifSuppressionRessource"),
    @NamedQuery(name = "Property.findByAllSalarier", query = "SELECT p FROM Property p WHERE p.allSalarier = :allSalarier"),
    @NamedQuery(name = "Property.findByAncienete", query = "SELECT p FROM Property p WHERE p.ancienete = :ancienete"),
    @NamedQuery(name = "Property.findByBlockModifHeureSup", query = "SELECT p FROM Property p WHERE p.blockModifHeureSup = :blockModifHeureSup"),
    @NamedQuery(name = "Property.findByBlockModifTache", query = "SELECT p FROM Property p WHERE p.blockModifTache = :blockModifTache"),
    @NamedQuery(name = "Property.findByCacherAjoutByMois", query = "SELECT p FROM Property p WHERE p.cacherAjoutByMois = :cacherAjoutByMois"),
    @NamedQuery(name = "Property.findByCompte", query = "SELECT p FROM Property p WHERE p.compte = :compte"),
    @NamedQuery(name = "Property.findByDateTirage", query = "SELECT p FROM Property p WHERE p.dateTirage = :dateTirage"),
    @NamedQuery(name = "Property.findByEnvoiMail", query = "SELECT p FROM Property p WHERE p.envoiMail = :envoiMail"),
    @NamedQuery(name = "Property.findByEnvoiSmsForDiscipline", query = "SELECT p FROM Property p WHERE p.envoiSmsForDiscipline = :envoiSmsForDiscipline"),
    @NamedQuery(name = "Property.findByEnvoiSmsForPayement", query = "SELECT p FROM Property p WHERE p.envoiSmsForPayement = :envoiSmsForPayement"),
    @NamedQuery(name = "Property.findByFormJurique", query = "SELECT p FROM Property p WHERE p.formJurique = :formJurique"),
    @NamedQuery(name = "Property.findByFormatPaie", query = "SELECT p FROM Property p WHERE p.formatPaie = :formatPaie"),
    @NamedQuery(name = "Property.findByFormuleCacForIrpp", query = "SELECT p FROM Property p WHERE p.formuleCacForIrpp = :formuleCacForIrpp"),
    @NamedQuery(name = "Property.findByHauteurImage", query = "SELECT p FROM Property p WHERE p.hauteurImage = :hauteurImage"),
    @NamedQuery(name = "Property.findByImageFillGramme", query = "SELECT p FROM Property p WHERE p.imageFillGramme = :imageFillGramme"),
    @NamedQuery(name = "Property.findByImpot", query = "SELECT p FROM Property p WHERE p.impot = :impot"),
    @NamedQuery(name = "Property.findByLargeurImage", query = "SELECT p FROM Property p WHERE p.largeurImage = :largeurImage"),
    @NamedQuery(name = "Property.findByLogin", query = "SELECT p FROM Property p WHERE p.login = :login"),
    @NamedQuery(name = "Property.findByManyBank", query = "SELECT p FROM Property p WHERE p.manyBank = :manyBank"),
    @NamedQuery(name = "Property.findByMatrEtudiant", query = "SELECT p FROM Property p WHERE p.matrEtudiant = :matrEtudiant"),
    @NamedQuery(name = "Property.findByMatriculeAuto", query = "SELECT p FROM Property p WHERE p.matriculeAuto = :matriculeAuto"),
    @NamedQuery(name = "Property.findByMonaie", query = "SELECT p FROM Property p WHERE p.monaie = :monaie"),
    @NamedQuery(name = "Property.findByNombreAutorisantCongerAdministratif", query = "SELECT p FROM Property p WHERE p.nombreAutorisantCongerAdministratif = :nombreAutorisantCongerAdministratif"),
    @NamedQuery(name = "Property.findByNombreAutorisantCongerAnnuel", query = "SELECT p FROM Property p WHERE p.nombreAutorisantCongerAnnuel = :nombreAutorisantCongerAnnuel"),
    @NamedQuery(name = "Property.findByNombreAutorisantCongerMaternite", query = "SELECT p FROM Property p WHERE p.nombreAutorisantCongerMaternite = :nombreAutorisantCongerMaternite"),
    @NamedQuery(name = "Property.findByNombreAutorisantCongerPartenite", query = "SELECT p FROM Property p WHERE p.nombreAutorisantCongerPartenite = :nombreAutorisantCongerPartenite"),
    @NamedQuery(name = "Property.findByNombreDemandeTraiteByMois", query = "SELECT p FROM Property p WHERE p.nombreDemandeTraiteByMois = :nombreDemandeTraiteByMois"),
    @NamedQuery(name = "Property.findByPaiePrime", query = "SELECT p FROM Property p WHERE p.paiePrime = :paiePrime"),
    @NamedQuery(name = "Property.findByPwd", query = "SELECT p FROM Property p WHERE p.pwd = :pwd"),
    @NamedQuery(name = "Property.findBySalaire", query = "SELECT p FROM Property p WHERE p.salaire = :salaire"),
    @NamedQuery(name = "Property.findBySalaireBase", query = "SELECT p FROM Property p WHERE p.salaireBase = :salaireBase"),
    @NamedQuery(name = "Property.findBySalaireBrute", query = "SELECT p FROM Property p WHERE p.salaireBrute = :salaireBrute"),
    @NamedQuery(name = "Property.findBySalairePlafonne", query = "SELECT p FROM Property p WHERE p.salairePlafonne = :salairePlafonne"),
    @NamedQuery(name = "Property.findBySalarierHaveImpot", query = "SELECT p FROM Property p WHERE p.salarierHaveImpot = :salarierHaveImpot"),
    @NamedQuery(name = "Property.findBySalarierIsNotHaveImpot", query = "SELECT p FROM Property p WHERE p.salarierIsNotHaveImpot = :salarierIsNotHaveImpot"),
    @NamedQuery(name = "Property.findBySenderId", query = "SELECT p FROM Property p WHERE p.senderId = :senderId"),
    @NamedQuery(name = "Property.findBySepAnnee", query = "SELECT p FROM Property p WHERE p.sepAnnee = :sepAnnee"),
    @NamedQuery(name = "Property.findBySepCodeEtab", query = "SELECT p FROM Property p WHERE p.sepCodeEtab = :sepCodeEtab"),
    @NamedQuery(name = "Property.findBySepCycle", query = "SELECT p FROM Property p WHERE p.sepCycle = :sepCycle"),
    @NamedQuery(name = "Property.findBySepFaculte", query = "SELECT p FROM Property p WHERE p.sepFaculte = :sepFaculte"),
    @NamedQuery(name = "Property.findBySepFiliere", query = "SELECT p FROM Property p WHERE p.sepFiliere = :sepFiliere"),
    @NamedQuery(name = "Property.findBySepNiveau", query = "SELECT p FROM Property p WHERE p.sepNiveau = :sepNiveau"),
    @NamedQuery(name = "Property.findBySepNumero", query = "SELECT p FROM Property p WHERE p.sepNumero = :sepNumero"),
    @NamedQuery(name = "Property.findBySepPays", query = "SELECT p FROM Property p WHERE p.sepPays = :sepPays"),
    @NamedQuery(name = "Property.findBySepSexe", query = "SELECT p FROM Property p WHERE p.sepSexe = :sepSexe"),
    @NamedQuery(name = "Property.findBySeuilAudioVisuel", query = "SELECT p FROM Property p WHERE p.seuilAudioVisuel = :seuilAudioVisuel"),
    @NamedQuery(name = "Property.findBySeuilCreditFoncier", query = "SELECT p FROM Property p WHERE p.seuilCreditFoncier = :seuilCreditFoncier"),
    @NamedQuery(name = "Property.findBySeuilIrrp", query = "SELECT p FROM Property p WHERE p.seuilIrrp = :seuilIrrp"),
    @NamedQuery(name = "Property.findBySeuilTaxCommunal", query = "SELECT p FROM Property p WHERE p.seuilTaxCommunal = :seuilTaxCommunal"),
    @NamedQuery(name = "Property.findBySizeAnnee", query = "SELECT p FROM Property p WHERE p.sizeAnnee = :sizeAnnee"),
    @NamedQuery(name = "Property.findBySizeCodeEtab", query = "SELECT p FROM Property p WHERE p.sizeCodeEtab = :sizeCodeEtab"),
    @NamedQuery(name = "Property.findBySizeCycle", query = "SELECT p FROM Property p WHERE p.sizeCycle = :sizeCycle"),
    @NamedQuery(name = "Property.findBySizeFaculte", query = "SELECT p FROM Property p WHERE p.sizeFaculte = :sizeFaculte"),
    @NamedQuery(name = "Property.findBySizeFiliere", query = "SELECT p FROM Property p WHERE p.sizeFiliere = :sizeFiliere"),
    @NamedQuery(name = "Property.findBySizeNiveau", query = "SELECT p FROM Property p WHERE p.sizeNiveau = :sizeNiveau"),
    @NamedQuery(name = "Property.findBySizeNumero", query = "SELECT p FROM Property p WHERE p.sizeNumero = :sizeNumero"),
    @NamedQuery(name = "Property.findBySizePays", query = "SELECT p FROM Property p WHERE p.sizePays = :sizePays"),
    @NamedQuery(name = "Property.findBySizeSexe", query = "SELECT p FROM Property p WHERE p.sizeSexe = :sizeSexe"),
    @NamedQuery(name = "Property.findByTauxAccidentTravail", query = "SELECT p FROM Property p WHERE p.tauxAccidentTravail = :tauxAccidentTravail"),
    @NamedQuery(name = "Property.findByTauxAllocationFamilial", query = "SELECT p FROM Property p WHERE p.tauxAllocationFamilial = :tauxAllocationFamilial"),
    @NamedQuery(name = "Property.findByTauxAutresAncienete", query = "SELECT p FROM Property p WHERE p.tauxAutresAncienete = :tauxAutresAncienete"),
    @NamedQuery(name = "Property.findByTauxCreditFrontier", query = "SELECT p FROM Property p WHERE p.tauxCreditFrontier = :tauxCreditFrontier"),
    @NamedQuery(name = "Property.findByTauxEntreprise", query = "SELECT p FROM Property p WHERE p.tauxEntreprise = :tauxEntreprise"),
    @NamedQuery(name = "Property.findByTauxFNE", query = "SELECT p FROM Property p WHERE p.tauxFNE = :tauxFNE"),
    @NamedQuery(name = "Property.findByTauxPremierAncienete", query = "SELECT p FROM Property p WHERE p.tauxPremierAncienete = :tauxPremierAncienete"),
    @NamedQuery(name = "Property.findByTauxSalarier", query = "SELECT p FROM Property p WHERE p.tauxSalarier = :tauxSalarier"),
    @NamedQuery(name = "Property.findByTotalCredit", query = "SELECT p FROM Property p WHERE p.totalCredit = :totalCredit"),
    @NamedQuery(name = "Property.findByEntreprise", query = "SELECT p FROM Property p WHERE p.entreprise = :entreprise"),
    @NamedQuery(name = "Property.findByMulticaisse", query = "SELECT p FROM Property p WHERE p.multicaisse = :multicaisse"),
    @NamedQuery(name = "Property.findByImprime", query = "SELECT p FROM Property p WHERE p.imprime = :imprime"),
    @NamedQuery(name = "Property.findByActifProduitNonStocker", query = "SELECT p FROM Property p WHERE p.actifProduitNonStocker = :actifProduitNonStocker"),
    @NamedQuery(name = "Property.findByActifRemiseTicket", query = "SELECT p FROM Property p WHERE p.actifRemiseTicket = :actifRemiseTicket"),
    @NamedQuery(name = "Property.findByCheminTicket", query = "SELECT p FROM Property p WHERE p.cheminTicket = :cheminTicket"),
    @NamedQuery(name = "Property.findByClientTicket", query = "SELECT p FROM Property p WHERE p.clientTicket = :clientTicket"),
    @NamedQuery(name = "Property.findByControleStock", query = "SELECT p FROM Property p WHERE p.controleStock = :controleStock"),
    @NamedQuery(name = "Property.findByDestinationAdresse", query = "SELECT p FROM Property p WHERE p.destinationAdresse = :destinationAdresse"),
    @NamedQuery(name = "Property.findByHistoriqueCaisse", query = "SELECT p FROM Property p WHERE p.historiqueCaisse = :historiqueCaisse"),
    @NamedQuery(name = "Property.findByLimiteStocke", query = "SELECT p FROM Property p WHERE p.limiteStocke = :limiteStocke"),
    @NamedQuery(name = "Property.findByPassword", query = "SELECT p FROM Property p WHERE p.password = :password"),
    @NamedQuery(name = "Property.findByPortSTMP", query = "SELECT p FROM Property p WHERE p.portSTMP = :portSTMP"),
    @NamedQuery(name = "Property.findByServeurSTMP", query = "SELECT p FROM Property p WHERE p.serveurSTMP = :serveurSTMP"),
    @NamedQuery(name = "Property.findByUsername", query = "SELECT p FROM Property p WHERE p.username = :username"),
    @NamedQuery(name = "Property.findByBoutique", query = "SELECT p FROM Property p WHERE p.boutique = :boutique"),
    @NamedQuery(name = "Property.findByEnvoitSms", query = "SELECT p FROM Property p WHERE p.envoitSms = :envoitSms"),
    @NamedQuery(name = "Property.findBySms", query = "SELECT p FROM Property p WHERE p.sms = :sms"),
    @NamedQuery(name = "Property.findByTel", query = "SELECT p FROM Property p WHERE p.tel = :tel"),
    @NamedQuery(name = "Property.findByPrixAchatInventaire", query = "SELECT p FROM Property p WHERE p.prixAchatInventaire = :prixAchatInventaire"),
    @NamedQuery(name = "Property.findByPrixVenteInventaire", query = "SELECT p FROM Property p WHERE p.prixVenteInventaire = :prixVenteInventaire"),
    @NamedQuery(name = "Property.findByBackupData", query = "SELECT p FROM Property p WHERE p.backupData = :backupData"),
    @NamedQuery(name = "Property.findByDbMysql", query = "SELECT p FROM Property p WHERE p.dbMysql = :dbMysql"),
    @NamedQuery(name = "Property.findByHeure", query = "SELECT p FROM Property p WHERE p.heure = :heure"),
    @NamedQuery(name = "Property.findByPasswordMysql", query = "SELECT p FROM Property p WHERE p.passwordMysql = :passwordMysql"),
    @NamedQuery(name = "Property.findByPathFileMysql", query = "SELECT p FROM Property p WHERE p.pathFileMysql = :pathFileMysql"),
    @NamedQuery(name = "Property.findByPortMysql", query = "SELECT p FROM Property p WHERE p.portMysql = :portMysql"),
    @NamedQuery(name = "Property.findByServerMysql", query = "SELECT p FROM Property p WHERE p.serverMysql = :serverMysql"),
    @NamedQuery(name = "Property.findByUserMysql", query = "SELECT p FROM Property p WHERE p.userMysql = :userMysql"),
    @NamedQuery(name = "Property.findByDestinationAdresse2", query = "SELECT p FROM Property p WHERE p.destinationAdresse2 = :destinationAdresse2"),
    @NamedQuery(name = "Property.findByDestinationAdresse3", query = "SELECT p FROM Property p WHERE p.destinationAdresse3 = :destinationAdresse3"),
    @NamedQuery(name = "Property.findByImpressionDirect", query = "SELECT p FROM Property p WHERE p.impressionDirect = :impressionDirect"),
    @NamedQuery(name = "Property.findByLimiteStockeMagasin", query = "SELECT p FROM Property p WHERE p.limiteStockeMagasin = :limiteStockeMagasin"),
    @NamedQuery(name = "Property.findByPortServeurDistance", query = "SELECT p FROM Property p WHERE p.portServeurDistance = :portServeurDistance"),
    @NamedQuery(name = "Property.findByServeurDistance", query = "SELECT p FROM Property p WHERE p.serveurDistance = :serveurDistance"),
    @NamedQuery(name = "Property.findByUseCaisseByPointVente", query = "SELECT p FROM Property p WHERE p.useCaisseByPointVente = :useCaisseByPointVente")})
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "property_id")
    private Long propertyId;
    @Column(name = "ResponsableSignature")
    private String responsableSignature;
    @Basic(optional = false)
    @Column(name = "actifMajDepense")
    private boolean actifMajDepense;
    @Basic(optional = false)
    @Column(name = "actifMajResource")
    private boolean actifMajResource;
    @Column(name = "actifNombre")
    private Boolean actifNombre;
    @Basic(optional = false)
    @Column(name = "actifSuppressionCommande")
    private boolean actifSuppressionCommande;
    @Basic(optional = false)
    @Column(name = "actifSuppressionDepense")
    private boolean actifSuppressionDepense;
    @Basic(optional = false)
    @Column(name = "actifSuppressionLivraison")
    private boolean actifSuppressionLivraison;
    @Basic(optional = false)
    @Column(name = "actifSuppressionRessource")
    private boolean actifSuppressionRessource;
    @Column(name = "allSalarier")
    private Boolean allSalarier;
    @Basic(optional = false)
    @Column(name = "ancienete")
    private int ancienete;
    @Column(name = "blockModifHeureSup")
    private Boolean blockModifHeureSup;
    @Column(name = "blockModifTache")
    private Boolean blockModifTache;
    @Basic(optional = false)
    @Column(name = "cacherAjoutByMois")
    private boolean cacherAjoutByMois;
    @Column(name = "compte")
    private String compte;
    @Column(name = "dateTirage")
    private Boolean dateTirage;
    @Column(name = "envoiMail")
    private Boolean envoiMail;
    @Column(name = "envoiSmsForDiscipline")
    private Boolean envoiSmsForDiscipline;
    @Column(name = "envoiSmsForPayement")
    private Boolean envoiSmsForPayement;
    @Column(name = "formJurique")
    private String formJurique;
    @Column(name = "formatPaie")
    private Integer formatPaie;
    @Basic(optional = false)
    @Column(name = "formuleCacForIrpp")
    private double formuleCacForIrpp;
    @Column(name = "hauteurImage")
    private Integer hauteurImage;
    @Column(name = "imageFillGramme")
    private Boolean imageFillGramme;
    @Basic(optional = false)
    @Column(name = "impot")
    private boolean impot;
    @Column(name = "largeurImage")
    private Integer largeurImage;
    @Column(name = "login")
    private String login;
    @Column(name = "manyBank")
    private Boolean manyBank;
    @Column(name = "matrEtudiant")
    private String matrEtudiant;
    @Column(name = "matriculeAuto")
    private Boolean matriculeAuto;
    @Column(name = "monaie")
    private String monaie;
    @Column(name = "nombreAutorisantCongerAdministratif")
    private Integer nombreAutorisantCongerAdministratif;
    @Column(name = "nombreAutorisantCongerAnnuel")
    private Integer nombreAutorisantCongerAnnuel;
    @Column(name = "nombreAutorisantCongerMaternite")
    private Integer nombreAutorisantCongerMaternite;
    @Column(name = "nombreAutorisantCongerPartenite")
    private Integer nombreAutorisantCongerPartenite;
    @Column(name = "nombreDemandeTraiteByMois")
    private Integer nombreDemandeTraiteByMois;
    @Column(name = "paiePrime")
    private Boolean paiePrime;
    @Column(name = "pwd")
    private String pwd;
    @Basic(optional = false)
    @Column(name = "salaire")
    private boolean salaire;
    @Column(name = "salaireBase")
    private Boolean salaireBase;
    @Column(name = "salaireBrute")
    private Boolean salaireBrute;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "salairePlafonne")
    private BigDecimal salairePlafonne;
    @Column(name = "salarierHaveImpot")
    private Boolean salarierHaveImpot;
    @Column(name = "salarierIsNotHaveImpot")
    private Boolean salarierIsNotHaveImpot;
    @Column(name = "senderId")
    private String senderId;
    @Column(name = "sepAnnee")
    private String sepAnnee;
    @Column(name = "sepCodeEtab")
    private String sepCodeEtab;
    @Column(name = "sepCycle")
    private String sepCycle;
    @Column(name = "sepFaculte")
    private String sepFaculte;
    @Column(name = "sepFiliere")
    private String sepFiliere;
    @Column(name = "sepNiveau")
    private String sepNiveau;
    @Column(name = "sepNumero")
    private String sepNumero;
    @Column(name = "sepPays")
    private String sepPays;
    @Column(name = "sepSexe")
    private String sepSexe;
    @Column(name = "seuilAudioVisuel")
    private BigDecimal seuilAudioVisuel;
    @Column(name = "seuilCreditFoncier")
    private BigDecimal seuilCreditFoncier;
    @Column(name = "seuilIrrp")
    private BigDecimal seuilIrrp;
    @Column(name = "seuilTaxCommunal")
    private BigDecimal seuilTaxCommunal;
    @Basic(optional = false)
    @Column(name = "sizeAnnee")
    private int sizeAnnee;
    @Basic(optional = false)
    @Column(name = "sizeCodeEtab")
    private int sizeCodeEtab;
    @Basic(optional = false)
    @Column(name = "sizeCycle")
    private int sizeCycle;
    @Basic(optional = false)
    @Column(name = "sizeFaculte")
    private int sizeFaculte;
    @Basic(optional = false)
    @Column(name = "sizeFiliere")
    private int sizeFiliere;
    @Basic(optional = false)
    @Column(name = "sizeNiveau")
    private int sizeNiveau;
    @Basic(optional = false)
    @Column(name = "sizeNumero")
    private int sizeNumero;
    @Basic(optional = false)
    @Column(name = "sizePays")
    private int sizePays;
    @Basic(optional = false)
    @Column(name = "sizeSexe")
    private int sizeSexe;
    @Column(name = "tauxAccidentTravail")
    private Double tauxAccidentTravail;
    @Column(name = "tauxAllocationFamilial")
    private Double tauxAllocationFamilial;
    @Column(name = "tauxAutresAncienete")
    private Double tauxAutresAncienete;
    @Column(name = "tauxCreditFrontier")
    private Double tauxCreditFrontier;
    @Column(name = "tauxEntreprise")
    private Double tauxEntreprise;
    @Column(name = "tauxFNE")
    private Double tauxFNE;
    @Column(name = "tauxPremierAncienete")
    private Double tauxPremierAncienete;
    @Column(name = "tauxSalarier")
    private Double tauxSalarier;
    @Basic(optional = false)
    @Column(name = "totalCredit")
    private int totalCredit;
    @Column(name = "Entreprise")
    private String entreprise;
    @Column(name = "multicaisse")
    private Boolean multicaisse;
    @Column(name = "imprime")
    private String imprime;
    @Column(name = "actifProduitNonStocker")
    private Boolean actifProduitNonStocker;
    @Column(name = "actifRemiseTicket")
    private Boolean actifRemiseTicket;
    @Column(name = "cheminTicket")
    private String cheminTicket;
    @Column(name = "clientTicket")
    private Boolean clientTicket;
    @Column(name = "controleStock")
    private Boolean controleStock;
    @Column(name = "destinationAdresse")
    private String destinationAdresse;
    @Column(name = "historiqueCaisse")
    private Boolean historiqueCaisse;
    @Column(name = "limiteStocke")
    private BigDecimal limiteStocke;
    @Column(name = "password")
    private String password;
    @Column(name = "portSTMP")
    private String portSTMP;
    @Column(name = "serveurSTMP")
    private String serveurSTMP;
    @Column(name = "username")
    private String username;
    @Column(name = "boutique")
    private String boutique;
    @Column(name = "envoitSms")
    private Boolean envoitSms;
    @Column(name = "sms")
    private Boolean sms;
    @Column(name = "tel")
    private String tel;
    @Column(name = "prixAchatInventaire")
    private Boolean prixAchatInventaire;
    @Column(name = "prixVenteInventaire")
    private Boolean prixVenteInventaire;
    @Column(name = "BackupData")
    private String backupData;
    @Column(name = "dbMysql")
    private String dbMysql;
    @Column(name = "heure")
    private String heure;
    @Column(name = "passwordMysql")
    private String passwordMysql;
    @Column(name = "pathFileMysql")
    private String pathFileMysql;
    @Column(name = "portMysql")
    private String portMysql;
    @Column(name = "serverMysql")
    private String serverMysql;
    @Column(name = "userMysql")
    private String userMysql;
    @Column(name = "destinationAdresse2")
    private String destinationAdresse2;
    @Column(name = "destinationAdresse3")
    private String destinationAdresse3;
    @Column(name = "impressionDirect")
    private Boolean impressionDirect;
    @Column(name = "limiteStockeMagasin")
    private BigDecimal limiteStockeMagasin;
    @Basic(optional = false)
    @Column(name = "portServeurDistance")
    private int portServeurDistance;
    @Column(name = "serveurDistance")
    private String serveurDistance;
    @Column(name = "useCaisseByPointVente")
    private Boolean useCaisseByPointVente;

    public Property() {
    }

    public Property(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Property(Long propertyId, boolean actifMajDepense, boolean actifMajResource, boolean actifSuppressionCommande, boolean actifSuppressionDepense, boolean actifSuppressionLivraison, boolean actifSuppressionRessource, int ancienete, boolean cacherAjoutByMois, double formuleCacForIrpp, boolean impot, boolean salaire, int sizeAnnee, int sizeCodeEtab, int sizeCycle, int sizeFaculte, int sizeFiliere, int sizeNiveau, int sizeNumero, int sizePays, int sizeSexe, int totalCredit, int portServeurDistance) {
        this.propertyId = propertyId;
        this.actifMajDepense = actifMajDepense;
        this.actifMajResource = actifMajResource;
        this.actifSuppressionCommande = actifSuppressionCommande;
        this.actifSuppressionDepense = actifSuppressionDepense;
        this.actifSuppressionLivraison = actifSuppressionLivraison;
        this.actifSuppressionRessource = actifSuppressionRessource;
        this.ancienete = ancienete;
        this.cacherAjoutByMois = cacherAjoutByMois;
        this.formuleCacForIrpp = formuleCacForIrpp;
        this.impot = impot;
        this.salaire = salaire;
        this.sizeAnnee = sizeAnnee;
        this.sizeCodeEtab = sizeCodeEtab;
        this.sizeCycle = sizeCycle;
        this.sizeFaculte = sizeFaculte;
        this.sizeFiliere = sizeFiliere;
        this.sizeNiveau = sizeNiveau;
        this.sizeNumero = sizeNumero;
        this.sizePays = sizePays;
        this.sizeSexe = sizeSexe;
        this.totalCredit = totalCredit;
        this.portServeurDistance = portServeurDistance;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getResponsableSignature() {
        return responsableSignature;
    }

    public void setResponsableSignature(String responsableSignature) {
        this.responsableSignature = responsableSignature;
    }

    public boolean getActifMajDepense() {
        return actifMajDepense;
    }

    public void setActifMajDepense(boolean actifMajDepense) {
        this.actifMajDepense = actifMajDepense;
    }

    public boolean getActifMajResource() {
        return actifMajResource;
    }

    public void setActifMajResource(boolean actifMajResource) {
        this.actifMajResource = actifMajResource;
    }

    public Boolean getActifNombre() {
        return actifNombre;
    }

    public void setActifNombre(Boolean actifNombre) {
        this.actifNombre = actifNombre;
    }

    public boolean getActifSuppressionCommande() {
        return actifSuppressionCommande;
    }

    public void setActifSuppressionCommande(boolean actifSuppressionCommande) {
        this.actifSuppressionCommande = actifSuppressionCommande;
    }

    public boolean getActifSuppressionDepense() {
        return actifSuppressionDepense;
    }

    public void setActifSuppressionDepense(boolean actifSuppressionDepense) {
        this.actifSuppressionDepense = actifSuppressionDepense;
    }

    public boolean getActifSuppressionLivraison() {
        return actifSuppressionLivraison;
    }

    public void setActifSuppressionLivraison(boolean actifSuppressionLivraison) {
        this.actifSuppressionLivraison = actifSuppressionLivraison;
    }

    public boolean getActifSuppressionRessource() {
        return actifSuppressionRessource;
    }

    public void setActifSuppressionRessource(boolean actifSuppressionRessource) {
        this.actifSuppressionRessource = actifSuppressionRessource;
    }

    public Boolean getAllSalarier() {
        return allSalarier;
    }

    public void setAllSalarier(Boolean allSalarier) {
        this.allSalarier = allSalarier;
    }

    public int getAncienete() {
        return ancienete;
    }

    public void setAncienete(int ancienete) {
        this.ancienete = ancienete;
    }

    public Boolean getBlockModifHeureSup() {
        return blockModifHeureSup;
    }

    public void setBlockModifHeureSup(Boolean blockModifHeureSup) {
        this.blockModifHeureSup = blockModifHeureSup;
    }

    public Boolean getBlockModifTache() {
        return blockModifTache;
    }

    public void setBlockModifTache(Boolean blockModifTache) {
        this.blockModifTache = blockModifTache;
    }

    public boolean getCacherAjoutByMois() {
        return cacherAjoutByMois;
    }

    public void setCacherAjoutByMois(boolean cacherAjoutByMois) {
        this.cacherAjoutByMois = cacherAjoutByMois;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public Boolean getDateTirage() {
        return dateTirage;
    }

    public void setDateTirage(Boolean dateTirage) {
        this.dateTirage = dateTirage;
    }

    public Boolean getEnvoiMail() {
        return envoiMail;
    }

    public void setEnvoiMail(Boolean envoiMail) {
        this.envoiMail = envoiMail;
    }

    public Boolean getEnvoiSmsForDiscipline() {
        return envoiSmsForDiscipline;
    }

    public void setEnvoiSmsForDiscipline(Boolean envoiSmsForDiscipline) {
        this.envoiSmsForDiscipline = envoiSmsForDiscipline;
    }

    public Boolean getEnvoiSmsForPayement() {
        return envoiSmsForPayement;
    }

    public void setEnvoiSmsForPayement(Boolean envoiSmsForPayement) {
        this.envoiSmsForPayement = envoiSmsForPayement;
    }

    public String getFormJurique() {
        return formJurique;
    }

    public void setFormJurique(String formJurique) {
        this.formJurique = formJurique;
    }

    public Integer getFormatPaie() {
        return formatPaie;
    }

    public void setFormatPaie(Integer formatPaie) {
        this.formatPaie = formatPaie;
    }

    public double getFormuleCacForIrpp() {
        return formuleCacForIrpp;
    }

    public void setFormuleCacForIrpp(double formuleCacForIrpp) {
        this.formuleCacForIrpp = formuleCacForIrpp;
    }

    public Integer getHauteurImage() {
        return hauteurImage;
    }

    public void setHauteurImage(Integer hauteurImage) {
        this.hauteurImage = hauteurImage;
    }

    public Boolean getImageFillGramme() {
        return imageFillGramme;
    }

    public void setImageFillGramme(Boolean imageFillGramme) {
        this.imageFillGramme = imageFillGramme;
    }

    public boolean getImpot() {
        return impot;
    }

    public void setImpot(boolean impot) {
        this.impot = impot;
    }

    public Integer getLargeurImage() {
        return largeurImage;
    }

    public void setLargeurImage(Integer largeurImage) {
        this.largeurImage = largeurImage;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Boolean getManyBank() {
        return manyBank;
    }

    public void setManyBank(Boolean manyBank) {
        this.manyBank = manyBank;
    }

    public String getMatrEtudiant() {
        return matrEtudiant;
    }

    public void setMatrEtudiant(String matrEtudiant) {
        this.matrEtudiant = matrEtudiant;
    }

    public Boolean getMatriculeAuto() {
        return matriculeAuto;
    }

    public void setMatriculeAuto(Boolean matriculeAuto) {
        this.matriculeAuto = matriculeAuto;
    }

    public String getMonaie() {
        return monaie;
    }

    public void setMonaie(String monaie) {
        this.monaie = monaie;
    }

    public Integer getNombreAutorisantCongerAdministratif() {
        return nombreAutorisantCongerAdministratif;
    }

    public void setNombreAutorisantCongerAdministratif(Integer nombreAutorisantCongerAdministratif) {
        this.nombreAutorisantCongerAdministratif = nombreAutorisantCongerAdministratif;
    }

    public Integer getNombreAutorisantCongerAnnuel() {
        return nombreAutorisantCongerAnnuel;
    }

    public void setNombreAutorisantCongerAnnuel(Integer nombreAutorisantCongerAnnuel) {
        this.nombreAutorisantCongerAnnuel = nombreAutorisantCongerAnnuel;
    }

    public Integer getNombreAutorisantCongerMaternite() {
        return nombreAutorisantCongerMaternite;
    }

    public void setNombreAutorisantCongerMaternite(Integer nombreAutorisantCongerMaternite) {
        this.nombreAutorisantCongerMaternite = nombreAutorisantCongerMaternite;
    }

    public Integer getNombreAutorisantCongerPartenite() {
        return nombreAutorisantCongerPartenite;
    }

    public void setNombreAutorisantCongerPartenite(Integer nombreAutorisantCongerPartenite) {
        this.nombreAutorisantCongerPartenite = nombreAutorisantCongerPartenite;
    }

    public Integer getNombreDemandeTraiteByMois() {
        return nombreDemandeTraiteByMois;
    }

    public void setNombreDemandeTraiteByMois(Integer nombreDemandeTraiteByMois) {
        this.nombreDemandeTraiteByMois = nombreDemandeTraiteByMois;
    }

    public Boolean getPaiePrime() {
        return paiePrime;
    }

    public void setPaiePrime(Boolean paiePrime) {
        this.paiePrime = paiePrime;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean getSalaire() {
        return salaire;
    }

    public void setSalaire(boolean salaire) {
        this.salaire = salaire;
    }

    public Boolean getSalaireBase() {
        return salaireBase;
    }

    public void setSalaireBase(Boolean salaireBase) {
        this.salaireBase = salaireBase;
    }

    public Boolean getSalaireBrute() {
        return salaireBrute;
    }

    public void setSalaireBrute(Boolean salaireBrute) {
        this.salaireBrute = salaireBrute;
    }

    public BigDecimal getSalairePlafonne() {
        return salairePlafonne;
    }

    public void setSalairePlafonne(BigDecimal salairePlafonne) {
        this.salairePlafonne = salairePlafonne;
    }

    public Boolean getSalarierHaveImpot() {
        return salarierHaveImpot;
    }

    public void setSalarierHaveImpot(Boolean salarierHaveImpot) {
        this.salarierHaveImpot = salarierHaveImpot;
    }

    public Boolean getSalarierIsNotHaveImpot() {
        return salarierIsNotHaveImpot;
    }

    public void setSalarierIsNotHaveImpot(Boolean salarierIsNotHaveImpot) {
        this.salarierIsNotHaveImpot = salarierIsNotHaveImpot;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSepAnnee() {
        return sepAnnee;
    }

    public void setSepAnnee(String sepAnnee) {
        this.sepAnnee = sepAnnee;
    }

    public String getSepCodeEtab() {
        return sepCodeEtab;
    }

    public void setSepCodeEtab(String sepCodeEtab) {
        this.sepCodeEtab = sepCodeEtab;
    }

    public String getSepCycle() {
        return sepCycle;
    }

    public void setSepCycle(String sepCycle) {
        this.sepCycle = sepCycle;
    }

    public String getSepFaculte() {
        return sepFaculte;
    }

    public void setSepFaculte(String sepFaculte) {
        this.sepFaculte = sepFaculte;
    }

    public String getSepFiliere() {
        return sepFiliere;
    }

    public void setSepFiliere(String sepFiliere) {
        this.sepFiliere = sepFiliere;
    }

    public String getSepNiveau() {
        return sepNiveau;
    }

    public void setSepNiveau(String sepNiveau) {
        this.sepNiveau = sepNiveau;
    }

    public String getSepNumero() {
        return sepNumero;
    }

    public void setSepNumero(String sepNumero) {
        this.sepNumero = sepNumero;
    }

    public String getSepPays() {
        return sepPays;
    }

    public void setSepPays(String sepPays) {
        this.sepPays = sepPays;
    }

    public String getSepSexe() {
        return sepSexe;
    }

    public void setSepSexe(String sepSexe) {
        this.sepSexe = sepSexe;
    }

    public BigDecimal getSeuilAudioVisuel() {
        return seuilAudioVisuel;
    }

    public void setSeuilAudioVisuel(BigDecimal seuilAudioVisuel) {
        this.seuilAudioVisuel = seuilAudioVisuel;
    }

    public BigDecimal getSeuilCreditFoncier() {
        return seuilCreditFoncier;
    }

    public void setSeuilCreditFoncier(BigDecimal seuilCreditFoncier) {
        this.seuilCreditFoncier = seuilCreditFoncier;
    }

    public BigDecimal getSeuilIrrp() {
        return seuilIrrp;
    }

    public void setSeuilIrrp(BigDecimal seuilIrrp) {
        this.seuilIrrp = seuilIrrp;
    }

    public BigDecimal getSeuilTaxCommunal() {
        return seuilTaxCommunal;
    }

    public void setSeuilTaxCommunal(BigDecimal seuilTaxCommunal) {
        this.seuilTaxCommunal = seuilTaxCommunal;
    }

    public int getSizeAnnee() {
        return sizeAnnee;
    }

    public void setSizeAnnee(int sizeAnnee) {
        this.sizeAnnee = sizeAnnee;
    }

    public int getSizeCodeEtab() {
        return sizeCodeEtab;
    }

    public void setSizeCodeEtab(int sizeCodeEtab) {
        this.sizeCodeEtab = sizeCodeEtab;
    }

    public int getSizeCycle() {
        return sizeCycle;
    }

    public void setSizeCycle(int sizeCycle) {
        this.sizeCycle = sizeCycle;
    }

    public int getSizeFaculte() {
        return sizeFaculte;
    }

    public void setSizeFaculte(int sizeFaculte) {
        this.sizeFaculte = sizeFaculte;
    }

    public int getSizeFiliere() {
        return sizeFiliere;
    }

    public void setSizeFiliere(int sizeFiliere) {
        this.sizeFiliere = sizeFiliere;
    }

    public int getSizeNiveau() {
        return sizeNiveau;
    }

    public void setSizeNiveau(int sizeNiveau) {
        this.sizeNiveau = sizeNiveau;
    }

    public int getSizeNumero() {
        return sizeNumero;
    }

    public void setSizeNumero(int sizeNumero) {
        this.sizeNumero = sizeNumero;
    }

    public int getSizePays() {
        return sizePays;
    }

    public void setSizePays(int sizePays) {
        this.sizePays = sizePays;
    }

    public int getSizeSexe() {
        return sizeSexe;
    }

    public void setSizeSexe(int sizeSexe) {
        this.sizeSexe = sizeSexe;
    }

    public Double getTauxAccidentTravail() {
        return tauxAccidentTravail;
    }

    public void setTauxAccidentTravail(Double tauxAccidentTravail) {
        this.tauxAccidentTravail = tauxAccidentTravail;
    }

    public Double getTauxAllocationFamilial() {
        return tauxAllocationFamilial;
    }

    public void setTauxAllocationFamilial(Double tauxAllocationFamilial) {
        this.tauxAllocationFamilial = tauxAllocationFamilial;
    }

    public Double getTauxAutresAncienete() {
        return tauxAutresAncienete;
    }

    public void setTauxAutresAncienete(Double tauxAutresAncienete) {
        this.tauxAutresAncienete = tauxAutresAncienete;
    }

    public Double getTauxCreditFrontier() {
        return tauxCreditFrontier;
    }

    public void setTauxCreditFrontier(Double tauxCreditFrontier) {
        this.tauxCreditFrontier = tauxCreditFrontier;
    }

    public Double getTauxEntreprise() {
        return tauxEntreprise;
    }

    public void setTauxEntreprise(Double tauxEntreprise) {
        this.tauxEntreprise = tauxEntreprise;
    }

    public Double getTauxFNE() {
        return tauxFNE;
    }

    public void setTauxFNE(Double tauxFNE) {
        this.tauxFNE = tauxFNE;
    }

    public Double getTauxPremierAncienete() {
        return tauxPremierAncienete;
    }

    public void setTauxPremierAncienete(Double tauxPremierAncienete) {
        this.tauxPremierAncienete = tauxPremierAncienete;
    }

    public Double getTauxSalarier() {
        return tauxSalarier;
    }

    public void setTauxSalarier(Double tauxSalarier) {
        this.tauxSalarier = tauxSalarier;
    }

    public int getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(int totalCredit) {
        this.totalCredit = totalCredit;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public Boolean getMulticaisse() {
        return multicaisse;
    }

    public void setMulticaisse(Boolean multicaisse) {
        this.multicaisse = multicaisse;
    }

    public String getImprime() {
        return imprime;
    }

    public void setImprime(String imprime) {
        this.imprime = imprime;
    }

    public Boolean getActifProduitNonStocker() {
        return actifProduitNonStocker;
    }

    public void setActifProduitNonStocker(Boolean actifProduitNonStocker) {
        this.actifProduitNonStocker = actifProduitNonStocker;
    }

    public Boolean getActifRemiseTicket() {
        return actifRemiseTicket;
    }

    public void setActifRemiseTicket(Boolean actifRemiseTicket) {
        this.actifRemiseTicket = actifRemiseTicket;
    }

    public String getCheminTicket() {
        return cheminTicket;
    }

    public void setCheminTicket(String cheminTicket) {
        this.cheminTicket = cheminTicket;
    }

    public Boolean getClientTicket() {
        return clientTicket;
    }

    public void setClientTicket(Boolean clientTicket) {
        this.clientTicket = clientTicket;
    }

    public Boolean getControleStock() {
        return controleStock;
    }

    public void setControleStock(Boolean controleStock) {
        this.controleStock = controleStock;
    }

    public String getDestinationAdresse() {
        return destinationAdresse;
    }

    public void setDestinationAdresse(String destinationAdresse) {
        this.destinationAdresse = destinationAdresse;
    }

    public Boolean getHistoriqueCaisse() {
        return historiqueCaisse;
    }

    public void setHistoriqueCaisse(Boolean historiqueCaisse) {
        this.historiqueCaisse = historiqueCaisse;
    }

    public BigDecimal getLimiteStocke() {
        return limiteStocke;
    }

    public void setLimiteStocke(BigDecimal limiteStocke) {
        this.limiteStocke = limiteStocke;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPortSTMP() {
        return portSTMP;
    }

    public void setPortSTMP(String portSTMP) {
        this.portSTMP = portSTMP;
    }

    public String getServeurSTMP() {
        return serveurSTMP;
    }

    public void setServeurSTMP(String serveurSTMP) {
        this.serveurSTMP = serveurSTMP;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBoutique() {
        return boutique;
    }

    public void setBoutique(String boutique) {
        this.boutique = boutique;
    }

    public Boolean getEnvoitSms() {
        return envoitSms;
    }

    public void setEnvoitSms(Boolean envoitSms) {
        this.envoitSms = envoitSms;
    }

    public Boolean getSms() {
        return sms;
    }

    public void setSms(Boolean sms) {
        this.sms = sms;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Boolean getPrixAchatInventaire() {
        return prixAchatInventaire;
    }

    public void setPrixAchatInventaire(Boolean prixAchatInventaire) {
        this.prixAchatInventaire = prixAchatInventaire;
    }

    public Boolean getPrixVenteInventaire() {
        return prixVenteInventaire;
    }

    public void setPrixVenteInventaire(Boolean prixVenteInventaire) {
        this.prixVenteInventaire = prixVenteInventaire;
    }

    public String getBackupData() {
        return backupData;
    }

    public void setBackupData(String backupData) {
        this.backupData = backupData;
    }

    public String getDbMysql() {
        return dbMysql;
    }

    public void setDbMysql(String dbMysql) {
        this.dbMysql = dbMysql;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getPasswordMysql() {
        return passwordMysql;
    }

    public void setPasswordMysql(String passwordMysql) {
        this.passwordMysql = passwordMysql;
    }

    public String getPathFileMysql() {
        return pathFileMysql;
    }

    public void setPathFileMysql(String pathFileMysql) {
        this.pathFileMysql = pathFileMysql;
    }

    public String getPortMysql() {
        return portMysql;
    }

    public void setPortMysql(String portMysql) {
        this.portMysql = portMysql;
    }

    public String getServerMysql() {
        return serverMysql;
    }

    public void setServerMysql(String serverMysql) {
        this.serverMysql = serverMysql;
    }

    public String getUserMysql() {
        return userMysql;
    }

    public void setUserMysql(String userMysql) {
        this.userMysql = userMysql;
    }

    public String getDestinationAdresse2() {
        return destinationAdresse2;
    }

    public void setDestinationAdresse2(String destinationAdresse2) {
        this.destinationAdresse2 = destinationAdresse2;
    }

    public String getDestinationAdresse3() {
        return destinationAdresse3;
    }

    public void setDestinationAdresse3(String destinationAdresse3) {
        this.destinationAdresse3 = destinationAdresse3;
    }

    public Boolean getImpressionDirect() {
        return impressionDirect;
    }

    public void setImpressionDirect(Boolean impressionDirect) {
        this.impressionDirect = impressionDirect;
    }

    public BigDecimal getLimiteStockeMagasin() {
        return limiteStockeMagasin;
    }

    public void setLimiteStockeMagasin(BigDecimal limiteStockeMagasin) {
        this.limiteStockeMagasin = limiteStockeMagasin;
    }

    public int getPortServeurDistance() {
        return portServeurDistance;
    }

    public void setPortServeurDistance(int portServeurDistance) {
        this.portServeurDistance = portServeurDistance;
    }

    public String getServeurDistance() {
        return serveurDistance;
    }

    public void setServeurDistance(String serveurDistance) {
        this.serveurDistance = serveurDistance;
    }

    public Boolean getUseCaisseByPointVente() {
        return useCaisseByPointVente;
    }

    public void setUseCaisseByPointVente(Boolean useCaisseByPointVente) {
        this.useCaisseByPointVente = useCaisseByPointVente;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (propertyId != null ? propertyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Property)) {
            return false;
        }
        Property other = (Property) object;
        if ((this.propertyId == null && other.propertyId != null) || (this.propertyId != null && !this.propertyId.equals(other.propertyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.repositories.Property[ propertyId=" + propertyId + " ]";
    }
    
}
