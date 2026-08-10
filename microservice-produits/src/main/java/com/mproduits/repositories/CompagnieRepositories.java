package com.mproduits.repositories;

import com.mproduits.model.Compagnie;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompagnieRepositories extends JpaRepository<Compagnie, Long> {

    Optional<Compagnie> findByCode(String code);
}
