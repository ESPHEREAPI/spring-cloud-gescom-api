package com.mproduits.repositories;

import com.mproduits.model.MargeCible;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MargeCibleRepositories extends JpaRepository<MargeCible, Long> {
    List<MargeCible> findByCompagnie_Id(Long compagnieId);
    Optional<MargeCible> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
