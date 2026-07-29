package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.Compagnie;

public interface CompagnieRepository extends JpaRepository<Compagnie, Long> {

    Optional<Compagnie> findByNom(String nom);

    boolean existsByNom(String nom);

    List<Compagnie> findByActifTrue();
}
