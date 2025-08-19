/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Usermenu;
import sid.service_admin.model.UsermenuPK;

/**
 *
 * @author USER01
 */
public interface UsermenuRepository extends JpaRepository<Usermenu, UsermenuPK> {
     @Query("SELECT um FROM Usermenu um WHERE  um.usermenuPK.userid= :user_id and um.menu.code= :menucode and um.menu.moduleid.code= :menucode")
   Usermenu checkModuleUserExist(@Param("user_id") Long user_id,@Param("modulecode") String modulecode,@Param("menucode") String menucode);
   
   List<Usermenu> findByPersonneAndAutorisationTrue(Personne personne);
    
}
