/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.mapper;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import org.springframework.stereotype.Service;
import sid.service_admin.ApplicationPropertiesConfiguration;
import sid.service_admin.repository.RolePermissionsRepositorie;
import sid.service_admin.dto.PermissionDTO;
import sid.service_admin.dto.RoleDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.dto.UserSessionDTO;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Roles;
//import sid.service_admin.model.User;
import java.util.stream.Collectors;
import java.util.Optional;
import sid.service_admin.dto.MenuDto;
import sid.service_admin.dto.ModuleDTO;
import sid.service_admin.dto.ProfilDTO;
import sid.service_admin.dto.UserCreateDTO;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Profil;
import sid.service_admin.utils.JwtExpiration;

/**
 *
 * @author USER01
 */
@Service
public class MapperDtoImpl {

    @Autowired
    ApplicationPropertiesConfiguration properties;
    @Autowired
    RolePermissionsRepositorie rolePermissionsRepositorie;

    public UserDTO mapToDTO(Personne user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
      

        // Map roles
        if (user.getRoleid() != null && user.getRoleid().getId() != null) {
            userDTO.setRoleid(user.getRoleid().getId());
            userDTO.setRole(mapRoleToDTO(user.getRoleid()));
            userDTO.setProfil(mapToDTOProfil(user.getProfilid()));
        }
        System.out.println("userDto :"+userDTO.toString() );
      userDTO.setBoutiqueid(user.getBoutique().getId());
        return userDTO;
    }

    public RoleDTO mapToDTORoles(Roles r) {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(r, roleDTO);

        return roleDTO;
    }

    public ProfilDTO mapToDTOProfil(Profil pr) {
        ProfilDTO profilDTO = new ProfilDTO();
        BeanUtils.copyProperties(pr, profilDTO);

        return profilDTO;
    }

    public ModuleDTO mapToDTOModulel(Modulesecurite md) {
        ModuleDTO moduleDTO = new ModuleDTO();
        BeanUtils.copyProperties(md, moduleDTO);

        return moduleDTO;
    }

    public Modulesecurite mapToDTOModulelDTO(ModuleDTO moduleDTO) {
        Modulesecurite module = new Modulesecurite();
        BeanUtils.copyProperties(moduleDTO, module);

        return module;
    }

    public MenuDto mapToDTOMenu(Menu menu) {
        MenuDto menuDto = new MenuDto();
        BeanUtils.copyProperties(menu, menuDto);

        return menuDto;
    }
    
    public Menu mapToDTOMenu(MenuDto menuDto) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDto, menu);

        return menu;
    }
     

    public Personne mapToDTOUserCreate(UserCreateDTO userCreateDTO) {
        Personne personne = new Personne();
        BeanUtils.copyProperties(userCreateDTO, personne);
        return personne;
    }

    public PermissionDTO mapToDto(Permission permission) {
        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(permission, permissionDTO);
        return permissionDTO;
    }

    public UserSessionDTO mapUserSessionDTOByuserDTO(UserDTO userDTO) {
        UserSessionDTO userSessionDTO = new UserSessionDTO();
        userSessionDTO.setUsersDTO(userDTO);
        userSessionDTO.setExpiresAt(JwtExpiration.expiresAt(properties.getJwtExpirationMs()));
        List<String> permission = Optional
                .ofNullable(rolePermissionsRepositorie.listePermissionsByRoles(userDTO.getRoleid()))
                .orElse(Collections.emptyList())
                .stream()
                .map(per -> per.getName())
                .collect(Collectors.toList());
        userSessionDTO.setPermissions(permission);
        userSessionDTO.setToken(JwtExpiration.generateJwtToken(userSessionDTO.getUsersDTO().getUserName(), userSessionDTO.getExpiresAt()));

        return userSessionDTO;
    }

    public RoleDTO mapRoleToDTO(Roles role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setDescription(role.getDescription());

        // Map permissions - we could include this if needed
        // roleDTO.setPermissions(...);
        return roleDTO;
    }
    
    


//    public String getCurrentUsername() {

////        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
////        if (principal instanceof UserDetails) {
////            return ((UserDetails) principal).getUsername();
////        } else {
////            return principal.toString();
////        }
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication.getPrincipal() == null) {
//            // Retourner un nom par défaut pour l'initialisation ou les opérations système
//            return "system";
//        }
//
//        if (authentication.getPrincipal() instanceof UserDetails) {
//            return ((UserDetails) authentication.getPrincipal()).getUsername();
//        } else {
//            return authentication.getName();
//        }
//    }
//    
    
}
