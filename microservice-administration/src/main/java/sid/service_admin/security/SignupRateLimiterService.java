package sid.service_admin.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Limiteur anti-abus en memoire pour les endpoints publics d'inscription
 * autonome (voir CompagnieController/CompagnieService.inscriptionAutonome
 * et renvoyerVerification) - pas de nouvelle table ni de Redis, un seul
 * processus microservice-administration tourne aujourd'hui (voir
 * docker-compose.yml) et une reinitialisation au redemarrage est
 * acceptable pour ce niveau de protection. Utilise avec deux cles
 * distinctes : IP client pour l'inscription, email demande pour le
 * renvoi de lien - empeche a la fois le spam de fausses compagnies et le
 * harcelement de la boite mail d'un tiers via le renvoi repete.
 */
@Service
public class SignupRateLimiterService {

    private final ConcurrentHashMap<String, Deque<Instant>> tentatives = new ConcurrentHashMap<>();

    /**
     * Autorise l'action si moins de maxParFenetre tentatives ont ete
     * enregistrees pour cette cle durant la fenetre glissante, et
     * enregistre cette tentative si autorisee.
     */
    public synchronized boolean autoriser(String cle, int maxParFenetre, Duration fenetre) {
        Instant maintenant = Instant.now();
        Instant limite = maintenant.minus(fenetre);

        Deque<Instant> historique = tentatives.computeIfAbsent(cle, k -> new ArrayDeque<>());
        while (!historique.isEmpty() && historique.peekFirst().isBefore(limite)) {
            historique.pollFirst();
        }

        if (historique.size() >= maxParFenetre) {
            return false;
        }
        historique.addLast(maintenant);
        return true;
    }
}
