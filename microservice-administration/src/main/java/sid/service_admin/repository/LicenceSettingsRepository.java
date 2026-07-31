package sid.service_admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.LicenceSettings;

public interface LicenceSettingsRepository extends JpaRepository<LicenceSettings, Long> {
}
