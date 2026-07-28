/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Entreprise;
import com.mproduits.model.Fournisseur;
import com.mproduits.model.Magasin;
import com.mproduits.model.Mois;
import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface MoisRepositories extends JpaRepository<Mois, Long> {

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId")
    List<Mois> findAllByAnneeId(@Param("anneeId") Integer anneeId);

    @Query("SELECT m FROM Mois m WHERE m.annee.id >= :anneeId")
    List<Mois> findAllByEntreprise(@Param("anneeId") Integer anneeId);

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId ")
    List<Mois> findAllByAnneeGrouped(@Param("anneeId") Integer anneeId);

    @Query("SELECT m FROM Mois m WHERE m.annee.id >= :anneeId AND m.numero = :numero ")
    List<Mois> findByEntrepriseAndNumero(@Param("anneeId") Integer anneeId, @Param("numero") int numero);

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId AND m.numero = :numero ")
    Optional<Mois> findByAnneeAndNumeroAndType(@Param("anneeId") Integer anneeId, @Param("numero") int numero);

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId  AND m.code = :code")
    Mois findByAnneeAndTypeAndCode(@Param("anneeId") Integer anneeId, @Param("code") String code);

//    @Query("SELECT m FROM Mois m JOIN m.PrimeSalarialCollection p WHERE p.entreprise = :entreprise GROUP BY m.id")
//    List<Mois> findAllInPrimeSalarial(@Param("entreprise") Entreprise entreprise);
//    @Query("SELECT DISTINCT c.mois FROM Commande c WHERE  c.entreprise.annee.id = :anneeId")
//    List<Mois> findAllMoisByCommandeFournisseur(@Param("fournisseur") Fournisseur fournisseur, @Param("anneeId") Integer anneeId);

    @Query("SELECT m FROM Mois m WHERE m.annee.id = :anneeId AND m.numero = :numero")
    Optional<Mois>  findOneByAnneeAndNumero(@Param("annee") int anneeId, @Param("numero") int numero);

}
