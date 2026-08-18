package com.mproduits.repositories;

import com.mproduits.model.Boutique;
import com.mproduits.model.HistoriqueRestaurationStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueRestaurationStockRepository extends JpaRepository<HistoriqueRestaurationStock, Long> {

    @Query("SELECT h FROM HistoriqueRestaurationStock h WHERE h.compagnie.id = :compagnieId ORDER BY h.dateRestauration DESC")
    List<HistoriqueRestaurationStock> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

    List<HistoriqueRestaurationStock> findByBoutique(Boutique boutique);

    // Suppression en masse (DML direct, aucune entite chargee) - voir
    // StockRestaurationService.reinitialiserBoutique : charger les entites
    // pour les supprimer une a une declenchait le meme "Found shared
    // references to a collection" que la restauration des qu'une reference
    // Produit dupliquee etait touchee deux fois dans la meme session.
    @Modifying
    @Query("DELETE FROM HistoriqueRestaurationStock h WHERE h.boutique = :boutique")
    int deleteByBoutiqueBulk(@Param("boutique") Boutique boutique);
}
