package com.mproduits.repositories;

import com.mproduits.model.StockImportFormat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockImportFormatRepositories extends JpaRepository<StockImportFormat, Long> {
    Optional<StockImportFormat> findByCompagnie_Id(Long compagnieId);
}
