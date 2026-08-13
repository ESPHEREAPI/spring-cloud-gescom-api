package com.mproduits.repositories;

import com.mproduits.model.Personne;
import com.mproduits.model.PersonnePermissionException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonnePermissionExceptionRepositories extends JpaRepository<PersonnePermissionException, Long> {

    List<PersonnePermissionException> findByPersonne(Personne personne);
}
