/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Roles;
import sid.service_admin.model.RolePermissions;

/**
 *
 * @author USER01
 */
@Repository
public interface RolePermissionsRepositorie extends JpaRepository<RolePermissions, Long> {

  @Query("SELECT rp FROM RolePermissions rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
public RolePermissions findByRoleAndPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);


    public List<RolePermissions> findByRole(Roles role);

    @Query("SELECT rp.permission FROM RolePermissions rp  WHERE rp.role.id= :roleid")
    public List<Permission> listePermissionsByRoles(@Param("roleid") Long roleid);
}
