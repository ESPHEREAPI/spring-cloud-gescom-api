package sid.service_admin.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.LicenceSettingsDTO;
import sid.service_admin.model.LicenceSettings;
import sid.service_admin.repository.LicenceSettingsRepository;

/** Parametres de l'essai gratuit en libre-service - reserve au SUPER_ADMIN. */
@Service
public class LicenceSettingsService {

    private static final Long SINGLETON_ID = 1L;

    private final LicenceSettingsRepository repository;

    public LicenceSettingsService(LicenceSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LicenceSettings getOrCreate() {
        return repository.findById(SINGLETON_ID).orElseGet(() -> repository.save(new LicenceSettings()));
    }

    @Transactional(readOnly = true)
    public LicenceSettingsDTO get() {
        return toDTO(getOrCreate());
    }

    @Transactional
    public LicenceSettingsDTO update(LicenceSettingsDTO dto) {
        LicenceSettings settings = getOrCreate();
        if (dto.getEssaiActif() != null) {
            settings.setEssaiActif(dto.getEssaiActif());
        }
        if (dto.getEssaiDureeJours() != null) {
            settings.setEssaiDureeJours(dto.getEssaiDureeJours());
        }
        if (dto.getEssaiMaxUtilisateurs() != null) {
            settings.setEssaiMaxUtilisateurs(dto.getEssaiMaxUtilisateurs());
        }
        if (dto.getEssaiMaxBoutiques() != null) {
            settings.setEssaiMaxBoutiques(dto.getEssaiMaxBoutiques());
        }
        if (dto.getEssaiModules() != null) {
            settings.setEssaiModules(dto.getEssaiModules());
        }
        return toDTO(repository.save(settings));
    }

    private LicenceSettingsDTO toDTO(LicenceSettings settings) {
        LicenceSettingsDTO dto = new LicenceSettingsDTO();
        BeanUtils.copyProperties(settings, dto);
        return dto;
    }
}
