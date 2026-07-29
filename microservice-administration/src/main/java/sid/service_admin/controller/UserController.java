/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.controller;

/**
 *
 * @author USER01
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.MediaType;
import sid.service_admin.dto.MenuDto;
import sid.service_admin.dto.MenuUserDTO;
import sid.service_admin.dto.ModuleDTO;
import sid.service_admin.dto.ModuleMenuExist;
import sid.service_admin.dto.ModuleUserDTO;
import sid.service_admin.dto.ProfilDTO;
import sid.service_admin.dto.RoleDTO;
import sid.service_admin.dto.UserCreateDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.dto.UserUpdateDTO;
import sid.service_admin.exceptions.ResourceNotFoundException;

import sid.service_admin.model.Indicatifpays;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.service.SecuriteService;
import sid.service_admin.service.UserService;

@RestController
//@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private SecuriteService securiteService;
    @Autowired
    private PersonneRepository personneRepository;

    @GetMapping("/users/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/users/by-username/{userName}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userName) {
        userService.deleteUser(userName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
     @GetMapping("/users/check/{username}")
    public ResponseEntity<Boolean> userexiste(@PathVariable String username) {
       Boolean resultat= userService.userExist(username);
        return new ResponseEntity<>(resultat,HttpStatus.OK);
    }

    @PostMapping(value = "/users/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
    public UserDTO registerUser(@RequestBody UserCreateDTO userCreateDTO, Authentication authentication) {
        System.out.println("" + userCreateDTO);
        UserDTO user = userService.createUser(userCreateDTO, authentication.getName());
        //return ResponseEntity.ok(user);
        // return new ResponseEntity<>(user, HttpStatus.OK);
        return user;
    }

    @GetMapping("/users/roles")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<RoleDTO>> listeRoles() {
        List<RoleDTO> roleDTO = userService.allRoles();
        return new ResponseEntity<>(roleDTO, HttpStatus.OK);
    }

    @GetMapping("/users/profils")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<ProfilDTO>> listeprofils() {
        List<ProfilDTO> profilDTOs = userService.allProfil();
        return new ResponseEntity<>(profilDTOs, HttpStatus.OK);
    }

    @GetMapping("/users/modules")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<ModuleDTO>> listeModules() {
        List<ModuleDTO> moduleDTOs = userService.allModule();
        return new ResponseEntity<>(moduleDTOs, HttpStatus.OK);
    }

    @GetMapping("/users/indicatifs-pays")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<Indicatifpays>> listeIndicatif() {
        List<Indicatifpays> listIndicatif = userService.listeIndicatif();
        return new ResponseEntity<>(listIndicatif, HttpStatus.OK);
    }

    @GetMapping("/users/{id}/modules")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<ModuleDTO>> listeModulesByUserName(@PathVariable Long id) {

        List<ModuleDTO> listeModule = userService.listeModuleByUser(id);
        return new ResponseEntity<>(listeModule, HttpStatus.OK);
    }

    @GetMapping("/users/{username}/modules-username")
    //@PreAuthorize("hasRole('ADMIN') or hasPermission('USER_READ')")
    public ResponseEntity<List<ModuleDTO>> listeModulesByUserName(@PathVariable String username) {
        Personne p = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + username));
        List<ModuleDTO> listeModule = userService.listeModuleByUser(p.getId());
        return new ResponseEntity<>(listeModule, HttpStatus.OK);
    }

//    @PutMapping("/users/modules-users")
//    public ResponseEntity<Void> updateModuleUser(@RequestBody ModuleUserDTO) {
//        List<Modulesecurite> mods = userService.findModlueByModuleDTO(modules);
//        userService.updateModuleByUser(id, mods, userinsert);
//
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
    @PutMapping("/users/modules-users")
    public ResponseEntity<Void> chargeModulesUsers(@RequestBody ModuleUserDTO moduleUserDTO) {
        System.out.println("=== DTO reçu ===");
        System.out.println("DTO complet: " + moduleUserDTO); // Utilisera toString()
        System.out.println("UserID: " + moduleUserDTO.getUserid());
        System.out.println("CreatBy: " + moduleUserDTO.getCreatBy());
        userService.updateModuleByUser(moduleUserDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/users/menus-users")
    public ResponseEntity<Void> chargeMenusUsers(@RequestBody MenuUserDTO menuUserDTO) {
        System.out.println("=== DTO reçu ===");
        System.out.println("DTO complet: " + menuUserDTO); // Utilisera toString()
        System.out.println("UserID: " + menuUserDTO.getUserid());
        System.out.println("CreatBy: " + menuUserDTO.getCreatBy());
        userService.updateModuleMenuByUser(menuUserDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users/{username}/all-menus-users")
    public ResponseEntity<MenuUserDTO> chargeMenusUsersAllModule(@PathVariable String username) {

        MenuUserDTO menuUserDTO = userService.chargeMenuUser(username);
        return new ResponseEntity<>(menuUserDTO, HttpStatus.OK);
    }

    @GetMapping("/users/modules/{moduleId}/menus")
    public ResponseEntity<List<MenuDto>> listeMenuByModule(@PathVariable Long moduleId) {
        Modulesecurite mod = userService.moduleById(moduleId);
        List<MenuDto> listMenuDto = userService.listeMenuByModule(mod);

        return new ResponseEntity<>(listMenuDto, HttpStatus.OK);

    }

    @GetMapping("/users/{userid}/modules/{moduleId}/menus")
    public ResponseEntity<List<MenuDto>> listeMenuByModuleByUser(@PathVariable Long userid, @PathVariable Long moduleId) {
        Modulesecurite mod = userService.moduleById(moduleId);
        List<MenuDto> listMenuDto = userService.listMenuByModuleByUser(userid, moduleId);

        return new ResponseEntity<>(listMenuDto, HttpStatus.OK);

    }
//       @GetMapping("/users/{userid}")
//      public ResponseEntity<List<MenuDto>> listeMenuByModuleByUser(@PathVariable Long userid,@PathVariable Long moduleId) {
//          Modulesecurite mod=userService.moduleById(moduleId);
//          List<MenuDto> listMenuDto=userService.listMenuByModuleByUser(userid, moduleId);
//          
//           return new ResponseEntity<>(listMenuDto, HttpStatus.OK);
//          
//      }

    @GetMapping("/users/{userid}/not-modules")
    public ResponseEntity<List<ModuleDTO>> listeModuleNotUser(@PathVariable Long userid) {

        List<ModuleDTO> listModuleDTO = securiteService.getModuleForNotUser(userid);

        return new ResponseEntity<>(listModuleDTO, HttpStatus.OK);

    }

    @PutMapping("/users/{userid}/update-user")
    public ResponseEntity<Void> chargeModulesUsers(@PathVariable Long userid, @RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUser(userid, userUpdateDTO);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/users/{userid}/update-user-password")
    public ResponseEntity<Void> chargeModulesUsersDataAndPassWord(@PathVariable Long userid, @RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUserDataAndPassWord(userid, userUpdateDTO);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/users/module-exist")
    public ResponseEntity<Boolean> moduleUserExist(@RequestBody ModuleMenuExist moduleMenuExist) {
        Boolean moduleExist = securiteService.checkModuleUserExiste(moduleMenuExist.getUsername(), moduleMenuExist.getModulecode());

        return new ResponseEntity<>(moduleExist, HttpStatus.OK);

    }

    @PutMapping("/users/menu-exist")
    public ResponseEntity<Boolean> menuUserExist(@RequestBody ModuleMenuExist moduleMenuExist) {
        Boolean menuExist = securiteService.checkMenuUserExiste(moduleMenuExist.getUsername(), moduleMenuExist.getModulecode(), moduleMenuExist.getMenucode());

        return new ResponseEntity<>(menuExist, HttpStatus.OK);

    }
}
