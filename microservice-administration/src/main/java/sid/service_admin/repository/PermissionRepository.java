/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.enums.OperationType;
import sid.service_admin.model.Permission;

/**
 *
 * @author USER01
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByOperationType(OperationType operationType);

    // Matrice Role x Menu x Action
    Optional<Permission> findByMenu_IdAndOperationType(Long menuId, OperationType operationType);
    List<Permission> findByMenu_Id(Long menuId);
}
