///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package sid.service_admin.model;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import java.io.Serializable;
//
///**
// *
// * @author USER01
// */
//import lombok.Data;
//import jakarta.persistence.*;
//import java.util.Date;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//
//@Entity
//@Table(name = "Utilisateursh2")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
//public class User implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//    @Column(nullable = false)
//    private String userName;
//
//    private String firstName;
//
//    @Column(nullable = false)
//    private String lastName;
//
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(nullable = false)
//    private boolean isActive = true;
//
//    @Column
//    private String phoneNumber;
//
//    @Column
//    private String address;
//
//    @Column
//    private String profileImageUrl;
//
//    @Column(nullable = false)
//    private String createdBy;
//
//    @Column(nullable = true)
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date updatedAt;
//    @Column(nullable = true)
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date lastlogin;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date createdAt;
//
//    @Column
//    private String lastModifiedBy;
//
//    @Column
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date lastModifiedDate;
//
////    @ManyToMany(fetch = FetchType.EAGER)
////    @JoinTable(
////            name = "user_role",
////            joinColumns = @JoinColumn(name = "user_id"),
////            inverseJoinColumns = @JoinColumn(name = "role_id")
////    )
////    private Set<Role> roles = new HashSet<>();
//    //@OneToMany(mappedBy = "user")
//    // private Set<UserRole> userRoles = new HashSet<>();
//    //private Set<Role> roles = new HashSet<>();
//    @JoinColumn(name = "Role", referencedColumnName = "id")
//    @ManyToOne(optional = false)
//    private Role role;
//
////    // Méthodes pour gérer les rôles
////    public void addRole(Role role) {
////        this.roles.add(role);
////    }
////
////    public void removeRole(Role role) {
////        this.roles.remove(role);
////    }
////
////    public boolean hasRole(String roleName) {
////        return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
////    }
////
////    public boolean hasPermission(String permissionName) {
////        return this.roles.stream()
////                .flatMap(role -> role.getPermissions().stream())
////                .anyMatch(permission -> permission.getName().equals(permissionName));
////    }
//}
