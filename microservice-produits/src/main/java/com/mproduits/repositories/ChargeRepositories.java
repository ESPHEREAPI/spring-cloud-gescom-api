package com.mproduits.repositories;

import com.mproduits.model.Charge;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRepositories extends JpaRepository<Charge, Long> {

    List<Charge> findByBoutique_Compagnie_Id(Long compagnieId);

    Optional<Charge> findByIdAndBoutique_Compagnie_Id(Long id, Long compagnieId);

    @Query("SELECT c FROM Charge c WHERE c.boutique.id = :boutiqueId AND c.dateCharge BETWEEN :debut AND :fin ORDER BY c.dateCharge DESC")
    List<Charge> findByBoutiqueAndPeriode(@Param("boutiqueId") Long boutiqueId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.montant), 0) FROM Charge c WHERE c.boutique.id = :boutiqueId AND c.dateCharge BETWEEN :debut AND :fin")
    BigDecimal sumByBoutiqueAndPeriode(@Param("boutiqueId") Long boutiqueId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
