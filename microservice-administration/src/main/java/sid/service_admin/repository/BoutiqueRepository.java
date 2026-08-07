/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sid.service_admin.model.Boutique;

/**
 *
 * @author USER01
 */
public interface BoutiqueRepository extends JpaRepository<Boutique, Long>{

    @Query("SELECT b FROM Boutique b WHERE b.compagnie.id = :compagnieId")
    List<Boutique> findByCompagnieId(@Param("compagnieId") Long compagnieId);
}
