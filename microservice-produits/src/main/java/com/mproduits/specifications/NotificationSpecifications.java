package com.mproduits.specifications;

import com.mproduits.model.NotificationClient;
import com.mproduits.dto.NotificationSearchCriteria;
import com.mproduits.enums.TypeNotification;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationSpecifications {

    public static Specification<NotificationClient> withCriteria(NotificationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔹 Filtrer par client
            if (criteria.getClientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), criteria.getClientId()));
            }

            // 🔹 Filtrer par facture liée
            if (criteria.getFactureId() != null) {
                predicates.add(cb.equal(root.get("facture").get("id"), criteria.getFactureId()));
            }

            // 🔹 Filtrer par type de notification (enum)
            if (criteria.getTypeNotification() != null) {
                predicates.add(cb.equal(root.get("typeNotification"), criteria.getTypeNotification()));
            }

            // 🔹 Filtrer par catégorie
            if (criteria.getCategorie() != null && !criteria.getCategorie().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("categorie")), "%" + criteria.getCategorie().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par statut
            if (criteria.getStatut() != null && !criteria.getStatut().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("statut")), criteria.getStatut().toLowerCase()));
            }

            // 🔹 Filtrer par destinataire
            if (criteria.getDestinataire() != null && !criteria.getDestinataire().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("destinataire")), "%" + criteria.getDestinataire().toLowerCase() + "%"));
            }

            // 🔹 Filtrer par priorité
            if (criteria.getPriorite() != null) {
                predicates.add(cb.equal(root.get("priorite"), criteria.getPriorite()));
            }

            // 🔹 Filtrer par date de création (intervalle)
            if (criteria.getDateCreationDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateCreation"), criteria.getDateCreationDebut()));
            }
            if (criteria.getDateCreationFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateCreation"), criteria.getDateCreationFin()));
            }

            // 🔹 Filtrer par date d’envoi (intervalle)
            if (criteria.getDateEnvoiDebut() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateEnvoi"), criteria.getDateEnvoiDebut()));
            }
            if (criteria.getDateEnvoiFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateEnvoi"), criteria.getDateEnvoiFin()));
            }

            // ✅ Combinaison de tous les filtres avec AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
