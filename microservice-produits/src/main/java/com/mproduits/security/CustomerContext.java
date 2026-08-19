package com.mproduits.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Miroir de TenantContext pour un client e-commerce authentifie (pas un
 * compte staff) - lit les attributs poses par JwtAuthFilter pour un token
 * "typ":"customer" (voir JwtService.generateCustomerToken). Null si la
 * requete n'a pas de token client valide (visiteur anonyme/invite) : les
 * endpoints publics qui acceptent aussi le guest checkout doivent traiter
 * null comme "pas de compte, se rabattre sur les champs invite du corps de
 * la requete", jamais comme une erreur.
 */
@Component
public class CustomerContext {

    @Autowired
    private HttpServletRequest request;

    public Long currentClientId() {
        return (Long) request.getAttribute("customerId");
    }

    public Long currentCompagnieId() {
        return (Long) request.getAttribute("customerCompagnieId");
    }
}
