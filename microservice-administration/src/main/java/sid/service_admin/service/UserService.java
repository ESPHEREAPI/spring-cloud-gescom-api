/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.repository.RoleRepository;
//import sid.service_admin.repository.UserRepository;

import sid.service_admin.dto.LoginRequest;
import sid.service_admin.dto.MenuDto;
import sid.service_admin.dto.MenuUserDTO;
import sid.service_admin.dto.ModuleDTO;
import sid.service_admin.dto.ModuleUserDTO;
import sid.service_admin.dto.ProfilDTO;
import sid.service_admin.dto.RoleDTO;

import sid.service_admin.dto.UserCreateDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.dto.UserUpdateDTO;
import sid.service_admin.dto.ChangePasswordDTO;
import sid.service_admin.enums.ModuleLicence;
import sid.service_admin.enums.ModulesParTypeCommerce;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.mapper.MapperDtoImpl;
import sid.service_admin.model.Boutique;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.CompagnieParametres;
import sid.service_admin.model.Indicatifpays;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Profil;
import sid.service_admin.model.Roles;
import sid.service_admin.model.Usermenu;
import sid.service_admin.model.UsermenuPK;
import sid.service_admin.model.Usermodule;
import sid.service_admin.model.UsermodulePK;
import sid.service_admin.repository.IndicatifpaysRepository;
import sid.service_admin.repository.MenuRepository;

import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.PaysRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.ProfilRepository;
import sid.service_admin.repository.ReligionRepository;
import sid.service_admin.utils.Crypto;
import sid.service_admin.repository.ModulesecuriteRepository;
import sid.service_admin.repository.UsermenuRepository;
import sid.service_admin.repository.UsermoduleRepository;
import sid.service_admin.security.TenantContext;

/**
 *
 * @author USER01
 */
@Service
@Data
@AllArgsConstructor
@Slf4j
public class UserService implements Serializable {

//    @Autowired
//    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PaysRepository paysRepositorye;
    private ReligionRepository religionRepository;
    private PersonneRepository personneRepository;
    private ProfilRepository profilRepository;
    private ModulesecuriteRepository moduleRepository;
    private MenuRepository menuRepository;
    private IndicatifpaysRepository indicatifpaysRepository;
    private UsermoduleRepository usermoduleRepository;
    private UsermenuRepository usermenuRepository;
    private CompagnieRepository compagnieRepository;
    private sid.service_admin.repository.CompagnieParametresRepository compagnieParametresRepository;
    private LoginGeneratorService loginGeneratorService;
    private ProfilPermissionMatrixService profilPermissionMatrixService;
    private sid.service_admin.repository.ActionRepository actionRepository;
    private LicenceService licenceService;
    private TenantContext tenantContext;

    private ISecurite securiteService;

    private PasswordEncoder passwordEncoder;
    //@Autowired
    MapperDtoImpl mapToDTO;

    /**
     * SUPER_ADMIN/SYSTEM_ADMIN (sans compagnie) voient tous les comptes ; un
     * COMPANY_ADMIN ne voit que les utilisateurs de sa propre compagnie.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        Long compagnieId = tenantContext.currentCompagnieId();
        List<Personne> personnes = compagnieId != null
                ? personneRepository.findByCompagnie_Id(compagnieId)
                : personneRepository.findAll();
        return personnes.stream()
                .map(u -> this.mapToDTO.mapToDTO(u))
                .collect(Collectors.toList());
    }

    /** Changement de mot de passe par le titulaire du compte lui-meme (ancien mot de passe requis). */
    @Transactional
    public void changeOwnPassword(String username, ChangePasswordDTO dto) {
        Personne user = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (dto.getNouveauMotDePasse() == null || dto.getNouveauMotDePasse().isBlank()) {
            throw new BadRequestException("Le nouveau mot de passe est obligatoire");
        }
        if (dto.getAncienMotDePasse() == null || !passwordEncoder.matches(dto.getAncienMotDePasse(), user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        user.setMustChangePassword(Boolean.FALSE);
        personneRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        Long compagnieId = tenantContext.currentCompagnieId();
        Personne user = (compagnieId != null
                ? personneRepository.findByIdAndCompagnie_Id(id, compagnieId)
                : personneRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return this.mapToDTO.mapToDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO, String actorUsername) {

        // Rattachement compagnie : explicite (SUPER_ADMIN/SYSTEM_ADMIN ciblant une
        // compagnie precise) sinon herite de la compagnie de l'acteur (cas
        // COMPANY_ADMIN creant un employe). null = compte systeme, pas de quota.
        // Un COMPANY_ADMIN (tenantContext.currentCompagnieId() renseigne) ne peut
        // jamais cibler une AUTRE compagnie que la sienne, meme si le corps de la
        // requete en indique une - seul un compte sans compagnie (SUPER_ADMIN/
        // SYSTEM_ADMIN) peut choisir explicitement la compagnie ciblee.
        Long actorCompagnieId = tenantContext.currentCompagnieId();
        Long compagnieId = actorCompagnieId != null ? actorCompagnieId : userCreateDTO.getCompagnieId();
        if (compagnieId == null && actorUsername != null) {
            compagnieId = personneRepository.findByUserName(actorUsername)
                    .map(Personne::getCompagnie)
                    .map(Compagnie::getId)
                    .orElse(null);
        }
        Long finalCompagnieId = compagnieId;
        Compagnie compagnie = finalCompagnieId != null
                ? compagnieRepository.findById(finalCompagnieId)
                        .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + finalCompagnieId))
                : null;

        // Login : saisi manuellement s'il est fourni, sinon genere automatiquement
        // selon le format choisi par la compagnie (CompagnieParametres#loginFormat,
        // voir "Option Entreprise") - couvre tous les types de commerce geres par
        // la plateforme (voir LoginGeneratorService).
        String userName = userCreateDTO.getUserName();
        if (userName == null || userName.isBlank()) {
            sid.service_admin.enums.LoginFormat format = compagnieId != null
                    ? compagnieParametresRepository.findByCompagnie_Id(compagnieId)
                            .map(CompagnieParametres::getLoginFormat)
                            .orElse(sid.service_admin.enums.LoginFormat.INITIALE_NOM)
                    : sid.service_admin.enums.LoginFormat.INITIALE_NOM;
            userName = loginGeneratorService.genererLogin(format, compagnie != null ? compagnie.getTypeCommerce() : null,
                    userCreateDTO.getFirstName(), userCreateDTO.getLastname(), compagnieId);
            userCreateDTO.setUserName(userName);
        }

        Personne user = null;
        if (personneRepository.existsByEmail(userCreateDTO.getEmail()) == Boolean.FALSE && personneRepository.findByUserName(userName).isEmpty()) {

            if (compagnieId != null) {
                licenceService.verifierQuotaUtilisateurs(compagnieId);
            }

            user = new Personne();
            user = mapToDTO.mapToDTOUserCreate(userCreateDTO);

            user.setCreatedBy(actorUsername == null ? "Systeme" : actorUsername);
            user.setCreatedAt(new java.util.Date());

            // Role fixe, jamais choisi par le client : les droits reels viennent
            // exclusivement du Profil (voir ProfilPermissionMatrixService). Un
            // client ne doit jamais pouvoir s'auto-assigner (ou assigner a un
            // employe) un role de hierarchie comme COMPANY_ADMIN/SUPER_ADMIN.
            Roles roleEmploye = roleRepository.findByName(sid.service_admin.security.RoleNames.EMPLOYE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role " + sid.service_admin.security.RoleNames.EMPLOYE + " introuvable - le bootstrap de demarrage ne s'est pas execute correctement"));
            user.setRoleid(roleEmploye);

            if (userCreateDTO.getProfilid() != null) {
                Profil profil = profilRepository.findById(userCreateDTO.getProfilid())
                        .orElseThrow(() -> new ResourceNotFoundException("Profil non trouve : " + userCreateDTO.getProfilid()));
                user.setProfilid(profil);
                verifierBoutiqueRequisePourProfil(profil, userCreateDTO.getBoutique());
            }
            if (compagnie != null) {
                user.setCompagnie(compagnie);
            }

//            user.setBp(userCreateDTO.getAdresse().getBp());
//            user.setEmail(userCreateDTO.getAdresse().getEmail());
//            user.setIndicatifPays(userCreateDTO.getAdresse().getIndicatifPays());
//            user.setPhoneNumber(userCreateDTO.getAdresse().getTel());
            user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
            user.setBoutique(userCreateDTO.getBoutique());
            Personne savedUser = personneRepository.save(user);
//        roles.stream()
//                .forEach(rl -> userRoleRepository.save(new UserRole(savedUser, rl)));
            return mapToDTO.mapToDTO(savedUser);

        }
//        else{
//            throw new ConflictException("Email is already in use.." + userCreateDTO.getEmail());  
//        }
        UserDTO userDTO = new UserDTO();
        userDTO.setEcheck_connection(Boolean.FALSE);
        userDTO.setMessageEcheck("Email Or UserName  is already in use.." + userCreateDTO.getEmail());
        return userDTO;

    }

    // L'ecran Utilisateurs > Attribution des Modules/Menus edite les tables
    // historiques Usermodule/Usermenu (grant par utilisateur). Depuis que les
    // droits d'un utilisateur avec Profil sont calcules depuis la matrice
    // Profil x Menu x Action (voir SecuriteService.getModuleByUser /
    // getMenusbyModuleForUser, ProfilPermissionMatrixService), ces tables ne
    // sont plus la source de verite pour ces comptes : retirer un menu ici
    // cherche une ligne Usermenu qui n'a jamais existe (ResourceNotFoundException),
    // et en ajouter un n'a aucun effet sur les menus reellement affiches. On
    // bloque donc cet ecran pour un utilisateur avec Profil, avec un message
    // qui redirige vers le bon endroit, plutot que de planter silencieusement.
    private void verifierPasGereParProfil(Personne p) {
        if (p.getProfilid() != null) {
            throw new BadRequestException(
                    "Cet utilisateur a le profil " + p.getProfilid().getCode()
                    + " : ses droits se gerent depuis l'ecran \"Profil\" (Securite > Profil), pas ici.");
        }
    }

    // Un compte Caissier n'agit que sur sa boutique assignee (voir
    // BoutiqueAccessGuard.verifierBoutiqueUtilisateur cote microservice-produits) :
    // sans boutique, TOUTES ses operations de caisse/vente sont rejetees en 403,
    // silencieusement pour l'utilisateur qui l'a cree. On bloque donc ce cas des
    // la creation/modification plutot que de laisser decouvrir le probleme a
    // l'usage.
    private void verifierBoutiqueRequisePourProfil(Profil profil, Boutique boutique) {
        if (profil == null || profil.getCode() == null) {
            return;
        }
        if ("CAISSIER".equalsIgnoreCase(profil.getCode()) && boutique == null) {
            throw new BadRequestException("Un compte avec le profil Caissier doit être rattaché à une boutique (Point de Vente).");
        }
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {

        Personne user = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(userUpdateDTO.getFirstName());
        user.setLastname(userUpdateDTO.getLastname());
        user.setTel(userUpdateDTO.getTel());
        user.setCodePays(userUpdateDTO.getCodePays());
        user.setQuartier(userUpdateDTO.getQuartier());
        user.setEmail(userUpdateDTO.getEmail());
        user.setAutorisationDeletes(userUpdateDTO.getAutorisationDeletes());
        user.setBoutique(userUpdateDTO.getBoutique());

        user.setIsActive(userUpdateDTO.getIsActive());

        // Audit fields
        String currentUsername = userUpdateDTO.getCreatedBy();
        user.setLastModifiedBy(currentUsername == null ? "Systeme" : currentUsername);
        user.setLastModifiedDate(new java.util.Date());

        // Le role n'est plus modifiable via ce formulaire (voir UserService#createUser) -
        // seul le Profil determine les droits reels de l'utilisateur.
        if (userUpdateDTO.getProfilid() != null
                && (user.getProfilid() == null || user.getProfilid().getId().equals(userUpdateDTO.getProfilid()) == Boolean.FALSE)) {
            Profil profil = profilRepository.findById(userUpdateDTO.getProfilid())
                    .orElseThrow(() -> new ResourceNotFoundException("Profil not found with id: " + userUpdateDTO.getProfilid()));
            user.setProfilid(profil);
        }
        verifierBoutiqueRequisePourProfil(user.getProfilid(), user.getBoutique());

        Personne updatedUser = personneRepository.save(user);
        return mapToDTO.mapToDTO(updatedUser);
    }

    @Transactional
    public UserDTO updateUserDataAndPassWord(Long id, UserUpdateDTO userUpdateDTO) {
        String pwd = passwordEncoder.encode(userUpdateDTO.getPassword());

        Personne user = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFirstName(userUpdateDTO.getFirstName());
        user.setLastname(userUpdateDTO.getLastname());
        user.setTel(userUpdateDTO.getTel());
        user.setCodePays(userUpdateDTO.getCodePays());
        user.setQuartier(userUpdateDTO.getQuartier());
        user.setEmail(userUpdateDTO.getEmail());
        user.setAutorisationDeletes(userUpdateDTO.getAutorisationDeletes());

        user.setIsActive(userUpdateDTO.getIsActive());

        // Audit fields
        String currentUsername = userUpdateDTO.getCreatedBy();
        user.setLastModifiedBy(currentUsername == null ? "Systeme" : currentUsername);
        user.setLastModifiedDate(new java.util.Date());
        user.setPassword(pwd);

        // Le role n'est plus modifiable via ce formulaire (voir UserService#createUser) -
        // seul le Profil determine les droits reels de l'utilisateur.
        if (userUpdateDTO.getProfilid() != null
                && (user.getProfilid() == null || user.getProfilid().getId().equals(userUpdateDTO.getProfilid()) == Boolean.FALSE)) {
            Profil profil = profilRepository.findById(userUpdateDTO.getProfilid())
                    .orElseThrow(() -> new ResourceNotFoundException("Profil not found with id: " + userUpdateDTO.getProfilid()));
            user.setProfilid(profil);
        }
        verifierBoutiqueRequisePourProfil(user.getProfilid(), user.getBoutique());

        Personne updatedUser = personneRepository.save(user);
        return mapToDTO.mapToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        Personne user = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        personneRepository.delete(user);
    }

    @Transactional
    public void deleteUser(String userName) {
        Personne user = personneRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userName));
        personneRepository.delete(user);
    }
    
     @Transactional
    public Boolean userExist(String userName) {
        Personne user = personneRepository.findByUserName(userName).orElseThrow(null);
           return     user!=null ;
    }

    public UserDTO authetification(LoginRequest loginRequest) {
        UserDTO userDTO = null;
        Personne user = null;
        // Login insensible a la casse : "Jdupont" et "jdupont" doivent aboutir au
        // meme compte. Le mot de passe, lui, reste verifie tel quel plus bas.
        //
        // Liste, pas Optional : userName n'a aucune contrainte d'unicite en
        // base (verification-puis-creation non atomique dans createUser) - si
        // un doublon existe, on ne devine pas lequel choisir, on essaie le
        // mot de passe fourni contre chaque candidat et on authentifie celui
        // qui correspond. Empeche un doublon de donnees de rendre TOUT le
        // monde partageant ce login incapable de se connecter (500 brut).
        List<Personne> candidats = personneRepository.findAllByUserNameIgnoreCase(loginRequest.getUserName());

        if (candidats.isEmpty()) {
            userDTO = new UserDTO();
            userDTO.setEcheck_connection(Boolean.TRUE);
            userDTO.setMessageEcheck("User not Exist : " + loginRequest.getUserName());
            // throw new ResourceNotFoundException("User not Exist : " + loginRequest.getUserName());
            return userDTO;

        }
        if (candidats.size() > 1) {
            log.warn("Plusieurs comptes ({}) partagent le login '{}' - contrainte d'unicite manquante en base, a nettoyer.",
                    candidats.size(), loginRequest.getUserName());
        }

        boolean passwordMatches = false;
        for (Personne candidat : candidats) {
            String storedHash = candidat.getPassword();
            boolean matches;
            boolean migrateToBcrypt = false;
            if (storedHash != null && storedHash.startsWith("$2")) {
                // hash BCrypt (deja migre)
                matches = passwordEncoder.matches(loginRequest.getPassWord(), storedHash);
            } else {
                // ancien hash SHA-256 non sale : verifie avec l'ancien algorithme, puis
                // migre transparement vers BCrypt si le mot de passe est correct
                matches = storedHash != null && storedHash.equals(Crypto.sha256(loginRequest.getPassWord()));
                migrateToBcrypt = matches;
            }
            if (matches) {
                user = candidat;
                passwordMatches = true;
                if (migrateToBcrypt) {
                    user.setPassword(passwordEncoder.encode(loginRequest.getPassWord()));
                    personneRepository.save(user);
                }
                break;
            }
        }

        if (!passwordMatches) {
            userDTO = new UserDTO();
            userDTO.setEcheck_connection(Boolean.TRUE);
            userDTO.setMessageEcheck("User or PassWord Not Correct...");
            // throw new ResourceNotFoundException("User not Exist : " + loginRequest.getUserName());
            return userDTO;
            //  throw new ResourceNotFoundException("User or PassWord Not Correct...");
        } else if (user.getIsActive() == Boolean.FALSE) {
            userDTO = new UserDTO();
            userDTO.setEcheck_connection(Boolean.TRUE);
            // Compte cree par inscription autonome (voir CompagnieService.inscriptionAutonome),
            // pas encore desactive par un administrateur - message distinct pour
            // ne pas orienter l'utilisateur vers un administrateur qui n'y peut rien.
            userDTO.setMessageEcheck("EN_ATTENTE_VERIFICATION".equals(user.getStatut())
                    ? "Veuillez verifier votre adresse email avant de vous connecter (voir le lien recu par email)."
                    : "The User is Not Active please Contact your Administrator..." + loginRequest.getUserName());
            // throw new ResourceNotFoundException("User not Exist : " + loginRequest.getUserName());
            return userDTO;
            // throw new ResourceNotFoundException("The User is Not Active please Contact your Administrator..." + loginRequest.getUserName());

        }
        //Role role=user.getRole();
        //try {
        //userRoles = userRoleRepository.listeRolesByUser(user.getId());
        // } catch (Exception e) {
        // System.out.println("message erreur:"+e.getLocalizedMessage()+" "+e.getMessage());
        //}

        //userRoles.forEach(System.out::println);
        //user.setRoles(userRoles);
        userDTO = this.mapToDTO.mapToDTO(user);
        return userDTO;

    }

    public List<RoleDTO> allRoles() {
        return roleRepository.findAll().stream()
                .map(role -> this.mapToDTO.mapRoleToDTO(role))
                .collect(Collectors.toList());
    }

    /**
     * Un profil est une politique de droits propre a une compagnie : un
     * COMPANY_ADMIN ne voit et n'assigne jamais les profils d'une autre
     * compagnie. SUPER_ADMIN/SYSTEM_ADMIN (sans compagnie) voient tous les
     * profils, a titre de supervision uniquement (voir ProfilPermissionMatrixService
     * pour le blocage de modification).
     */
    @Transactional(readOnly = true)
    public List<ProfilDTO> allProfil() {
        Long compagnieId = tenantContext.currentCompagnieId();
        List<Profil> profils = compagnieId != null
                ? profilRepository.findByCompagnie_Id(compagnieId)
                : profilRepository.findAll();
        return profils.stream()
                .map(profil -> this.mapToDTO.mapToDTOProfil(profil))
                .collect(Collectors.toList());
    }

    /**
     * Cree un nouveau profil (categorie de droits Menu x Action, voir
     * ProfilPermissionMatrixService) - permet a un COMPANY_ADMIN de creer
     * ses propres variantes (ex: "CAISSIER_CODEBARRE" distinct de
     * "CAISSIER_COMPTOIR") plutot que de se limiter aux profils par defaut.
     * Rattache automatiquement le profil a la compagnie de l'acteur - un
     * compte sans compagnie (SUPER_ADMIN/SYSTEM_ADMIN) ne peut pas en creer,
     * il n'a pas de compagnie a laquelle le rattacher.
     */
    @Transactional
    public ProfilDTO createProfil(String code, String description) {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Seul un administrateur de compagnie peut creer un profil");
        }
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Le code du profil est obligatoire");
        }
        if (profilRepository.findByCodeAndCompagnie_Id(code, compagnieId) != null) {
            throw new ConflictException("Un profil avec le code '" + code + "' existe deja pour votre compagnie");
        }
        Profil profil = new Profil(code);
        profil.setDescription(description);
        profil.setStatut("ACTIF");
        profil.setAddDate(new java.util.Date());
        profil.setCompagnie(compagnieRepository.findById(compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + compagnieId)));
        Profil saved = profilRepository.save(profil);
        return this.mapToDTO.mapToDTOProfil(saved);
    }

    /**
     * Seme les profils metier par defaut pour une compagnie qui n'en a encore
     * aucun, pour qu'un COMPANY_ADMIN puisse immediatement configurer sa
     * matrice de droits (Menu x Action) sans partir de zero. Sans effet si la
     * compagnie a deja au moins un profil (creation manuelle ou deja semee).
     * Appele a la creation d'une compagnie (CompagnieService) et en
     * rattrapage au demarrage pour les compagnies existantes (SuperAdminBootstrap).
     */
    // Menus par defaut par profil (codes reels seedes par InitiationDb - voir
    // ProfilPermissionMatrixService#seedPermissionsParDefaut). Sans ceci, un
    // profil fraichement cree n'a AUCUN menu visible tant qu'un administrateur
    // n'a pas configure manuellement l'ecran Permission - decouvert en
    // production avec un compte Caissier dont la sidebar etait entierement
    // vide (seul le Dashboard, qui echappe a ce controle, restait visible).
    private static final java.util.Map<String, java.util.List<String>> MENUS_PAR_DEFAUT_PAR_PROFIL = java.util.Map.of(
            "ADMIN", java.util.List.of(
                    // vente
                    "Vente Articles", "Vente Art./CodeBare", "Historique Caisse", "Controle Caisse", "Mode Paiement", "Bon D Achat",
                    // facturation
                    "Client", "Devise Client", "Facture", "Rapport", "Versement",
                    // stock
                    "Produits", "Transfert Stock", "Dashboard Transfert", "Fournisseur", "Verouillage Stock",
                    "Pointe de Vente", "Commande Fournisseur", "Inventaire", "Mise a jour du Stock", "Destockage", "Static Stock", "Code Bare",
                    // comptabilite
                    "Type Resource", "Ressource", "Historique vente", "Controle Vente", "Marge Caisse",
                    "Compte Client", "Charge", "Marge", "Type depense", "Element Ressource/Depense",
                    // administration
                    "Configuration", "Option Entreprise", "Recond. session anterieur", "Initialisation Stock",
                    // parametrage
                    "Annee", "Employeur", "Entreprise", "Boutique", "Categorie Produit", "Specifique Produit",
                    "Ville", "Magasin", "Service", "Type client", "Zone Vente",
                    // securite
                    "Module Securite", "Utilisateurs", "Roles", "Profil",
                    // photocophie
                    "Photocopie/Saisir", "Historique Photocopie"),
            "CAISSIER", java.util.List.of(
                    "Vente Articles", "Vente Art./CodeBare", "Historique Caisse", "Controle Caisse", "Mode Paiement", "Bon D Achat"),
            "COMMERCIAL", java.util.List.of(
                    "Client", "Devise Client", "Commande Fournisseur", "Produits", "Fournisseur"),
            "COMPTABLE", java.util.List.of(
                    "Client", "Facture", "Rapport", "Versement",
                    "Type Resource", "Ressource", "Historique vente", "Controle Vente", "Marge Caisse",
                    "Compte Client", "Charge", "Marge", "Type depense", "Element Ressource/Depense"),
            "USER", java.util.List.of());

    // Actions par defaut a semer pour un profil fraichement cree - resolues
    // depuis le catalogue Action (table), pas un enum Java fige. PRINT est
    // desormais inclus : le catalogue Action a remplace l'ancien ENUM MySQL
    // natif qui empechait son insertion (voir ActionService, Permission.java).
    private static final java.util.Set<String> CODES_ACTIONS_PAR_DEFAUT =
            java.util.Set.of("READ", "WRITE", "UPDATE", "DELETE", "PRINT");

    private java.util.List<sid.service_admin.model.Action> actionsParDefaut() {
        return actionRepository.findAll().stream()
                .filter(a -> CODES_ACTIONS_PAR_DEFAUT.contains(a.getCode()))
                .toList();
    }

    @Transactional
    public void seedProfilsParDefautPourCompagnie(Compagnie compagnie) {
        if (compagnie == null || compagnie.getId() == null
                || profilRepository.countByCompagnie_Id(compagnie.getId()) > 0) {
            return;
        }
        Stream.of(
                new String[]{"ADMIN", "Acces complet aux fonctionnalites de la compagnie"},
                new String[]{"CAISSIER", "Vente au comptoir, encaissement"},
                new String[]{"COMMERCIAL", "Devis, commandes, suivi clientele"},
                new String[]{"COMPTABLE", "Facturation, comptabilite, tresorerie"},
                new String[]{"USER", "Acces de base, sans droit particulier"})
                .forEach(codeEtDescription -> {
                    Profil profil = new Profil(codeEtDescription[0]);
                    profil.setDescription(codeEtDescription[1]);
                    profil.setStatut("ACTIF");
                    profil.setAddDate(new java.util.Date());
                    profil.setCompagnie(compagnie);
                    Profil saved = profilRepository.save(profil);

                    java.util.List<String> menusParDefaut = MENUS_PAR_DEFAUT_PAR_PROFIL.getOrDefault(
                            codeEtDescription[0], java.util.List.of());
                    if (!menusParDefaut.isEmpty()) {
                        profilPermissionMatrixService.seedPermissionsParDefaut(saved, menusParDefaut, actionsParDefaut());
                    }
                });
    }

    /**
     * Rattrapage pour les profils DEJA existants (compagnies creees avant ce
     * fix) qui n'ont encore aucune permission configuree - c'est exactement
     * le trou qui laissait un utilisateur Caissier avec une sidebar
     * entierement vide. N'ecrase jamais une configuration deja faite
     * manuellement (via l'ecran Permission) : ne seme que les profils dont
     * la matrice est totalement vide. Appele au demarrage, voir
     * SuperAdminBootstrap.
     */
    @Transactional
    public void seedPermissionsManquantesPourProfilsExistants() {
        profilRepository.findAll().forEach(profil -> {
            if (profilPermissionMatrixService.aDejaDesPermissions(profil)) {
                return;
            }
            java.util.List<String> menusParDefaut = MENUS_PAR_DEFAUT_PAR_PROFIL.getOrDefault(
                    profil.getCode(), java.util.List.of());
            if (!menusParDefaut.isEmpty()) {
                profilPermissionMatrixService.seedPermissionsParDefaut(profil, menusParDefaut, actionsParDefaut());
            }
        });
    }

    // Actions metier (distinctes des READ/WRITE/UPDATE/DELETE/PRINT generiques)
    // utilisees pour brancher reellement Valider/Annuler/Imprimer une Facture
    // et Valider une vente Code Barre, cote microservice-produits (voir
    // JwtAuthFilter/EffectivePermissionService la-bas). Toujours semees et
    // accordees au demarrage (idempotent, jamais gate sur "profil deja
    // configure" comme seedPermissionsManquantesPourProfilsExistants) car
    // sans ce backfill, toutes les compagnies existantes perdraient d'un
    // coup l'acces a ces boutons deja utilises en production.
    private static final java.util.Map<String, String> LIBELLES_ACTIONS_METIER = java.util.Map.of(
            "VALIDER", "Valider",
            "ANNULER", "Annuler",
            "IMPRIMER", "Imprimer");

    @Transactional
    public void seedActionsMetierEtDroitsParDefaut() {
        LIBELLES_ACTIONS_METIER.forEach((code, libelle) -> {
            if (actionRepository.findByCode(code).isEmpty()) {
                actionRepository.save(new sid.service_admin.model.Action(code, libelle));
            }
        });

        java.util.List<sid.service_admin.model.Action> actionsFacture = actionRepository.findAll().stream()
                .filter(a -> java.util.Set.of("VALIDER", "ANNULER", "IMPRIMER").contains(a.getCode()))
                .toList();
        java.util.List<sid.service_admin.model.Action> actionsVente = actionRepository.findAll().stream()
                .filter(a -> "VALIDER".equals(a.getCode()))
                .toList();

        profilRepository.findAll().forEach(profil -> {
            if (java.util.Set.of("ADMIN", "COMPTABLE").contains(profil.getCode())) {
                profilPermissionMatrixService.seedPermissionsParDefaut(profil, java.util.List.of("Facture"), actionsFacture);
            }
            if (java.util.Set.of("ADMIN", "CAISSIER").contains(profil.getCode())) {
                profilPermissionMatrixService.seedPermissionsParDefaut(profil, java.util.List.of("Vente Art./CodeBare"), actionsVente);
            }
        });
    }

    /**
     * Nouveau menu "Initialisation Stock" (format personnalise + restauration
     * de stock par boutique, voir StockRestaurationController cote
     * microservice-produits) : accorde de facon additive READ/WRITE/UPDATE/
     * DELETE + VALIDER (l'action qui protege l'application reelle d'une
     * restauration) aux profils ADMIN deja existants - meme raison que
     * seedActionsMetierEtDroitsParDefaut, sans ce backfill aucun ADMIN d'une
     * compagnie deja creee ne verrait ce menu.
     */
    @Transactional
    public void seedInitialisationStockDroitsParDefaut() {
        java.util.List<sid.service_admin.model.Action> actions = new java.util.ArrayList<>(actionsParDefaut());
        actionRepository.findByCode("VALIDER").ifPresent(actions::add);

        profilRepository.findAll().forEach(profil -> {
            if ("ADMIN".equals(profil.getCode())) {
                profilPermissionMatrixService.seedPermissionsParDefaut(profil, java.util.List.of("Initialisation Stock"), actions);
            }
        });

        // Un COMPANY_ADMIN n'a jamais de profil assigne (voir
        // AdminAccountService.createCompanyAdmin) : son sidebar est gere par
        // l'ancien mecanisme Usermodule/Usermenu par utilisateur, pas par la
        // matrice Profil ci-dessus. provisionerModulesEtMenusPourTypeCommerce
        // est deja concu pour ce rattrapage ("resynchroniser un compte
        // existant apres l'ajout de nouveaux menus au catalogue") mais n'est
        // normalement declenche qu'a la main - on l'appelle ici pour tous les
        // COMPANY_ADMIN existants, sinon aucun ne verrait "Initialisation Stock".
        personneRepository.findByRoleid_Name(sid.service_admin.security.RoleNames.COMPANY_ADMIN).stream()
                .filter(p -> p.getCompagnie() != null)
                .forEach(p -> securiteService.provisionerModulesEtMenusPourTypeCommerce(p, p.getCompagnie().getTypeCommerce()));
    }

    /**
     * Catalogue de modules pour l'ecran de gestion des utilisateurs. Filtre par le
     * type de commerce de la compagnie de l'acteur (meme logique que le formulaire
     * de licence, voir ModulesParTypeCommerce) - non filtre pour un acteur sans
     * compagnie (SUPER_ADMIN/SYSTEM_ADMIN).
     */
    public List<ModuleDTO> allModule(String actorUsername) {
        List<Modulesecurite> modules = moduleRepository.findAll();
        Compagnie compagnie = personneRepository.findByUserName(actorUsername)
                .map(Personne::getCompagnie)
                .orElse(null);
        if (compagnie != null) {
            java.util.Set<String> codesAutorises = ModulesParTypeCommerce.pour(compagnie.getTypeCommerce()).stream()
                    .map(ModuleLicence::getCode)
                    .collect(Collectors.toSet());
            modules = modules.stream().filter(m -> codesAutorises.contains(m.getCode())).collect(Collectors.toList());
        }
        return modules.stream()
                .map(md -> this.mapToDTO.mapToDTOModulel(md))
                .collect(Collectors.toList());
    }

    public List<MenuDto> allMenu() {
        return menuRepository.findAll().stream()
                .map(menu -> this.mapToDTO.mapToDTOMenu(menu))
                .collect(Collectors.toList());
    }

    public List<Indicatifpays> listeIndicatif() {
        return indicatifpaysRepository.findAll();
    }

    public List<MenuDto> listeMenuByModule(Modulesecurite mod) {
        return securiteService.getMenusByModule(mod).stream()
                .map(menu -> this.mapToDTO.mapToDTOMenu(menu))
                .collect(Collectors.toList());

    }

    public List<ModuleDTO> listeModuleByUser(Personne p) {
        return securiteService.getModuleByUser(p).stream()
                .map(md -> this.mapToDTO.mapToDTOModulel(md))
                .collect(Collectors.toList());
    }

    public List<ModuleDTO> listeModuleByUser(Long personneid) {
        Personne p = personneRepository.findById(personneid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + personneid));
        return securiteService.getModuleByUser(p).stream()
                .map(md -> this.mapToDTO.mapToDTOModulel(md))
                .collect(Collectors.toList());
    }

    public UserDTO findUserByUserName(Long userid) {
        Personne user = personneRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userid));
        return mapToDTO.mapToDTO(user);

    }

    public void updateModuleByUser(ModuleUserDTO moduleUserDTO) {
        //on trie sur tous les module qu il a eut avant la modifoication
        Long userid = moduleUserDTO.getUserid();
        Personne p = personneRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userid));
        verifierPasGereParProfil(p);
        List<Modulesecurite> moduleUsers = (List<Modulesecurite>) securiteService.getModuleByUser(p);
        List<Modulesecurite> modules = moduleUserDTO.getModules().stream()
                .map(mod -> mapToDTO.mapToDTOModulelDTO(mod))
                .collect(Collectors.toList());
        String CurrentUsername = moduleUserDTO.getCreatBy();

        if (modules.containsAll(moduleUsers) == Boolean.TRUE) {//on verifit si le module contient tous les modules lier a l utilisateur avant la mise a jour 

            //retire les liste deja lier a l utilisateur 
            modules.removeAll(moduleUsers);
            modules.stream()
                    .map(md -> saveModuleuser(md, p, CurrentUsername))
                    .collect(Collectors.toList());

        } else {
            //on recupere les modules a retirer

            List<Modulesecurite> moduleRemove = moduleUsers.stream()
                    .filter(mod -> !modules.contains(mod))
                    .collect(Collectors.toList());
            //on retire les modules

            moduleRemove.forEach(mod -> removeModuleByUser(mod, p));

            //on ajoutte les nouveau module
            List<Modulesecurite> moduleAdd = modules.stream()
                    .filter(mod -> !moduleUsers.contains(mod))
                    .collect(Collectors.toList());
            moduleAdd.stream()
                    .map(mod -> saveModuleuser(mod, p, CurrentUsername))
                    .collect(Collectors.toList());
        }
    }

    public void updateModuleMenuByUser(MenuUserDTO menuUserDTO) {
        //on trie sur tous les module qu il a eut avant la modifoication
        Long userid = menuUserDTO.getUserid();
        Personne p = personneRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userid));
        verifierPasGereParProfil(p);
        Modulesecurite modul = moduleRepository.findById(menuUserDTO.getModuleid())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + menuUserDTO.getModuleid()));
        List<Menu> menusUsers = (List<Menu>) securiteService.getMenusbyModuleForUser(p, modul);
        List<Menu> menus = menuUserDTO.getMenus().stream()
                .map(menu -> mapToDTO.mapToDTOMenu(menu))
                .collect(Collectors.toList());
        String CurrentUsername = menuUserDTO.getCreatBy();

        if (!menusUsers.isEmpty() && menus.containsAll(menusUsers) == Boolean.TRUE) {//on verifit si le menusUsers contient tous les menusUsers lier a l utilisateur avant la mise a jour 

            //retire les liste deja lier a l utilisateur 
            menus.removeAll(menusUsers);
            menus.stream()
                    .map(md -> saveMenuser(md, p, CurrentUsername))
                    .collect(Collectors.toList());

        } else {
            //on recupere les menus a retirer

            List<Menu> menuRemove = menusUsers.stream()
                    .filter(me -> !menus.contains(me))
                    .collect(Collectors.toList());
            //on retire les modules

            menuRemove.forEach(menu -> removeMenuByUser(menu, p));

            //on ajoutte les nouveau module
            List<Menu> menuleAdd = menus.stream()
                    .filter(menu -> !menusUsers.contains(menu))
                    .collect(Collectors.toList());
            menuleAdd.stream()
                    .map(mod -> saveMenuser(mod, p, CurrentUsername))
                    .collect(Collectors.toList());
        }
    }
    /**
     * Menus visibles pour le sidebar. Un utilisateur avec un Profil (voir
     * ProfilPermissionMatrixService) herite des menus de sa matrice Profil x
     * Menu x Action ; sans profil (ex: admin compagnie provisionne directement
     * via Usermenu a la creation), on garde l'ancien mecanisme par utilisateur.
     */
    public MenuUserDTO chargeMenuUser(String userName){
        Personne p=personneRepository.findByUserName(userName).get();
        List<MenuDto> listMenuUsers;
        if (p.getProfilid() != null) {
            listMenuUsers = profilPermissionMatrixService.getMenusVisibles(p.getProfilid().getId()).stream()
                    .map(mapToDTO::mapToDTOMenu)
                    .collect(Collectors.toList());
        } else {
            listMenuUsers = usermenuRepository.findByPersonneAndAutorisationTrue(p).stream()
                    .map(um-> mapToDTO.mapToDTOMenu(um.getMenu()))
                    .collect(Collectors.toList());
        }
        MenuUserDTO menuUserDTO=new MenuUserDTO();
        menuUserDTO.setMenus(listMenuUsers);
        menuUserDTO.setUserid(p.getId());
        return menuUserDTO;
    }

    public Usermodule saveModuleuser(Modulesecurite mod, Personne p, String CurrentUsername) {
        Usermodule um = new Usermodule(new UsermodulePK(mod.getId(), p.getId()));
        um.setModulesecurite(mod);
        um.setPersonne(p);
        um.setUserAdd(CurrentUsername);
        um.setAddDate(new Date());
        usermoduleRepository.save(um);
        return um;
    }

    public void removeModuleByUser(Modulesecurite mod, Personne p) {
        Usermodule um = usermoduleRepository.findById(new UsermodulePK(mod.getId(), p.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Module  not found with user: " + p.getUserName()));

        usermoduleRepository.delete(um);
    }

    public Usermenu saveMenuser(Menu menu, Personne p, String CurrentUsername) {
        Usermenu m = new Usermenu(new UsermenuPK(menu.getId(), p.getId()));
        m.setMenu(menu);
        m.setPersonne(p);
        m.setUserAdd(CurrentUsername);
        m.setAddDate(new Date());
        m.setAutorisation(Boolean.TRUE);
        usermenuRepository.save(m);
        return m;
    }

    public void removeMenuByUser(Menu men, Personne p) {
        Usermenu um = usermenuRepository.findById(new UsermenuPK(men.getId(), p.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Module  not found with user: " + men.getId()));

        usermenuRepository.delete(um);
    }

    public List<Modulesecurite> findModlueByModuleDTO(List<ModuleDTO> moduleDtos) {
        return moduleDtos.stream()
                .map(md -> this.mapToDTO.mapToDTOModulelDTO(md))
                .collect(Collectors.toList());
    }

    public Modulesecurite moduleById(Long modulueid) {
        Modulesecurite mod = moduleRepository.findById(modulueid)
                .orElseThrow(() -> new ResourceNotFoundException("Module  not found  id : " + modulueid));
        return mod;
    }

    public List<MenuDto> listMenuByModuleByUser(Long userid, Long modulueid) {
        Modulesecurite mod = moduleRepository.findById(modulueid)
                .orElseThrow(() -> new ResourceNotFoundException("Module  not found  id : " + modulueid));
        Personne p = personneRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userid));
        return securiteService.getMenusbyModuleForUser(p, mod).stream()
                .map(m -> mapToDTO.mapToDTOMenu(m))
                .collect(Collectors.toList());

    }
}
