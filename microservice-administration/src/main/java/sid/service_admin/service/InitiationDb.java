/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.repository.PermissionRepository;
import sid.service_admin.repository.RolePermissionsRepositorie;
import sid.service_admin.repository.RoleRepository;
//import sid.service_admin.repository.UserRepository;
import sid.service_admin.dto.UserCreateDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.enums.OperationType;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Indicatifpays;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Pays;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Profil;
import sid.service_admin.model.Religion;
//import sid.service_admin.model.Roles;
import sid.service_admin.model.RolePermissions;
import sid.service_admin.model.Roles;
import sid.service_admin.model.Titre;
import sid.service_admin.repository.IndicatifpaysRepository;
import sid.service_admin.repository.MenuRepository;
import sid.service_admin.repository.ModulesecuriteRepository;
import sid.service_admin.repository.PaysRepository;
import sid.service_admin.repository.ProfilRepository;
import sid.service_admin.repository.ReligionRepository;
import sid.service_admin.repository.TitreRepository;
import java.util.*;

/**
 *
 * @author USER01
 */
@Transactional
@Data
@Service
//@AllArgsConstructor

public class InitiationDb implements InitDB {

    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
//    private UserRepository userRepository;
    private UserService UserService;
    private IndicatifpaysRepository indicatifpaysRepository;

    private RolePermissionsRepositorie rolePermissionsRepositorie;
    private Roles roles;
    private PaysRepository paysRepository;
    private ReligionRepository religionRepository;
    private TitreRepository titreRepository;
    ProfilRepository profilRepository;
    private ModulesecuriteRepository modulesecuriteRepository;
    private MenuRepository menuRepository;
//    public InitiationDb(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository, UserService UserService,
//           RolePermissionsRepositorie rolePermissionsRepositorie) {
//        this.roleRepository = roleRepository;
//        this.permissionRepository = permissionRepository;
//        this.userRepository = userRepository;
//        this.UserService = UserService;
//     
//        this.rolePermissionsRepositorie = rolePermissionsRepositorie;
//
//    }

    public InitiationDb(RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserService userService,
            IndicatifpaysRepository indicatifpaysRepository,
            RolePermissionsRepositorie rolePermissionsRepositorie,
            PaysRepository paysRepository,
            ReligionRepository religionRepository,
            TitreRepository titreRepository,
            ProfilRepository profilRepository, ModulesecuriteRepository modulesecuriteRepository, MenuRepository menuRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.UserService = userService;
        this.indicatifpaysRepository = indicatifpaysRepository;
        this.rolePermissionsRepositorie = rolePermissionsRepositorie;
        this.paysRepository = paysRepository;
        this.religionRepository = religionRepository;
        this.titreRepository = titreRepository;
        this.profilRepository = profilRepository;
        this.modulesecuriteRepository = modulesecuriteRepository;
        this.menuRepository = menuRepository;
    }

    @Override

    public List<Roles> createRoles() {

        Stream.of("ADMIN", "CAISSIER", "COMMERCIAL", "ASSISTANT COMMERCIAL ", "COMPTABLE", "USER")
                .forEach(r -> {
                    if (roleRepository.findByName(r) == null) {
                        roleRepository.save(new Roles(r));
                    }
                });
        return roleRepository.findAll();
    }

    @Override
    public List<Permission> createPermission() {
        // D'abord créer/vérifier l'existence de toutes les permissions
        Stream.of(OperationType.WRITE, OperationType.READ,
                OperationType.UPDATE, OperationType.DELETE)
                .forEach(per -> {
                    System.out.println("sortie..." + permissionRepository.findByName(per.name()));
                    if (permissionRepository.findByName(per.name()) == null) {
                        permissionRepository.save(new Permission(per));
                    }
                });

        // Ensuite récupérer la liste complète
        return permissionRepository.findAll();
    }

    @Override
    public void addIndicatifPays() {
        List<Indicatifpays> colIndicatif = new ArrayList<>();
        colIndicatif = indicatifpaysRepository.findAll();
        if (colIndicatif.isEmpty()) {
            colIndicatif.add(new Indicatifpays("237"));

            indicatifpaysRepository.saveAll(colIndicatif);
        }

    }

    @Override
    public Collection<Religion> getAllReligions() {
        Collection<Religion> listreligion = new ArrayList<>();
        listreligion = religionRepository.findAll();
        if (listreligion == null || listreligion.isEmpty()) {
            listreligion.add(new Religion("C", "Catholique", new Date()));
            listreligion.add(new Religion("I", "Islam", new Date()));
            listreligion.add(new Religion("P", "Protestant", new Date()));
            religionRepository.saveAll(listreligion);
        }
        return listreligion;
    }

    @Override
    public Collection<Titre> getAllTitres() {
        List<Titre> listetitres = new ArrayList<>();
        listetitres = titreRepository.findAll();
        if (listetitres == null || listetitres.isEmpty()) {
            listetitres.add(new Titre("Dr", "Docteur"));
            listetitres.add(new Titre("Ing", "Ingénieur"));
            listetitres.add(new Titre("M.", "Messieur"));
            listetitres.add(new Titre("Mlle", "Mademoiselle"));
            listetitres.add(new Titre("Mme", "Madame"));
            listetitres.add(new Titre("Mr", "Monsieur"));
            listetitres.add(new Titre("Ms", "Maître"));
            listetitres.add(new Titre("Pf", "Professeur"));
            listetitres.add(new Titre("Ps", "Pasteur"));
            titreRepository.saveAll(listetitres);
        }
        return listetitres;

    }

    @Override
    public void addMenuToAdmin() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<Pays> getAllPays() {
        String Continent = "Afrique";
        // on teste s'il n'y a pas encore de pays
        List<Pays> listpays = new ArrayList<>();
        listpays = paysRepository.findAll();
        if (listpays == null || listpays.isEmpty()) {

            listpays.add(new Pays("CM", "CAMEROUN", "CAMEROON", Continent));
            listpays.add(new Pays("GA", "GABON", "GABON", Continent));
            listpays.add(new Pays("ZA", "AFRIQUE DU SUD", "SOUTH AFRICA", Continent));
            listpays.add(new Pays("AO", "ANGOLA", "ANGOLA", Continent));
            listpays.add(new Pays("BJ", "BENIN", "BENIN", Continent));
            listpays.add(new Pays("BW", "BOTSWANA", "BOTSWANA", Continent));
            listpays.add(new Pays("BF", "BURKINA FASO", "BURKINA FASO", Continent));
            listpays.add(new Pays("CV", "CAP-VERT", "CAPE VERDE", Continent));
            listpays.add(new Pays("CF", "CENTRAFRIQUE", "CENTRAFRICA", Continent));
            listpays.add(new Pays("CG", "CONGO", "CONGO", Continent));
            listpays.add(new Pays("CD", "CONGO RD", "CONGO RD", Continent));
            listpays.add(new Pays("CI", "COTE D'IVOIRE", "COTE D'IVOIRE", Continent));
            listpays.add(new Pays("EG", "EGYPTE", "EGYPT", Continent));
            listpays.add(new Pays("ET", "ETHIOPIE", "ETHIOPIA", Continent));
            listpays.add(new Pays("GM", "GAMBIE", "GAMBIA", Continent));
            listpays.add(new Pays("GH", "GHANA", "GHANA", Continent));
            listpays.add(new Pays("GN", "GUINEE", "GUINEA", Continent));
            listpays.add(new Pays("GW", "GUINEE-BISSAU", "GUINEA-BISSAU", Continent));
            listpays.add(new Pays("GQ", "GUINEE EQUATORIALE", "EQUATORIAL GUINEA", Continent));
            listpays.add(new Pays("KE", "KENYA", "KENYA", Continent));
            listpays.add(new Pays("LR", "LIBERIA", "LIBERIA", Continent));
            listpays.add(new Pays("MG", "MADAGASCAR", "MADAGASCAR", Continent));
            listpays.add(new Pays("ML", "MALI", "MALI", Continent));
            listpays.add(new Pays("NA", "NAMIBIE", "NAMIBIA", Continent));
            listpays.add(new Pays("NG", "NIGERIA", "NIGERIA", Continent));
            listpays.add(new Pays("NE", "NIGER", "NIGER", Continent));
            listpays.add(new Pays("SN", "SENEGAL", "SENEGAL", Continent));
            listpays.add(new Pays("TD", "TCHAD", "CHAD", Continent));
            listpays.add(new Pays("TG", "TOGO", "Gabon", Continent));
            listpays.add(new Pays("TN", "TUNISIE", "TUNISIA", Continent));
            listpays.add(new Pays("ZM", "ZAMBIE", "ZAMBIA", Continent));
            listpays.add(new Pays("ZW", "ZIMBABWE", "ZIMBABWE", Continent));
            paysRepository.saveAll(listpays);
        }
        return listpays;

    }

    @Override
    public Collection<Modulesecurite> getAllModuleSecurite() {
        Collection<Modulesecurite> modsec = new ArrayList<>();

        modsec.add(new Modulesecurite("facturation", "Facturation clients"));
        modsec.add(new Modulesecurite("securite", "gestion Securite"));
        modsec.add(new Modulesecurite("stock", "gestion des stocks"));
        modsec.add(new Modulesecurite("comptabilite", "gestion de la comptabilite"));

        modsec.add(new Modulesecurite("vente", "Gestion des ventes"));
        modsec.add(new Modulesecurite("administration", "administration de l entreprise"));
        // modsec.add(new Modulesecurite("securite", "securite  de l entreprise"));
        modsec.add(new Modulesecurite("parametrage", "parametrage de  l entreprise"));
        modsec.add(new Modulesecurite("photocophie", "gestion photocopie "));
//            modsec.add(new Modulesecurite(ModuleMenu.mdsNotification, "gestion des sms et mail"));
//            modsec.add(new Modulesecurite(ModuleMenu.mdsMtn, "maintenance"));
//            modsec.add(new Modulesecurite(ModuleMenu.mdsEtats, "Reporting"));

//        this.modulesecuriteRepository.saveAll(modsec);
        return modsec.stream()
                .filter(md -> this.modulesecuriteRepository.findByCode(md.getCode()) == null)
                .map(mds -> this.modulesecuriteRepository.save(mds)).toList();

    }

    @Override
    public Collection<Menu> getMenuByModuleFacturation() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("facturation");
        Map<String, Object> parMap = new HashMap<>();
        parMap.put("moduleid", modsec5.getId());
        Collection<Menu> menuone = new ArrayList<>();
        //menuone = menudao.findAllEntitiesByUsingQueryName(Menu.FIND_BY_MODULEID, parMap);
        if (modsec5 != null) {

            menuone.add(new Menu("Client", "gestion client", modsec5));
            menuone.add(new Menu("Devise Client", "Gestion devis client", modsec5));
            menuone.add(new Menu("Facture", "Facture client", modsec5));
            menuone.add(new Menu("Rapport", "Rapport Facturation", modsec5));
            menuone.add(new Menu("Versement", "Versement client", modsec5));
//            menuone.add(new Menu("salaire", "gestion de la paie", false, "/paiement/gestionPaie", modsec5));
//            menuone.add(new Menu("caisse", "gestion de la caisse", false, "/paiement/gestioncaisse", modsec5));
//            menuone.add(new Menu(ModuleMenu.mPaGestionAnnciennette, "ANCIENNETE", false, "/paiement/gestionAnciennette", modsec5));
//            menuone.add(new Menu(ModuleMenu.mPOperationDivert, "Operqtion Diverts", false, "/paiement/operationDiverts", modsec5));
//            menuone.add(new Menu(ModuleMenu.Impot_Salarier, "impot Salarier", false, "/paiement/gestionImpot", modsec5));

        }
        upsertMenus(menuone);
        return menuone;

    }
    
     @Override
    public Collection<Menu> getMenuByModuleStock() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("stock");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Produits", "Gestion des produits", modsec5));
            menuone.add(new Menu("Transfert Stock", "Transfert de stock entre boutiques", modsec5));
            menuone.add(new Menu("Dashboard Transfert", "tableau de bord pour le transfert de stock", modsec5));
            menuone.add(new Menu("Fournisseur", "Gestion des fournisseurs", modsec5));
            menuone.add(new Menu("Verouillage Stock", "Verouillage du stock", modsec5));
            menuone.add(new Menu("Pointe de Vente", "Gestion des points de vente", modsec5));
            menuone.add(new Menu("Commande Fournisseur", "Gestion des commandes fournisseur", modsec5));
            menuone.add(new Menu("Inventaire", "Gestion de l'inventaire", modsec5));
            menuone.add(new Menu("Mise a jour du Stock", "Mise a jour du stock", modsec5));
            menuone.add(new Menu("Destockage", "Gestion du destockage", modsec5));
            menuone.add(new Menu("Static Stock", "Gestion des static de stock", modsec5));
            menuone.add(new Menu("Code Bare", "Gestion des codes barre", modsec5));

        }
        upsertMenus(menuone);
        return menuone;

    }

    @Override
    public Collection<Menu> getMenuByModulePhotocopie() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("photocophie");
        Map<String, Object> parMap = new HashMap<>();
       // parMap.put("moduleid", modsec5.getId());
        Collection<Menu> menuone = new ArrayList<>();
        //menuone = menudao.findAllEntitiesByUsingQueryName(Menu.FIND_BY_MODULEID, parMap);
        if (modsec5 != null) {

            menuone.add(new Menu("Photocopie/Saisir", "Gestion saisir des photocopies", modsec5));
             menuone.add(new Menu("Historique Photocopie", "historique  des photocopies", modsec5));
            //menuone.add(new Menu("Static Stock", "Gestion des static de stock", modsec5));
            //menuone.add(new Menu("Facture", "Facture client", modsec5));
           // menuone.add(new Menu("Rapport", "Rapport Facturation", modsec5));
            //menuone.add(new Menu("Versement", "Versement client", modsec5));
//            menuone.add(new Menu("salaire", "gestion de la paie", false, "/paiement/gestionPaie", modsec5));
//            menuone.add(new Menu("caisse", "gestion de la caisse", false, "/paiement/gestioncaisse", modsec5));
//            menuone.add(new Menu(ModuleMenu.mPaGestionAnnciennette, "ANCIENNETE", false, "/paiement/gestionAnciennette", modsec5));
//            menuone.add(new Menu(ModuleMenu.mPOperationDivert, "Operqtion Diverts", false, "/paiement/operationDiverts", modsec5));
//            menuone.add(new Menu(ModuleMenu.Impot_Salarier, "impot Salarier", false, "/paiement/gestionImpot", modsec5));

        }
        upsertMenus(menuone);
        return menuone;

    }

    @Override
    public Collection<Menu> getMenuByModuleVente() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("vente");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Vente Articles", "Vente d'articles", modsec5));
            menuone.add(new Menu("Vente Art./CodeBare", "Vente d'articles par code barre", modsec5));
            menuone.add(new Menu("Historique Caisse", "Historique de caisse", modsec5));
            menuone.add(new Menu("Controle Caisse", "Controle de caisse", modsec5));
            menuone.add(new Menu("Mode Paiement", "Gestion des modes de paiement", modsec5));
            menuone.add(new Menu("Bon D Achat", "Gestion des bons d'achat", modsec5));

        }
        upsertMenus(menuone);
        return menuone;
    }

    @Override
    public Collection<Menu> getMenuByModuleSecurite() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("securite");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Module Securite", "Gestion des modules de securite", modsec5));
            menuone.add(new Menu("Utilisateurs", "Gestion des utilisateurs", modsec5));
            menuone.add(new Menu("Roles", "Gestion des roles", modsec5));
            menuone.add(new Menu("Profil", "Gestion des profils", modsec5));

        }
        upsertMenus(menuone);
        return menuone;
    }

    @Override
    public Collection<Menu> getMenuByModuleAdministration() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("administration");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Configuration", "Configuration generale", modsec5));
            menuone.add(new Menu("Option Entreprise", "Options de l'entreprise", modsec5));
            menuone.add(new Menu("Recond. session anterieur", "Reconduction de la session anterieure", modsec5));

        }
        upsertMenus(menuone);
        return menuone;
    }

    @Override
    public Collection<Menu> getMenuByModuleComptabilite() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("comptabilite");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Type Resource", "Gestion des types de ressource", modsec5));
            menuone.add(new Menu("Ressource", "Gestion des ressources", modsec5));
            menuone.add(new Menu("Historique vente", "Historique des ventes", modsec5));
            menuone.add(new Menu("Controle Vente", "Controle des ventes", modsec5));
            menuone.add(new Menu("Marge  Caisse", "Marge de caisse", modsec5));
            menuone.add(new Menu("Compte Client", "Gestion des comptes client", modsec5));
            menuone.add(new Menu("Charge", "Gestion des charges", modsec5));
            menuone.add(new Menu("Marge", "Gestion de la marge", modsec5));
            menuone.add(new Menu("Type depense", "Gestion des types de depense", modsec5));
            menuone.add(new Menu("Element Ressource/Depense", "Gestion des elements de ressource/depense", modsec5));

        }
        upsertMenus(menuone);
        return menuone;
    }

    @Override
    public Collection<Menu> getMenuByModuleParametrage() {
        Modulesecurite modsec5 = this.modulesecuriteRepository.findByCode("parametrage");
        Collection<Menu> menuone = new ArrayList<>();
        if (modsec5 != null) {

            menuone.add(new Menu("Annee", "Gestion des annees", modsec5));
            menuone.add(new Menu("Employeur", "Gestion des employeurs", modsec5));
            menuone.add(new Menu("Entreprise", "Gestion de l'entreprise", modsec5));
            menuone.add(new Menu("Boutique", "Gestion des boutiques", modsec5));
            menuone.add(new Menu("Categorie Produit", "Gestion des categories de produit", modsec5));
            menuone.add(new Menu("Specifique Produit", "Gestion des specifiques produit", modsec5));
            menuone.add(new Menu("Ville", "Gestion des villes", modsec5));
            menuone.add(new Menu("Magasin", "Gestion des magasins", modsec5));
            menuone.add(new Menu("Service", "Gestion des services", modsec5));
            menuone.add(new Menu("Type client", "Gestion des types de client", modsec5));
            menuone.add(new Menu("Zone Vente", "Gestion des zones de vente", modsec5));

        }
        upsertMenus(menuone);
        return menuone;
    }

    /**
     * Insere chaque menu s'il n'existe pas encore (recherche par code, insensible
     * a la casse cote MySQL) ; si un menu du meme code existe deja mais rattache
     * a un autre module (coquille historique, ex. l'ancien seed errone de
     * getMenuByModuleVente), le rattache et corrige sa casse plutot que de le
     * laisser orphelin - sinon la creation du bon menu serait silencieusement
     * ignoree par idempotence.
     */
    private void upsertMenus(Collection<Menu> menus) {
        for (Menu menu : menus) {
            Menu existant = this.menuRepository.findByCode(menu.getCode());
            if (existant == null) {
                menu.setEtat(false);
                this.menuRepository.save(menu);
            } else if (existant.getModuleid() == null
                    || !existant.getModuleid().getId().equals(menu.getModuleid().getId())) {
                existant.setCode(menu.getCode());
                existant.setModuleid(menu.getModuleid());
                this.menuRepository.save(existant);
            }
        }
    }

}
