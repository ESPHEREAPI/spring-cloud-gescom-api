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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.ModuleDTO;
import sid.service_admin.enums.ModuleLicence;
import sid.service_admin.enums.ModulesParTypeCommerce;
import sid.service_admin.enums.TypeCommerce;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.mapper.MapperDtoImpl;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Usermenu;
import sid.service_admin.model.UsermenuPK;
import sid.service_admin.model.Usermodule;
import sid.service_admin.model.UsermodulePK;
import sid.service_admin.repository.MenuRepository;
import sid.service_admin.repository.ModulesecuriteRepository;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.UsermenuRepository;
import sid.service_admin.repository.UsermoduleRepository;
import sid.service_admin.utils.Crypto;

/**
 *
 * @author USER01
 */
@Service
@Transactional
public class SecuriteService implements ISecurite {

    @Autowired
    private PersonneRepository personneRepository;

    @Autowired
    private ModulesecuriteRepository modulesecuriteRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UsermoduleRepository usermoduleRepository;

    @Autowired
    private UsermenuRepository usermenuRepository;
    @Autowired
    MapperDtoImpl mapper;
    @Autowired
    private ProfilPermissionMatrixService profilPermissionMatrixService;

    /**
     * Vérification si l'utilisateur qui est en train de se connecter existe
     */
    @Override
    public Personne getAuthentification(Personne u) {
        String pwd = Crypto.sha256(u.getPassword());
        return personneRepository.findByUserNameAndassword(u.getUserName(), pwd);
    }

    @Override
    public void resetPassword(Personne p, String newPassword) {
        p.setPassword(newPassword);
        personneRepository.save(p);
    }

    /**
     * Retourne tous les paramètres d'un module
     */
    @Override
    public Modulesecurite getModule(Modulesecurite modulesecurite) {
        return modulesecuriteRepository.findById(modulesecurite.getId()).orElse(null);
    }

    /**
     * Récupération des modules d'un user. Un utilisateur avec un Profil (voir
     * ProfilPermissionMatrixService) voit les modules deduits de sa matrice
     * Profil x Menu x Action ; sans profil (ex: admin compagnie provisionne
     * directement via Usermodule a la creation), on garde l'ancien mecanisme
     * par utilisateur pour ne pas casser les comptes existants.
     */
    @Override
    public Collection<Modulesecurite> getModuleByUser(Personne u) {
        if (u.getProfilid() != null) {
            return profilPermissionMatrixService.getMenusVisibles(u.getProfilid().getId()).stream()
                    .map(Menu::getModuleid)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return modulesecuriteRepository.listModulesByUserId(u.getId());
    }

    /**
     * Récupération de tous les modules
     */
    @Override
    public Collection<Modulesecurite> getAllModules() {
        return modulesecuriteRepository.findAll();
    }

    /**
     * Récupération des modules qui ne sont pas attribués à un user
     *
     * @param u
     * @return
     */
    @Override
    public List<ModuleDTO> getModuleForNotUser(Long userid) {
        Personne p = personneRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userid));
        java.util.stream.Stream<sid.service_admin.model.Modulesecurite> modules =
                modulesecuriteRepository.findModulesNotAssignedToUser(p.getId()).stream();
        if (p.getCompagnie() != null) {
            java.util.Set<String> codesAutorises = sid.service_admin.enums.ModulesParTypeCommerce
                    .pour(p.getCompagnie().getTypeCommerce()).stream()
                    .map(sid.service_admin.enums.ModuleLicence::getCode)
                    .collect(Collectors.toSet());
            modules = modules.filter(m -> codesAutorises.contains(m.getCode()));
        }
        return modules.map(mod -> mapper.mapToDTOModulel(mod))
                .collect(Collectors.toList());

    }

    /**
     * Récupération des menus d'un module
     *
     * @param mod
     * @return
     */
    @Override
    public Collection<Menu> getMenusByModule(Modulesecurite mod) {
        return menuRepository.findByModuleid(mod);
    }

    /**
     * Récupération des menus d'un user pour un module précis
     *
     * @param u
     * @param m
     * @return
     */
    @Override
    public Collection<Menu> getMenusbyModuleForUser(Personne u, Modulesecurite m) {
        if (u == null || m == null) {
            return new ArrayList<>();
        }
        if (u.getProfilid() != null) {
            return profilPermissionMatrixService.getMenusVisibles(u.getProfilid().getId()).stream()
                    .filter(menu -> menu.getModuleid() != null && menu.getModuleid().getId().equals(m.getId()))
                    .collect(Collectors.toList());
        }
        return menuRepository.findMenusByModuleAndUserWithAuthorization(m.getId(), u.getId());
    }

    /**
     * Récupération des menus non attribués à un user pour un module précis
     *
     * @param u
     * @param m
     */
    @Override
    public Collection<Menu> getMenusForNotModuleUser(Personne u, Modulesecurite m) {
        // Récupération de tous les menus du module
        List<Menu> allModuleMenus = menuRepository.findByModuleidId(m.getId());

        // Récupération des menus autorisés pour l'utilisateur
        List<Menu> authorizedMenus = menuRepository.findMenusByModuleAndUserWithAuthorization(m.getId(), u.getId());

        // Récupération des menus non autorisés pour l'utilisateur
        List<Menu> unauthorizedMenus = menuRepository.findMenusByModuleAndUserWithoutAuthorization(m.getId(), u.getId());

        // Création de la liste résultante
        Set<Menu> resultMenus = new HashSet<>(allModuleMenus);
        resultMenus.removeAll(authorizedMenus);
        resultMenus.addAll(unauthorizedMenus);

        return new ArrayList<>(resultMenus);
    }

    @Override
    public void addModuleToUser(Personne u, Modulesecurite m) {
        Usermodule usermodule = new Usermodule();
        usermodule.setAddDate(new Date());
        usermodule.setUserAdd(u.getUserName());
        usermodule.setUsermodulePK(new UsermodulePK(m.getId(), u.getId()));
        usermoduleRepository.save(usermodule);
    }

    @Override
    public void addModulesToUser(Personne u, List<Modulesecurite> mod) {
        // Récupération des modules de l'user
        Collection<Modulesecurite> moduleUser = this.getModuleByUser(u);

        // Test si l'user a déjà des modules
        if (moduleUser.isEmpty()) {
            // Parcours de la liste et insertion
            for (Modulesecurite m : mod) {
                this.addModuleToUser(u, m);
            }
        } else {
            for (Modulesecurite m : mod) {
                if (!moduleUser.contains(m)) {
                    this.addModuleToUser(u, m);
                }
            }
        }
    }

    @Override
    public void removeModulesToUser(Personne u, List<Modulesecurite> mod) {
        // Vérification si les modules sont présents
        if (mod != null && !mod.isEmpty()) {
            for (Modulesecurite m : mod) {
                // Recherche dans la table pour pouvoir supprimer
                UsermodulePK pk = new UsermodulePK(m.getId(), u.getId());
                Optional<Usermodule> modSecu = usermoduleRepository.findById(pk);
                if (modSecu.isPresent()) {
                    usermoduleRepository.delete(modSecu.get());
                }
            }
        }
    }

    @Override
    public void addMenutoUser(Personne u, Menu m) {
        // UsermenuPK/Usermenu attendent (menuid, userid) dans cet ordre - ne pas inverser.
        UsermenuPK pk = new UsermenuPK(m.getId(), u.getId());
        Optional<Usermenu> existingUsermenu = usermenuRepository.findById(pk);

        if (existingUsermenu.isPresent()) {
            Usermenu usermenu = existingUsermenu.get();
            usermenu.setAutorisation(Boolean.TRUE);
            usermenuRepository.save(usermenu);
        } else {
            Usermenu usermenu = new Usermenu(m.getId(), u.getId());
            usermenu.setAutorisation(Boolean.TRUE);
            usermenuRepository.save(usermenu);
        }
    }

    @Override
    public void removeMenutoUser(Personne u, Menu m) {
        UsermenuPK pk = new UsermenuPK(m.getId(), u.getId());
        Optional<Usermenu> existingUsermenu = usermenuRepository.findById(pk);

        if (existingUsermenu.isPresent()) {
            Usermenu usermenu = existingUsermenu.get();
            usermenu.setAutorisation(Boolean.FALSE);
            usermenuRepository.save(usermenu);
        } else {
            Usermenu usermenu = new Usermenu(m.getId(), u.getId());
            usermenu.setAutorisation(Boolean.FALSE);
            usermenuRepository.save(usermenu);
        }
    }

    /**
     * (Re)accorde a un utilisateur tous les modules/menus applicables au type de
     * commerce donne (voir ModulesParTypeCommerce) - idempotent, n'ajoute que ce
     * qui manque. Utilise a la creation d'un admin compagnie, et pour
     * resynchroniser un compte existant apres l'ajout de nouveaux menus au
     * catalogue (voir InitiationDb).
     */
    @Override
    public void provisionerModulesEtMenusPourTypeCommerce(Personne personne, TypeCommerce typeCommerce) {
        List<Modulesecurite> modules = ModulesParTypeCommerce.pour(typeCommerce).stream()
                .map(m -> modulesecuriteRepository.findByCode(m.getCode()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        addModulesToUser(personne, modules);

        for (Modulesecurite module : modules) {
            for (Menu menu : menuRepository.findByModuleid(module)) {
                addMenutoUser(personne, menu);
            }
        }
    }

    @Override
    public Collection<Menu> getMenusForUser(Personne u) {
        if (u == null) {
            return new ArrayList<>();
        }

        // Récupération de tous les menus de l'utilisateur
        List<Menu> allUserMenus = menuRepository.findMenusByUserId(u.getId());

        // Récupération des menus autorisés
        List<Menu> authorizedMenus = menuRepository.findMenusByUserIdWithAuthorization(u.getId());

        // Récupération des menus non autorisés
        List<Menu> unauthorizedMenus = menuRepository.findMenusByUserIdWithoutAuthorization(u.getId());

        // Construction de la liste finale
        Set<Menu> resultMenus = new HashSet<>(allUserMenus);
        resultMenus.addAll(authorizedMenus);
        resultMenus.removeAll(unauthorizedMenus);

        return new ArrayList<>(resultMenus);
    }

    @Override
    public void insertUserInLogger(Personne u) {
        // Implémentation du logger de connexion si nécessaire
        // ConnectionLogger connexionUser = new ConnectionLogger();
        // connexionUser.setUtilisateur(u.getMatricule());
        // connexionUser.setDateConnexion(new Date());
        // connectionLoggerRepository.save(connexionUser);
    }

    @Override
    public boolean checkModuleUserExiste(String username, String codemodule) {
        try {
            Personne user = personneRepository.findByUserName(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

            Usermodule usermodule = usermoduleRepository.checkModuleUserExist(user.getId(), codemodule);

            // Retourne true si usermodule existe (n'est pas null), false sinon
            return usermodule != null;

        } catch (ResourceNotFoundException e) {
            // Si l'utilisateur n'existe pas, retourne false
            return false;
        }

    }

    @Override
    public boolean checkMenuUserExiste(String username, String codemodule, String codemenu) {

        try {
            Personne user = personneRepository.findByUserName(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

            Usermenu usermenu = usermenuRepository.checkModuleUserExist(user.getId(), codemodule,codemenu);

            // Retourne true si usemenu existe (n'est pas null), false sinon
            return usermenu != null;

        } catch (ResourceNotFoundException e) {
            // Si l'utilisateur n'existe pas, retourne false
            return false;
        }
    }
}
