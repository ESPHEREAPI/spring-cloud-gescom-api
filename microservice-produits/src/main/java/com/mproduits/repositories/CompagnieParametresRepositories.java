package com.mproduits.repositories;

import com.mproduits.model.CompagnieParametres;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompagnieParametresRepositories extends JpaRepository<CompagnieParametres, Long> {

    Optional<CompagnieParametres> findByCompagnie_Id(Long compagnieId);
}
