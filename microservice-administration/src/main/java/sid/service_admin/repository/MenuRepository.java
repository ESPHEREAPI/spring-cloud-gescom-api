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
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;

/**
 *
 * @author USER01
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>{
    List<Menu> findByModuleid(Modulesecurite moduleid);
    
    @Query("SELECT m FROM Menu m WHERE m.moduleid.id = :moduleId")
    List<Menu> findByModuleidId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT m FROM Menu m JOIN m.usermenuCollection u WHERE " +
           "m.moduleid.id = :moduleId AND u.personne.id = :userId AND (u.autorisation = true )")
    List<Menu> findMenusByModuleAndUserWithAuthorization(@Param("moduleId") Long moduleId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM Menu m JOIN m.usermenuCollection u WHERE " +
           "m.moduleid.id = :moduleId AND u.usermenuPK.userid = :userId AND (u.autorisation = false )")
    List<Menu> findMenusByModuleAndUserWithoutAuthorization(@Param("moduleId") Long moduleId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM Menu m JOIN m.usermenuCollection u WHERE u.usermenuPK.userid = :userId")
    List<Menu> findMenusByUserId(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Menu m JOIN m.usermenuCollection u WHERE " +
           "u.usermenuPK.userid = :userId AND (u.autorisation = true )")
    List<Menu> findMenusByUserIdWithAuthorization(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Menu m JOIN m.usermenuCollection u WHERE " +
           "u.usermenuPK.userid = :userId AND (u.autorisation = false )")
    List<Menu> findMenusByUserIdWithoutAuthorization(@Param("userId") Long userId);
}
