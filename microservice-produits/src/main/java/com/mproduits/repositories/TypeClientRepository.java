package com.mproduits.repositories;

import com.mproduits.model.Typeclient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeClientRepository extends JpaRepository<Typeclient, Long> {

    @Query("SELECT t FROM Typeclient t WHERE t.compagnie.id = :compagnieId AND (" +
           "LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.libelle) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Typeclient> findBySearchAndCompagnieId(@Param("search") String search, @Param("compagnieId") Long compagnieId, Pageable pageable);

    Page<Typeclient> findByCompagnie_Id(Long compagnieId, Pageable pageable);

    List<Typeclient> findByCompagnie_Id(Long compagnieId);

    Optional<Typeclient> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
