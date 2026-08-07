package com.mproduits.repositories;

import com.mproduits.model.Ressource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RessourceRepositories extends JpaRepository<Ressource, Long> {

    List<Ressource> findByBoutique_Compagnie_Id(Long compagnieId);

    Optional<Ressource> findByIdAndBoutique_Compagnie_Id(Long id, Long compagnieId);

    @Query("SELECT r FROM Ressource r WHERE r.boutique.id = :boutiqueId AND r.dateRessource BETWEEN :debut AND :fin ORDER BY r.dateRessource DESC")
    List<Ressource> findByBoutiqueAndPeriode(@Param("boutiqueId") Long boutiqueId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(r.montant), 0) FROM Ressource r WHERE r.boutique.id = :boutiqueId AND r.dateRessource = :date")
    BigDecimal sumByBoutiqueAndDate(@Param("boutiqueId") Long boutiqueId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(r.montant), 0) FROM Ressource r WHERE r.boutique.id = :boutiqueId AND r.dateRessource BETWEEN :debut AND :fin")
    BigDecimal sumByBoutiqueAndPeriode(@Param("boutiqueId") Long boutiqueId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
