/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Usermodule;
import sid.service_admin.model.UsermodulePK;

/**
 *
 * @author USER01
 */
public interface UsermoduleRepository extends JpaRepository<Usermodule, UsermodulePK>{
     @Query("SELECT um FROM Usermodule um WHERE  um.usermodulePK.userid= :user_id and um.modulesecurite.code= :modulecode")
   Usermodule checkModuleUserExist(@Param("user_id") Long user_id,@Param("modulecode") String modulecode);
    
}
