package com.mproduits.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mproduits.dto.LicenceStatutDTO;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Applique la licence a partir du statut recupere via LicenceClient, avec un
 * cache court (Caffeine, deja utilise dans ce module - voir CacheConfig) et
 * une grace hors-ligne : si l'admin est injoignable, on sert le dernier
 * statut connu pendant une fenetre courte plutot que de bloquer
 * immediatement, avant de refuser au-dela.
 */
@Component
public class LicenceStatusService {

    private static final Logger logger = LoggerFactory.getLogger(LicenceStatusService.class);

    private final LicenceClient licenceClient;

    @Value("${app.licence.graceMinutes:20}")
    private long graceMinutes;

    /** Duree pendant laquelle un statut en cache est considere frais (pas de rappel reseau). */
    private static final Duration TTL_FRAIS = Duration.ofSeconds(60);

    private final Cache<Long, CachedStatut> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    @Autowired
    public LicenceStatusService(LicenceClient licenceClient) {
        this.licenceClient = licenceClient;
    }

    public LicenceCheckResult check(Long compagnieId) {
        CachedStatut cached = cache.getIfPresent(compagnieId);
        if (cached != null && Duration.between(cached.fetchedAt, Instant.now()).compareTo(TTL_FRAIS) <= 0) {
            return evaluate(cached.statut);
        }

        Optional<LicenceStatutDTO> live = licenceClient.fetchStatut(compagnieId);
        if (live.isPresent()) {
            cache.put(compagnieId, new CachedStatut(live.get(), Instant.now()));
            return evaluate(live.get());
        }

        if (cached != null && Duration.between(cached.fetchedAt, Instant.now()).toMinutes() <= graceMinutes) {
            logger.warn("Service de licence injoignable pour la compagnie {} - repli sur le dernier statut connu (grace)", compagnieId);
            return evaluate(cached.statut);
        }

        return LicenceCheckResult.deny("Licence introuvable ou service de licence injoignable");
    }

    private LicenceCheckResult evaluate(LicenceStatutDTO statut) {
        if (!"ACTIVE".equals(statut.getStatut())) {
            return LicenceCheckResult.deny("Licence " + statut.getStatut() + " pour cette compagnie");
        }
        if (statut.getDateExpiration() != null && statut.getDateExpiration().before(new Date())) {
            return LicenceCheckResult.deny("Licence expiree pour cette compagnie");
        }
        return LicenceCheckResult.allow(statut);
    }

    private record CachedStatut(LicenceStatutDTO statut, Instant fetchedAt) {
    }
}
