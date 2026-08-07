package sid.service_admin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.CompagnieParametres;

@Repository
public interface CompagnieParametresRepository extends JpaRepository<CompagnieParametres, Long> {
    Optional<CompagnieParametres> findByCompagnie_Id(Long compagnieId);
}
