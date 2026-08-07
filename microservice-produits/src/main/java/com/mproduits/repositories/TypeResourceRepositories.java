package com.mproduits.repositories;

import com.mproduits.model.TypeResource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeResourceRepositories extends JpaRepository<TypeResource, Long> {
    List<TypeResource> findByCompagnie_Id(Long compagnieId);
    Optional<TypeResource> findByIdAndCompagnie_Id(Long id, Long compagnieId);
}
