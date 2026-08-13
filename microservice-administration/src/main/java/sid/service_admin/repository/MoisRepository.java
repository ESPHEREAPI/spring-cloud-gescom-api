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
import sid.service_admin.model.Mois;

/**
 *
 * @author USER01
 */
@Repository
public interface MoisRepository extends JpaRepository<Mois, Long>{


    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId AND m.numero = :numero")
    Mois findOneByAnneeAndNumero(@Param("annee") int anneeId, @Param("numero") int numero);

    /**
     * Variante liste : la table mois est partagee avec microservice-produits
     * (meme schema), qui a eu (avant correction) plusieurs chemins de
     * creation racee pour le mois courant (voir ControleVenteService,
     * PhotocopieService, ServiceCommande cote microservice-produits). Un
     * doublon deja en base fait planter findOneByAnneeAndNumero avec
     * IncorrectResultSizeDataAccessException - AuthController l'appelle
     * pour TOUT login reussi, donc un tel doublon bloquait la connexion de
     * toute la compagnie.
     */
    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId AND m.numero = :numero")
    List<Mois> findAllByAnneeAndNumero(@Param("annee") int anneeId, @Param("numero") int numero);

}
