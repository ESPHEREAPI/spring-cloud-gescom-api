package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Personne;
import sid.service_admin.model.PersonnePermissionException;

@Repository
public interface PersonnePermissionExceptionRepository extends JpaRepository<PersonnePermissionException, Long> {

    List<PersonnePermissionException> findByPersonne(Personne personne);

    Optional<PersonnePermissionException> findByPersonneAndPermission(Personne personne, Permission permission);
}
