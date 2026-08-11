package com.mproduits.repositories;

import com.mproduits.model.HistoriqueCorrectionStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueCorrectionStockRepository extends JpaRepository<HistoriqueCorrectionStock, Long> {

    @Query("SELECT h FROM HistoriqueCorrectionStock h WHERE h.compagnie.id = :compagnieId ORDER BY h.dateCorrection DESC")
    List<HistoriqueCorrectionStock> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);
}
