package com.mproduits.repositories;

import com.mproduits.model.Boutique;
import com.mproduits.model.HistoriqueRestaurationStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueRestaurationStockRepository extends JpaRepository<HistoriqueRestaurationStock, Long> {

    @Query("SELECT h FROM HistoriqueRestaurationStock h WHERE h.compagnie.id = :compagnieId ORDER BY h.dateRestauration DESC")
    List<HistoriqueRestaurationStock> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

    List<HistoriqueRestaurationStock> findByBoutique(Boutique boutique);
}
