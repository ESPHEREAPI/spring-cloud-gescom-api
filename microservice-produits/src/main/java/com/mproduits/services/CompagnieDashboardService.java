package com.mproduits.services;

import com.mproduits.dto.CompagnieDashboardDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompagnieDashboardService {

    private final VenteRepositories venteRepositories;
    private final FactureRepository factureRepository;
    private final ServiceCommande serviceCommande;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    public CompagnieDashboardDTO genererDashboard() {
        Long compagnieId = compagnieCourante();

        LocalDate aujourdHui = LocalDate.now();
        Date debutJour = toDate(aujourdHui);
        Date finJour = toDate(aujourdHui.plusDays(1));

        LocalDate debutMoisDate = aujourdHui.withDayOfMonth(1);
        Date debutMois = toDate(debutMoisDate);
        Date finMois = toDate(debutMoisDate.plusMonths(1));

        BigDecimal totalVentesJour = venteRepositories.sumTotalNetByCompagnieAndPeriode(compagnieId, debutJour, finJour);
        long nombreVentesJour = venteRepositories.countByCompagnieAndPeriode(compagnieId, debutJour, finJour);

        BigDecimal totalVentesMois = venteRepositories.sumTotalNetByCompagnieAndPeriode(compagnieId, debutMois, finMois);
        long nombreVentesMois = venteRepositories.countByCompagnieAndPeriode(compagnieId, debutMois, finMois);

        Object[] impaye = factureRepository.calculerImpayeParCompagnie(compagnieId);
        BigDecimal montantImpaye = impaye != null && impaye[0] != null ? (BigDecimal) impaye[0] : BigDecimal.ZERO;
        long nombreFacturesImpayees = impaye != null && impaye[1] != null ? (Long) impaye[1] : 0L;

        long nombreProduitsStockFaible;
        try {
            nombreProduitsStockFaible = serviceCommande.alertstock(new BigDecimal(10)).size();
        } catch (Exception e) {
            nombreProduitsStockFaible = 0L;
        }

        List<CompagnieDashboardDTO.PointEvolution> evolutionVentes = genererEvolution6Mois(compagnieId, aujourdHui);

        List<Object[]> repartition = venteRepositories.aggregerVentesParBoutique(compagnieId, debutMois, finMois);
        List<CompagnieDashboardDTO.VenteParBoutique> ventesParBoutique = new ArrayList<>();
        for (Object[] ligne : repartition) {
            ventesParBoutique.add(CompagnieDashboardDTO.VenteParBoutique.builder()
                    .boutiqueId((Long) ligne[0])
                    .boutiqueNom((String) ligne[1])
                    .total((BigDecimal) ligne[2])
                    .build());
        }

        return CompagnieDashboardDTO.builder()
                .totalVentesJour(totalVentesJour)
                .nombreVentesJour(nombreVentesJour)
                .totalVentesMois(totalVentesMois)
                .nombreVentesMois(nombreVentesMois)
                .montantImpaye(montantImpaye)
                .nombreFacturesImpayees(nombreFacturesImpayees)
                .nombreProduitsStockFaible(nombreProduitsStockFaible)
                .evolutionVentes(evolutionVentes)
                .ventesParBoutique(ventesParBoutique)
                .build();
    }

    private List<CompagnieDashboardDTO.PointEvolution> genererEvolution6Mois(Long compagnieId, LocalDate reference) {
        List<CompagnieDashboardDTO.PointEvolution> evolution = new ArrayList<>();
        SimpleDateFormat libelleMois = new SimpleDateFormat("MMM yyyy", Locale.FRENCH);

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(reference).minusMonths(i);
            Date debut = toDate(ym.atDay(1));
            Date fin = toDate(ym.plusMonths(1).atDay(1));

            BigDecimal total = venteRepositories.sumTotalNetByCompagnieAndPeriode(compagnieId, debut, fin);
            long nombre = venteRepositories.countByCompagnieAndPeriode(compagnieId, debut, fin);

            evolution.add(CompagnieDashboardDTO.PointEvolution.builder()
                    .mois(libelleMois.format(debut))
                    .total(total)
                    .nombre(nombre)
                    .build());
        }
        return evolution;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
