/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Mois;

/**
 *
 * @author USER01
 */
@Repository
public interface MoisRepository extends JpaRepository<Mois, Long>{
    

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId AND m.numero = :numero")
    Mois findOneByAnneeAndNumero(@Param("annee") int anneeId, @Param("numero") int numero);

    
}
