package com.mproduits.specifications;

import com.mproduits.model.VersementClient;
import com.mproduits.dto.VersementSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class VersementSpecifications {

    /**
     * A utiliser partout dans VersementService a la place de withCriteria() -
     * ajoute toujours le filtre compagnie, quels que soient les autres
     * criteres fournis par l'appelant (empeche de lister les versements
     * d'une autre compagnie en omettant simplement le filtre boutique).
     */
    public static Specification<VersementClient> withCriteriaAndCompagnie(VersementSearchCriteria criteria, Long compagnieId) {
        return withCriteria(criteria).and((root, query, cb) ->
                cb.equal(root.get("client").get("compagnie").get("id"), compagnieId));
    }

    public static Specification<VersementClient> withCriteria(VersementSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
              if (criteria.getBoutiqueid() != null) {
                predicates.add(cb.equal(root.get("facture").get("boutique").get("id"), criteria.getBoutiqueid()));
            }
            // 🔹 Filtrer par client
            if (criteria.getClientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), criteria.getClientId()));
            }

            // 🔹 Filtrer par facture liée
            if (criteria.getFactureId() != null) {
                predicates.add(cb.equal(root.get("facture").get("id"), criteria.getFactureId()));
            }

            // 🔹 Filtrer par numéro de versement
            if (criteria.getNumeroVersement() != null && !criteria.getNumeroVersement().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("numeroVersement")), 
                        "%" + criteria.getNumeroVersement().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par mode de paiement
            if (criteria.getModePaiement() != null) {
                predicates.add(cb.equal(root.get("modePaiement"), criteria.getModePaiement()));
            }

            // 🔹 Filtrer par statut
            if (criteria.getStatut() != null && !criteria.getStatut().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("statut")), criteria.getStatut().toLowerCase()));
            }

            // 🔹 Filtrer par référence de paiement
            if (criteria.getReferencePaiement() != null && !criteria.getReferencePaiement().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("referencePaiement")), 
                        "%" + criteria.getReferencePaiement().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par montant (intervalle)
            if (criteria.getMontantMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("montant"), criteria.getMontantMin()));
            }
            if (criteria.getMontantMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("montant"), criteria.getMontantMax()));
            }

            // 🔹 Filtrer par date de versement (intervalle)
            if (criteria.getDateVersementDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateVersement"), criteria.getDateVersementDebut()));
            }
            if (criteria.getDateVersementFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateVersement"), criteria.getDateVersementFin()));
            }

            // ✅ Combine toutes les conditions avec AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
