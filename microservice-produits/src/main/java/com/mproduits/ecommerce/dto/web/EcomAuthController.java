package com.mproduits.ecommerce.dto.web;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Client;
import com.mproduits.model.Compagnie;
import com.mproduits.repositories.ClientRepository;
import com.mproduits.repositories.CompagnieRepositories;
import com.mproduits.security.JwtService;
import java.time.Instant;
import java.util.Date;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compte client optionnel pour le site e-commerce public (voir
 * EcomCheckoutController pour la commande invite, qui reste possible sans
 * jamais passer par ici). Emet des JWT clients (typ=customer, voir
 * JwtService/JwtAuthFilter) - completement distincts des JWT staff emis par
 * microservice-administration, jamais acceptes sur une route interne.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/microservice-produits/e-com/compagnie")
public class EcomAuthController {

    private static final long EXPIRATION_MS = 30L * 24 * 60 * 60 * 1000; // 30 jours

    private final CompagnieRepositories compagnieRepositories;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record RegisterRequest(String email, String password, String nom, String telephone) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record AuthResponse(String token, Long clientId, String nom, String email) {
    }

    @PostMapping("/{code}/register")
    public ResponseEntity<AuthResponse> register(@PathVariable String code, @RequestBody RegisterRequest request) {
        Compagnie compagnie = resolveCompagnie(code);
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new BadRequestException("Email et mot de passe requis");
        }
        if (request.password().length() < 6) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 6 caracteres");
        }

        Client client = clientRepository.findByEmailIgnoreCaseAndCompagnie_Id(request.email(), compagnie.getId())
                .orElseGet(() -> {
                    Client nouveau = new Client();
                    nouveau.setEmail(request.email());
                    nouveau.setCompagnie(compagnie);
                    nouveau.setStatut("ACTIF");
                    return nouveau;
                });
        // Un client invite existant (cree par une commande sans compte, voir
        // EcomCheckoutController.findOrCreateGuestClient) est mis a niveau en
        // place plutot que duplique - son historique de commandes reste
        // rattache au meme Client.id une fois inscrit.
        if (!client.isGuestOnly() && client.getPasswordHash() != null) {
            throw new BadRequestException("Un compte existe deja avec cet email");
        }
        client.setPasswordHash(passwordEncoder.encode(request.password()));
        client.setGuestOnly(false);
        if (StringUtils.hasText(request.nom())) {
            client.setNom(request.nom());
        }
        if (StringUtils.hasText(request.telephone())) {
            client.setTelephone(request.telephone());
        }
        client = clientRepository.save(client);

        return ResponseEntity.ok(buildAuthResponse(client, compagnie.getId()));
    }

    @PostMapping("/{code}/login")
    public ResponseEntity<AuthResponse> login(@PathVariable String code, @RequestBody LoginRequest request) {
        Compagnie compagnie = resolveCompagnie(code);
        Client client = clientRepository.findByEmailIgnoreCaseAndCompagnie_Id(request.email(), compagnie.getId())
                .filter(c -> c.getPasswordHash() != null)
                .filter(c -> passwordEncoder.matches(request.password(), c.getPasswordHash()))
                .orElseThrow(() -> new BadRequestException("Email ou mot de passe incorrect"));

        return ResponseEntity.ok(buildAuthResponse(client, compagnie.getId()));
    }

    private AuthResponse buildAuthResponse(Client client, Long compagnieId) {
        Date expiry = Date.from(Instant.now().plusMillis(EXPIRATION_MS));
        String token = jwtService.generateCustomerToken(client.getId(), compagnieId, expiry);
        return new AuthResponse(token, client.getId(), client.getNom(), client.getEmail());
    }

    private Compagnie resolveCompagnie(String code) {
        return compagnieRepositories.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Compagnie introuvable : " + code));
    }
}
