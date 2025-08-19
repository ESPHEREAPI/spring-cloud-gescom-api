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
import lombok.AllArgsConstructor;
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
import sid.service_admin.model.Pays;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Profil;
import sid.service_admin.model.Religion;
//import sid.service_admin.model.Roles;
import sid.service_admin.model.RolePermissions;
import sid.service_admin.model.Roles;
import sid.service_admin.model.Titre;
import sid.service_admin.repository.IndicatifpaysRepository;
import sid.service_admin.repository.PaysRepository;
import sid.service_admin.repository.ProfilRepository;
import sid.service_admin.repository.ReligionRepository;
import sid.service_admin.repository.TitreRepository;

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
                        ProfilRepository profilRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.UserService = userService;
        this.indicatifpaysRepository = indicatifpaysRepository;
        this.rolePermissionsRepositorie = rolePermissionsRepositorie;
        this.paysRepository = paysRepository;
        this.religionRepository = religionRepository;
        this.titreRepository = titreRepository;
        this.profilRepository = profilRepository;
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

//    @Override
//    public UserDTO createAdmin() {
//        Optional<Role> role_admin = roleRepository.findByName("ADMIN");
//        List<Permission> allPermission = new ArrayList<>();
////        if (role_admin.isPresent() == Boolean.FALSE) {
//// ajoute des permissions
//        Stream.of(OperationType.WRITE, OperationType.READ,
//                OperationType.UPDATE, OperationType.DELETE)
//                .forEach(per -> {
////                    System.out.println("sortie..." + permissionRepository.findByName(per.name()));
//                    if (permissionRepository.findByName(per.name()).isPresent() == Boolean.FALSE) {
//                        permissionRepository.save(new Permission(per));
//                    }
//                });
//        Roles role = new Roles("ADMIN");
//        if (roleRepository.findByName(role.getName()).isPresent() == Boolean.FALSE) {
//            role = roleRepository.save(role);
//        } else {
//            String nameRole = role.getName();
//            role = roleRepository.findByName(role.getName()).orElseThrow(() -> new ResourceNotFoundException("Roles Not Exist Create Roles " + nameRole));
//        }
//        this.roles = role;
//
//        Set<Permission> listePermission = new HashSet<>(permissionRepository.findAll());
//        List<RolePermissions> listeRolePermission = listePermission.stream()
//                .map(per -> this.checkRolePermission(this.roles, per)).collect(Collectors.toList());
//
////        }
//        UserCreateDTO userCreateDTO = new UserCreateDTO();
//        userCreateDTO.setUserName("admin");
//        userCreateDTO.setEmail("frankjiatou@gmail.com");
//        userCreateDTO.setFirstName("Ndeugoe");
//        userCreateDTO.setLastName("Samuel");
//        userCreateDTO.setPassword("jiatou14101987");
//        userCreateDTO.setPhoneNumber("694923568");
//        userCreateDTO.setAddress("Bonamoussadi");
//        userCreateDTO.setRoleid(this.roles.getId());
//
//        return UserService.createUser(userCreateDTO);
//    }

    private RolePermissions checkRolePermission(Roles r, Permission p) {
        RolePermissions rp = null;
        try {
            rp = rolePermissionsRepositorie.findByRoleAndPermission(r.getId(), p.getId());
            if (rp.getId() != null && rp.getId() != null) {
                return rp;
            }
        } catch (Exception e) {
            //rp = rolePermissionsRepositorie.save(new RolePermissions(r, p));
        }

        rp = rolePermissionsRepositorie.save(new RolePermissions(r, p));
        return rp;
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
    public UserDTO getAdmin() {

        Profil profil = new Profil();
        profil = profilRepository.findByCode("ADMIN");
        if (profil == null) {
            profil = new Profil("ADMIN");
            profil.setDescription("administrateur");
            profil.setLockDuration(0);
            profil.setNberEchecCnx(0);
            profil.setPwdDuration(0);
            profilRepository.save(profil);
        }
    

        profil = new Profil();
        profil = profilRepository.findByCode("CAISSE");
        if (profil == null) {
            profil = new Profil("CAISSE");
            profil.setDescription("CAISSIER(E)");
            profil.setLockDuration(0);
            profil.setNberEchecCnx(0);
            profil.setPwdDuration(0);
            profilRepository.save(profil);

        }
//        parMap.put("code", "admin");
//        Roles role = new Roles();
//        role = roleDao.findEntityByUsingQueryName(Roles.FIND_BY_CODE, parMap);
//        if (role == null) {
//            role = new Roles("admin");
//            role.setDescription("administrateur");
//            roleDao.create(role);
//        }
//
//        logger.debug("le role:{0}", role.toString());
//
//        parMap.clear();
//        parMap.put("matricule", "admin");
//        Personne user = new Personne();
//        user = userDao.findEntityByUsingQueryName(Personne.FIND_BY_MATRICULE, parMap);
//
//        if (user == null) {
//            user = new Personne("admin");
//            user.setPwd(Crypto.sha256("admin"));
//            user.setNom("admin");
//            user.setCompteActif(true);
//
//            user.setProfilid(profil);
//            user.setRole(role);
//
//            userDao.create(user);
//        }

 Optional<Roles> role_admin = roleRepository.findByName("admin");
        List<Permission> allPermission = new ArrayList<>();
//        if (role_admin.isPresent() == Boolean.FALSE) {
// ajoute des permissions
        Stream.of(OperationType.WRITE, OperationType.READ,
                OperationType.UPDATE, OperationType.DELETE)
                .forEach(per -> {
//                    System.out.println("sortie..." + permissionRepository.findByName(per.name()));
                    if (permissionRepository.findByName(per.name()).isPresent() == Boolean.FALSE) {
                        permissionRepository.save(new Permission(per));
                    }
                });
        Roles role = new Roles("admin");
        if (roleRepository.findByName(role.getName()).isPresent() == Boolean.FALSE) {
            role = roleRepository.save(role);
        } else {
            String nameRole = role.getName();
            role = roleRepository.findByName(role.getName()).orElseThrow(() -> new ResourceNotFoundException("Role Not Exist Create Role " + nameRole));
        }
        this.roles = role;

        Set<Permission> listePermission = new HashSet<>(permissionRepository.findAll());
        List<RolePermissions> listeRolePermission = listePermission.stream()
                .map(per -> this.checkRolePermission(this.roles, per)).collect(Collectors.toList());

//        }
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUserName("admin");
        //userCreateDTO.setEmail("frankjiatou@gmail.com");
        userCreateDTO.setFirstName("Ndeugoe");
        userCreateDTO.setLastname("Samuel");
        userCreateDTO.setPassword("jiatou14101987");
       // userCreateDTO.setPhoneNumber("694923568");
       // userCreateDTO.setAddress("Bonamoussadi");
        //userCreateDTO.setRole(Ma);
        

        return UserService.createUser(userCreateDTO);


    }

   

}
