///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package sid.service_admin.service;
//
///**
// *
// * @author USER01
// */
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import sid.service_admin.dao.RolePermissionsRepositorie;
//import sid.service_admin.dao.UserRepository;
//import sid.service_admin.model.Permission;
//import sid.service_admin.model.User;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    @Autowired
//    private UserRepository userRepository;
// 
//
//    @Autowired
//    private RolePermissionsRepositorie rolePermissionsRepositorie;
//
//    @Override
//    @Transactional
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//
//        List<GrantedAuthority> authorities = user.getUserRoles().stream()
//                .map(roleUser -> new SimpleGrantedAuthority("ROLE_" + roleUser.getRole().getName()))
//                .collect(Collectors.toList());
//        Set<Permission> permissions = new HashSet<>();
//        permissions = user.getUserRoles().stream()
//                .flatMap(r -> rolePermissionsRepositorie.listePermissionsByRoles(r.getId()).stream())
//                .collect(Collectors.toSet());
//
//        permissions.forEach(permission
//                -> authorities.add(new SimpleGrantedAuthority(permission.getName()))
//        );
//
//        return new UserDetailsImpl(
//                user.getId(),
//                user.getEmail(),
//                user.getPassword(),
//                user.isActive(),
//                authorities
//        );
//    }
//}
