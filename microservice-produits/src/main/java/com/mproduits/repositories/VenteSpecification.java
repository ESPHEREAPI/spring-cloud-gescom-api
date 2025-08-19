/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.enums.StatutVente;
import com.mproduits.model.Personne;
import com.mproduits.model.Vente;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;


/**
 *
 * @author USER01
 */
public class VenteSpecification {
    public static Specification<Vente> hasStatut(StatutVente statut) {
        return (root, query, cb) -> cb.equal(root.get("statut"), statut);
    }

    public static Specification<Vente> dateVenteBetween(LocalDateTime debut, LocalDateTime fin) {
        return (root, query, cb) -> cb.between(root.get("dateVente"), debut, fin);
    }
    
     // Nouvelle spécification pour la recherche
    public static Specification<Vente> searchByNumeroTicketOrUser(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return null;
            }
            
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            
            return criteriaBuilder.or(
                // Recherche par numéro de ticket (insensible à la casse)
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("numeroTicket")), 
                    likePattern
                ),
                // Recherche par nom d'utilisateur/caissier (insensible à la casse)
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("userinsert")), 
                    likePattern
                )
            );
        };
    }
    
    // Alternative si vous avez une relation avec User
    public static Specification<Vente> searchByNumeroTicketOrUserAdvanced(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return null;
            }
            
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            
            // Si vous avez une relation avec une entité User
            Join<Vente, Personne> userJoin = root.join("vendeur", JoinType.LEFT);
            
            return criteriaBuilder.or(
                // Recherche par numéro de ticket
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("numeroTicket")), 
                    likePattern
                ),
                // Recherche par nom d'utilisateur
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("userinsert")), 
                    likePattern
                ),
                // Recherche par nom complet de l'utilisateur (si relation existe)
                criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("nom")), 
                    likePattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("prenom")), 
                    likePattern
                )
            );
        };
    }
}


