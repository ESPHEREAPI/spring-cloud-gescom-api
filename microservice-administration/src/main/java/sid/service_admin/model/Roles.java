/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author USER01
 */
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role")
@Data
//@AllArgsConstructor
@NoArgsConstructor
public class Roles implements Serializable {

   @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
  @Column(name ="code" ,nullable = false, unique = true)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "lastUserModif")
    private String lastUserModif;
    @Column(name = "lastdateModif")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastdateModif;
    @Column(name = "statut")
    private String statut;
    @Column(name = "userDelete")
    private String userDelete;
    @OneToMany(mappedBy = "roleid")
    private List<Personne> personneList;

   // private static final long serialVersionUID = 1L;
   
  


//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "role_permission",
//            joinColumns = @JoinColumn(name = "role_id"),
//            inverseJoinColumns = @JoinColumn(name = "permission_id")
//    )
//    private Set<Permission> permissions = new HashSet<>();

//    @ManyToMany(mappedBy = "roles")
//    private Set<User> users = new HashSet<>();
    @OneToMany(mappedBy = "role")
    private Set<RolePermissions> rolePermissionses = new HashSet<>();

    public Roles(String name) {
        this.name = name;
        this.description = name;

    }
    
    

//    // Méthodes pour gérer les permissions
//    public void addPermission(Permission permission) {
//        this.permissions.add(permission);
//    }
//
//    public void removePermission(Permission permission) {
//        this.permissions.remove(permission);
//    }
//
//    public boolean hasPermission(String permissionName) {
//        return this.permissions.stream().anyMatch(permission -> permission.getName().equals(permissionName));
//    }

   

}
