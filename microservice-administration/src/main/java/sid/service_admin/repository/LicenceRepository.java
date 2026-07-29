package sid.service_admin.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.Licence;

public interface LicenceRepository extends JpaRepository<Licence, Long> {
    Optional<Licence> findByCompagnie_Id(Long compagnieId);
}
