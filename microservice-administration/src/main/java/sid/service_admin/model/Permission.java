/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.OperationType;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "permission")
@Data

@AllArgsConstructor
@NoArgsConstructor
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    public Permission(OperationType operationType) {
        this.operationType = operationType;
        this.description = operationType.name();
        name = operationType.name();
    }
     @OneToMany(mappedBy = "permission")
    private Set<RolePermissions> rolePermissionses = new HashSet<>();


//    @ManyToMany(mappedBy = "permissions")
//    private Set<Role> roles = new HashSet<>();
//     @OneToMany(mappedBy = "permission")
//    private Set<RolePermissions> rolePermissionses = new HashSet<>();

//    // Méthodes pour gérer les rôles
//    public void addRole(Role role) {
//        this.roles.add(role);
//    }
//
//    public void removeRole(Role role) {
//        this.roles.remove(role);
//    }

   

}
