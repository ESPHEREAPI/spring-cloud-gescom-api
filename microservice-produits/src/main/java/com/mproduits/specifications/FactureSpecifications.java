package com.mproduits.specifications;

import com.mproduits.model.Facture;
import com.mproduits.dto.FactureSearchCriteria;
import com.mproduits.enums.StatutFacture;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FactureSpecifications {

    public static Specification<Facture> withCriteria(FactureSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔹 Filtrer par client
            if (criteria.getClientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), criteria.getClientId()));
            }

            // 🔹 Filtrer par numéro de facture
            if (criteria.getNumeroFacture() != null && !criteria.getNumeroFacture().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("numeroFacture")), 
                        "%" + criteria.getNumeroFacture().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par référence externe
            if (criteria.getReferenceExterne() != null && !criteria.getReferenceExterne().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("referenceExterne")), 
                        "%" + criteria.getReferenceExterne().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par statut de facture
            if (criteria.getStatut() != null) {
                predicates.add(cb.equal(root.get("statut"), criteria.getStatut()));
            }

            // 🔹 Filtrer par date de facture (intervalle)
            if (criteria.getDateFactureDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateFacture"), criteria.getDateFactureDebut()));
            }
            if (criteria.getDateFactureFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateFacture"), criteria.getDateFactureFin()));
            }

            // 🔹 Filtrer par date d’échéance (intervalle)
            if (criteria.getDateEcheanceDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateEcheance"), criteria.getDateEcheanceDebut()));
            }
            if (criteria.getDateEcheanceFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateEcheance"), criteria.getDateEcheanceFin()));
            }

            // 🔹 Filtrer par montant total (intervalle)
            if (criteria.getMontantMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalTtc"), criteria.getMontantMin()));
            }
            if (criteria.getMontantMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalTtc"), criteria.getMontantMax()));
            }

            // 🔹 Filtrer les factures en retard
            if (Boolean.TRUE.equals(criteria.getEnRetard())) {
                Date now = new Date();
                predicates.add(cb.lessThan(root.get("dateEcheance"), now));
                predicates.add(cb.notEqual(root.get("statut"), StatutFacture.PAYEE));
            }

            // 🧩 Combinaison des filtres (AND)
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
