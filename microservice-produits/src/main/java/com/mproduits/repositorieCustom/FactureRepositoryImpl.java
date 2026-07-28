/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.repositorieCustom;

import com.mproduits.dto.FactureStatistiques;
import com.mproduits.model.Facture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
/**
 *
 * @author USER01
 */
@Repository
public class FactureRepositoryImpl implements FactureRepositoryCustom{
   @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(readOnly = true)
    public FactureStatistiques calculerStatistiques(Date dateDebut, Date dateFin) {
        // JPQL agrégé (sans constructor expression) -> renvoie Object[]
 
            String jpql = """
            SELECT 
                COUNT(f),
                SUM(CASE WHEN f.statut = 'BROUILLON' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'NON_PAYEE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'PARTIELLE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'PAYEE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'EN_RETARD' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'ANNULEE' THEN 1 ELSE 0 END),
                COALESCE(SUM(f.totalHt), 0),
                COALESCE(SUM(f.totalTtc), 0),
                COALESCE(SUM(f.montantPaye), 0),
                COALESCE(SUM(f.soldeRestant), 0),
                COALESCE(SUM(CASE WHEN f.statut = 'EN_RETARD' THEN f.soldeRestant ELSE 0 END), 0),
                CASE WHEN SUM(f.totalTtc) > 0 THEN (SUM(f.montantPaye) * 1.0 / SUM(f.totalTtc) * 100.0) ELSE 0 END
            FROM Facture f
            WHERE f.dateFacture BETWEEN :debut AND :fin
        """;
            
            
        Query q = em.createQuery(jpql);
        q.setParameter("debut", dateDebut);
        q.setParameter("fin", dateFin);

        Object single = q.getSingleResult(); // renvoie Object[] avec les agrégats
        Object[] row = (single instanceof Object[] arr) ? arr : new Object[] { single };

        // Conversion sécurisée
        Long totalFactures = toLong(row[0]);
        Long brouillon = toLong(row[1]);
        Long nonPayees = toLong(row[2]);
        Long partielles = toLong(row[3]);
        Long payees = toLong(row[4]);
        Long enRetard = toLong(row[5]);
        Long annulees = toLong(row[6]);

        BigDecimal totalHt = toBigDecimal(row[7]);
        BigDecimal totalTtc = toBigDecimal(row[8]);
        BigDecimal montantPaye = toBigDecimal(row[9]);
        BigDecimal montantImpaye = toBigDecimal(row[10]);
        BigDecimal montantEnRetard = toBigDecimal(row[11]);

        // tauxRecouvrement = Double (pour correspondre au constructeur que tu as)
        Double tauxRecouvrement = toDouble(row[12]);

        // Calcul du délai moyen en Java : on récupère les factures payées (datePaiementComplet not null)
        String q2 = "SELECT f FROM Facture f WHERE f.dateFacture BETWEEN :debut AND :fin";
        List<Facture> factures = em.createQuery(q2, Facture.class)
                .setParameter("debut", dateDebut)
                .setParameter("fin", dateFin)
                .getResultList();

        // calcul moyenne jours entre dateFacture et datePaiementComplet
        double moyenneJours = factures.stream()
                .filter(f -> f.getDatePaiementComplet() != null && f.getDateFacture() != null)
                .mapToLong(f -> {
                    long days = ChronoUnit.DAYS.between(
                            f.getDateFacture().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            f.getDatePaiementComplet().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    );
                    return Math.max(days, 0);
                })
                .average()
                .orElse(0.0);

        // Construire le DTO en utilisant le constructeur que tu as (les deux derniers params en Double)
        FactureStatistiques stats = new FactureStatistiques(
                totalFactures,
                brouillon,
                nonPayees,
                partielles,
                payees,
                enRetard,
                annulees,
                totalHt,
                totalTtc,
                montantPaye,
                montantImpaye,
                montantEnRetard,
                tauxRecouvrement,
                moyenneJours
               

        );

        return stats;
    
    }
   
    // Helpers de conversion robustes
    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Double d) return d;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
    }

    @Override
    @Transactional(readOnly = true)
    public FactureStatistiques calculerStatistiques() {
        // JPQL agrégé (sans constructor expression) -> renvoie Object[]
 
            String jpql = """
            SELECT 
                COUNT(f),
                SUM(CASE WHEN f.statut = 'BROUILLON' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'NON_PAYEE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'PARTIELLE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'PAYEE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'EN_RETARD' THEN 1 ELSE 0 END),
                SUM(CASE WHEN f.statut = 'ANNULEE' THEN 1 ELSE 0 END),
                COALESCE(SUM(f.totalHt), 0),
                COALESCE(SUM(f.totalTtc), 0),
                COALESCE(SUM(f.montantPaye), 0),
                COALESCE(SUM(f.soldeRestant), 0),
                COALESCE(SUM(CASE WHEN f.statut = 'EN_RETARD' THEN f.soldeRestant ELSE 0 END), 0),
                CASE WHEN SUM(f.totalTtc) > 0 THEN (SUM(f.montantPaye) * 1.0 / SUM(f.totalTtc) * 100.0) ELSE 0 END
            FROM Facture f order by f.dateFacture ASC
           
        """;
            
            
        Query q = em.createQuery(jpql);
       // q.setParameter("debut", dateDebut);
       // q.setParameter("fin", dateFin);

        Object single = q.getSingleResult(); // renvoie Object[] avec les agrégats
        Object[] row = (single instanceof Object[] arr) ? arr : new Object[] { single };

        // Conversion sécurisée
        Long totalFactures = toLong(row[0]);
        Long brouillon = toLong(row[1]);
        Long nonPayees = toLong(row[2]);
        Long partielles = toLong(row[3]);
        Long payees = toLong(row[4]);
        Long enRetard = toLong(row[5]);
        Long annulees = toLong(row[6]);

        BigDecimal totalHt = toBigDecimal(row[7]);
        BigDecimal totalTtc = toBigDecimal(row[8]);
        BigDecimal montantPaye = toBigDecimal(row[9]);
        BigDecimal montantImpaye = toBigDecimal(row[10]);
        BigDecimal montantEnRetard = toBigDecimal(row[11]);

        // tauxRecouvrement = Double (pour correspondre au constructeur que tu as)
        Double tauxRecouvrement = toDouble(row[12]);

        // Calcul du délai moyen en Java : on récupère les factures payées (datePaiementComplet not null)
        String q2 = "SELECT f FROM Facture f order by f.dateFacture ASC" ;
        List<Facture> factures = em.createQuery(q2, Facture.class)
               // .setParameter("debut", dateDebut)
               // .setParameter("fin", dateFin)
                .getResultList();

        // calcul moyenne jours entre dateFacture et datePaiementComplet
        double moyenneJours = factures.stream()
                .filter(f -> f.getDatePaiementComplet() != null && f.getDateFacture() != null)
                .mapToLong(f -> {
                    long days = ChronoUnit.DAYS.between(
                            f.getDateFacture().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            f.getDatePaiementComplet().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    );
                    return Math.max(days, 0);
                })
                .average()
                .orElse(0.0);

        // Construire le DTO en utilisant le constructeur que tu as (les deux derniers params en Double)
        FactureStatistiques stats = new FactureStatistiques(
                totalFactures,
                brouillon,
                nonPayees,
                partielles,
                payees,
                enRetard,
                annulees,
                totalHt,
                totalTtc,
                montantPaye,
                montantImpaye,
                montantEnRetard,
                tauxRecouvrement,
                moyenneJours
               

        );

        return stats;
    
    }
    
}
