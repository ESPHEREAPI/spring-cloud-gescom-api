package sid.service_admin.repository;

///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
// */
//package sid.service_admin.dao;
//
//import feign.Param;
//import java.util.List;
//import java.util.Set;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import sid.service_admin.model.Role;
//import sid.service_admin.model.User;
//import sid.service_admin.model.UserRole;
//
///**
// *
// * @author USER01
// */
//public interface UserRoleRepository extends JpaRepository<UserRole, Long>{
// public UserRole findByRoleAndUser(Role role,User user);   
// public List<UserRole>findByRole(Role role);
// @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id= :roleId")
// public Set<Role> listeRolesByUser(@Param("roleId") Long roleId);
//}
