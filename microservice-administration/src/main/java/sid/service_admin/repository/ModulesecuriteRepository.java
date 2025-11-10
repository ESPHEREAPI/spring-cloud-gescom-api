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
import sid.service_admin.model.Modulesecurite;

/**
 *
 * @author USER01
 */
@Repository
public interface ModulesecuriteRepository extends JpaRepository<Modulesecurite, Long>{
     @Query("SELECT m FROM Modulesecurite m JOIN m.usermoduleCollection u WHERE u.usermodulePK.userid = :user_id")
    List<Modulesecurite> listModulesByUserId(@Param("useri_id") Long user_id);
    
    @Query("SELECT m FROM Modulesecurite m WHERE m.id NOT IN " +
           "(SELECT mod.id FROM Modulesecurite mod JOIN mod.usermoduleCollection u WHERE u.usermodulePK.userid = :user_id)")
    List<Modulesecurite> findModulesNotAssignedToUser(@Param("user_id") Long user_id);
   
    Modulesecurite findByCode(String code);
}
