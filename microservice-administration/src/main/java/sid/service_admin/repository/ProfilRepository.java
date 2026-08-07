/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Profil;

/**
 *
 * @author USER01
 */
@Repository
public interface ProfilRepository extends JpaRepository<Profil, Long>{
    Profil findByCode(@Param("code")String code);
    List<Profil> findByCompagnie_Id(Long compagnieId);
    Profil findByCodeAndCompagnie_Id(String code, Long compagnieId);
    long countByCompagnie_Id(Long compagnieId);
}
