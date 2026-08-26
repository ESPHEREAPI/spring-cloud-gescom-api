package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findByPersonne_IdAndUsedFalse(Long personneId);
}
