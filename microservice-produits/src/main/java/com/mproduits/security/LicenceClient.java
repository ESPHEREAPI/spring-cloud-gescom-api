package com.mproduits.security;

import com.mproduits.dto.LicenceStatutDTO;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Appelle l'endpoint interne de microservice-administration pour connaitre
 * le statut de licence d'une compagnie. Timeout court, ne leve jamais
 * d'exception : un echec (reseau, admin down, 4xx/5xx) est simplement un
 * Optional vide, a charge de LicenceStatusService de decider quoi en faire
 * (repli sur le cache avec grace, ou refus).
 */
@Component
public class LicenceClient {

    private static final Logger logger = LoggerFactory.getLogger(LicenceClient.class);
    private static final String HEADER_INTERNAL_KEY = "X-Internal-Service-Key";

    @Value("${app.licence.adminBaseUrl}")
    private String adminBaseUrl;

    @Value("${app.internal.service-secret:}")
    private String internalServiceSecret;

    private RestClient restClient;

    @PostConstruct
    void init() {
        // Echoue au demarrage plutot que d'envoyer des appels internes jamais
        // authentifiables (secret vide -> LicenceStatusService refuse tout en
        // silence, deja arrive et couteux a diagnostiquer).
        if (internalServiceSecret == null || internalServiceSecret.isBlank()) {
            throw new IllegalStateException(
                    "APP_INTERNAL_SERVICE_SECRET n'est pas configure (variable d'environnement obligatoire, "
                    + "meme valeur que cote microservice-administration) - la verification de licence ne peut pas fonctionner.");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(adminBaseUrl)
                .requestFactory(factory)
                .build();
    }

    public Optional<LicenceStatutDTO> fetchStatut(Long compagnieId) {
        try {
            LicenceStatutDTO dto = restClient.get()
                    .uri("/internal/licences/compagnie/{id}/statut", compagnieId)
                    .header(HEADER_INTERNAL_KEY, internalServiceSecret)
                    .retrieve()
                    .body(LicenceStatutDTO.class);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            logger.warn("Verification de licence indisponible pour la compagnie {} : {}", compagnieId, e.getMessage());
            return Optional.empty();
        }
    }
}
