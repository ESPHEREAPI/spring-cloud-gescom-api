package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Action;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    Optional<Action> findByCode(String code);
    List<Action> findAllByOrderByLibelleAsc();
}
