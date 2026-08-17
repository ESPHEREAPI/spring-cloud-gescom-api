package sid.service_admin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.StockImportFormat;

@Repository
public interface StockImportFormatRepository extends JpaRepository<StockImportFormat, Long> {
    Optional<StockImportFormat> findByCompagnie_Id(Long compagnieId);
}
