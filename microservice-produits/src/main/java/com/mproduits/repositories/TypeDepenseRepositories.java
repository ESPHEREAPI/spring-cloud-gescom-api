package com.mproduits.repositories;

import com.mproduits.model.TypeDepense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeDepenseRepositories extends JpaRepository<TypeDepense, Long> {
    List<TypeDepense> findByCompagnie_Id(Long compagnieId);
    Optional<TypeDepense> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
