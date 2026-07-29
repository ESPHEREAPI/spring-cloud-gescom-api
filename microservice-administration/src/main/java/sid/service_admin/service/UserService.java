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
import lombok.AllArgsConstructor;
import lombok.Data;
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
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.mapper.MapperDtoImpl;
import sid.service_admin.model.Boutique;
import sid.service_admin.model.Compagnie;
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

/**
 *
 * @author USER01
 */
@Service
@Data
@AllArgsConstructor
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
    private LicenceService licenceService;

    private ISecurite securiteService;

    private PasswordEncoder passwordEncoder;
    //@Autowired
    MapperDtoImpl mapToDTO;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return personneRepository.findAll().stream()
                .map(u -> this.mapToDTO.mapToDTO(u))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        Personne user = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return this.mapToDTO.mapToDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO, String actorUsername) {

        Personne user = null;
        if (personneRepository.existsByEmail(userCreateDTO.getEmail()) == Boolean.FALSE && personneRepository.findByUserName(userCreateDTO.getUserName()).isEmpty()) {

            // Rattachement compagnie : explicite (SUPER_ADMIN/SYSTEM_ADMIN ciblant une
            // compagnie precise) sinon herite de la compagnie de l'acteur (cas
            // COMPANY_ADMIN creant un employe). null = compte systeme, pas de quota.
            Long compagnieId = userCreateDTO.getCompagnieId();
            if (compagnieId == null && actorUsername != null) {
                compagnieId = personneRepository.findByUserName(actorUsername)
                        .map(Personne::getCompagnie)
                        .map(Compagnie::getId)
                        .orElse(null);
            }
            if (compagnieId != null) {
                licenceService.verifierQuotaUtilisateurs(compagnieId);
            }

            user = new Personne();
            user = mapToDTO.mapToDTOUserCreate(userCreateDTO);

            user.setCreatedBy(actorUsername == null ? "Systeme" : actorUsername);
            user.setCreatedAt(new java.util.Date());
            Optional<Roles> role = roleRepository.findById(userCreateDTO.getRoleid());
            if (role.isPresent()) {
                user.setRoleid(role.get());
            }
            Optional<Profil> profil = profilRepository.findById(userCreateDTO.getProfilid());
            if (role.isPresent()) {
                user.setProfilid(profil.get());

            }
            if (compagnieId != null) {
                Long finalCompagnieId = compagnieId;
                user.setCompagnie(compagnieRepository.findById(finalCompagnieId)
                        .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + finalCompagnieId)));
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

        // mise a jour du role et profile
        if (user.getRoleid() == null || user.getRoleid().getId().equals(userUpdateDTO.getRoleid()) == Boolean.FALSE) {
            Roles role = roleRepository.findById(userUpdateDTO.getRoleid())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userUpdateDTO.getRoleid()));
            user.setRoleid(role);
        }
        if (user.getProfilid() == null || user.getProfilid().getId().equals(userUpdateDTO.getProfilid()) == Boolean.FALSE) {
            Profil profil = profilRepository.findById(userUpdateDTO.getProfilid())
                    .orElseThrow(() -> new ResourceNotFoundException("Profil not found with id: " + userUpdateDTO.getRoleid()));
            user.setProfilid(profil);
        }

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

        // mise a jour du role et profile (profil optionnel pour les comptes de la hierarchie admin)
        if (userUpdateDTO.getRoleid() != null
                && (user.getRoleid() == null || user.getRoleid().getId().equals(userUpdateDTO.getRoleid()) == Boolean.FALSE)) {
            Roles role = roleRepository.findById(userUpdateDTO.getRoleid())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userUpdateDTO.getRoleid()));
            user.setRoleid(role);
        }
        if (userUpdateDTO.getProfilid() != null
                && (user.getProfilid() == null || user.getProfilid().getId().equals(userUpdateDTO.getProfilid()) == Boolean.FALSE)) {
            Profil profil = profilRepository.findById(userUpdateDTO.getProfilid())
                    .orElseThrow(() -> new ResourceNotFoundException("Profil not found with id: " + userUpdateDTO.getProfilid()));
            user.setProfilid(profil);
        }

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
        Optional<Personne> userEntite = personneRepository.findByUserName(loginRequest.getUserName());

        if (userEntite.isPresent() == Boolean.FALSE) {
            userDTO = new UserDTO();
            userDTO.setEcheck_connection(Boolean.TRUE);
            userDTO.setMessageEcheck("User not Exist : " + loginRequest.getUserName());
            // throw new ResourceNotFoundException("User not Exist : " + loginRequest.getUserName());
            return userDTO;

        }
        user = userEntite.get();
        boolean passwordMatches;
        String storedHash = user.getPassword();
        if (storedHash != null && storedHash.startsWith("$2")) {
            // hash BCrypt (deja migre)
            passwordMatches = passwordEncoder.matches(loginRequest.getPassWord(), storedHash);
        } else {
            // ancien hash SHA-256 non sale : verifie avec l'ancien algorithme, puis
            // migre transparement vers BCrypt si le mot de passe est correct
            passwordMatches = storedHash != null && storedHash.equals(Crypto.sha256(loginRequest.getPassWord()));
            if (passwordMatches) {
                user.setPassword(passwordEncoder.encode(loginRequest.getPassWord()));
                personneRepository.save(user);
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
            userDTO.setMessageEcheck("The User is Not Active please Contact your Administrator..." + loginRequest.getUserName());
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

    public List<ProfilDTO> allProfil() {
        return profilRepository.findAll().stream()
                .map(profil -> this.mapToDTO.mapToDTOProfil(profil))
                .collect(Collectors.toList());
    }

    public List<ModuleDTO> allModule() {
        return moduleRepository.findAll().stream()
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
    public MenuUserDTO chargeMenuUser(String userName){
        //Long userid=moduleUserDTOs.getUserid();
        Personne p=personneRepository.findByUserName(userName).get();
        List<MenuDto> listMenuUsers=usermenuRepository.findByPersonneAndAutorisationTrue(p).stream()
                .map(um-> mapToDTO.mapToDTOMenu(um.getMenu()))
                .collect(Collectors.toList());
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
