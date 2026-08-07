/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

/**
 *
 * @author USER01
 */

import com.mproduits.model.TransfertStock;
    import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransfertStockRepository extends JpaRepository<TransfertStock, Long> {

    /**
     * Récupère tous les transferts d'un magasin source
     *
     * @param magasinSourceId ID du magasin source
     * @return Liste des transferts sortants
     */
    List<TransfertStock> findBySourceId(Long magasinSourceId);

    /**
     * Récupère tous les transferts vers un magasin destination
     *
     * @param magasinDestinationId ID du magasin destination
     * @return Liste des transferts entrants
     */
    List<TransfertStock> findByDestinationId(Long magasinDestinationId);

    /**
     * Récupère tous les transferts d'un produit
     *
     * @param produitId ID du produit
     * @return Liste des transferts du produit
     */
    List<TransfertStock> findByProduitId(Long produitId);

    // --- Variantes scopees par compagnie (voir TransfertController) ---

    Optional<TransfertStock> findByIdAndEntreprise_EntreprisePK_CompagnieId(Long id, Long compagnieId);

    @Query("SELECT t FROM TransfertStock t WHERE t.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY t.dateTransfert DESC")
    List<TransfertStock> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT t FROM TransfertStock t WHERE t.source.id = :sourceId AND t.entreprise.entreprisePK.compagnieId = :compagnieId")
    List<TransfertStock> findBySourceIdAndCompagnieId(@Param("sourceId") Long sourceId, @Param("compagnieId") Long compagnieId);

    @Query("SELECT t FROM TransfertStock t WHERE t.destination.id = :destinationId AND t.entreprise.entreprisePK.compagnieId = :compagnieId")
    List<TransfertStock> findByDestinationIdAndCompagnieId(@Param("destinationId") Long destinationId, @Param("compagnieId") Long compagnieId);

    @Query("SELECT t FROM TransfertStock t WHERE t.produit.id = :produitId AND t.entreprise.entreprisePK.compagnieId = :compagnieId")
    List<TransfertStock> findByProduitIdAndCompagnieId(@Param("produitId") Long produitId, @Param("compagnieId") Long compagnieId);

    /**
     * Récupère les transferts d'un produit spécifique dans une plage de dates
     * 
     * @param produitId ID du produit
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des transferts filtrés
     */
    List<TransfertStock> findByProduitIdAndDateTransfertBetween(
            Long produitId, Date dateDebut, Date dateFin);

    /**
     * Récupère les transferts entre deux magasins spécifiques
     * 
     * @param sourceId ID du magasin source
     * @param destinationId ID du magasin destination
     * @return Liste des transferts entre ces magasins
     */
    List<TransfertStock> findBySourceIdAndDestinationId(Long sourceId, Long destinationId);

    /**
     * Récupère les transferts dans une plage de dates
     * 
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des transferts dans la période
     */
    List<TransfertStock> findByDateTransfertBetween(Date dateDebut, Date dateFin);

    /**
     * Calcule la valeur totale des transferts d'un magasin source
     * Requête personnalisée JPA
     * 
     * @param magasinSourceId ID du magasin source
     * @return Somme des valeurs estimées
     */
    @Query("SELECT COALESCE(SUM(t.valeurEstimation), 0) FROM TransfertStock t WHERE t.source.id = :sourceId")
    BigDecimal calculerValeurSortieParMagasin(@Param("sourceId") Long magasinSourceId);

    /**
     * Calcule la valeur totale des transferts reçus par un magasin destination
     * 
     * @param magasinDestinationId ID du magasin destination
     * @return Somme des valeurs estimées
     */
    @Query("SELECT COALESCE(SUM(t.valeurEstimation), 0) FROM TransfertStock t WHERE t.destination.id = :destinationId")
    BigDecimal calculerValeurEntreeParMagasin(@Param("destinationId") Long magasinDestinationId);

    /**
     * Récupère les derniers N transferts (pour l'historique récent)
     * 
     * @param limit Nombre de transferts à récupérer
     * @return Liste des N derniers transferts
     */
    @Query(value = "SELECT t FROM TransfertStock t ORDER BY t.dateTransfert DESC LIMIT :limit", 
           nativeQuery = false)
    List<TransfertStock> findDerniersTransferts(@Param("limit") int limit);

    /**
     * Récupère le nombre total de transferts effectués
     * 
     * @return Nombre total de transferts
     */
    long count();

    /**
     * Récupère le nombre de transferts d'un produit
     * 
     * @param produitId ID du produit
     * @return Nombre de transferts
     */
    long countByProduitId(Long produitId);
}
