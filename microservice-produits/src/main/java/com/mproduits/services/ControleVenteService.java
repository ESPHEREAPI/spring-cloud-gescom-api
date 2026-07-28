package com.mproduits.services;

import com.mproduits.dto.ControleVenteDTO;

import com.mproduits.dto.ControleVente;
import com.mproduits.dto.ControleVenteSummaryDTO;
import com.mproduits.enums.StatutVente;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Photocopie;
import com.mproduits.model.Vente;
import com.mproduits.model.VersementClient;
import com.mproduits.repositories.EntrepriseRepository;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PhotocopieRepository;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.repositories.VersementClientRepository;
import com.mproduits.utiles.IdleDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service métier pour le contrôle des ventes. Consolide toutes les recettes
 * (caisse, clients, photocopies, ressources) et génère des rapports journaliers
 * et mensuels.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-28
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ControleVenteService {

    private final LigneVenteRepositories caisseRepository;
    private final VenteRepositories venteRepository;
    private final VersementClientRepository versementRepository;
    private final PhotocopieRepository photocopieRepository;

    private final MoisRepositories moisRepository;
    private final EntrepriseRepository entrepriseRepository;
    private List<Vente> caissesDates;
    private List<VersementClient> versementDates;
    private List<Photocopie> photocopies;
    private Set<LocalDate> allDates;

    /**
     * Génère le contrôle des ventes pour un mois donné. Consolide toutes les
     * opérations par jour.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @return Liste des contrôles de vente par jour
     * @throws ResourceNotFoundException Si le mois ou l'entreprise n'existe pas
     */
    public List<ControleVenteDTO> generateControleVentes(Long moisId, int anneeid, Long boutiqueid) {
        log.info("Génération du contrôle des ventes - Mois: {}, Entreprise: {}", moisId, anneeid);

        // Récupération et validation du mois
        Mois mois = moisRepository.findOneByAnneeAndNumero(anneeid,moisId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Mois non trouvé avec l'ID : " + moisId));

        // Récupération et validation de l'entreprise
        Entreprise entreprise = entrepriseRepository.findByAnneeId(anneeid)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise non trouvée avec l'ID : " + anneeid));

        // Récupération de toutes les dates distinctes ayant des opérations
        Set<LocalDate> allDates = collectAllDatesWithOperations(mois, entreprise, boutiqueid);

        // Génération des contrôles de vente pour chaque date
        List<ControleVente> controles = allDates.stream()
                .map(date -> generateControleVenteForDate(date, mois, entreprise))
                .filter(ControleVente::hasData)
                .sorted(Comparator.comparing(ControleVente::getDate))
                .collect(Collectors.toList());

        log.info("Contrôle des ventes généré : {} jours avec des opérations", controles.size());

        return controles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Génère le contrôle de vente pour une date spécifique.
     *
     * @param date La date du contrôle
     * @param mois Le mois comptable
     * @param entreprise L'entreprise
     * @return Le contrôle de vente pour cette date
     */
    private ControleVente generateControleVenteForDate(LocalDate date, Mois mois, Entreprise entreprise) {
        log.debug("Génération du contrôle pour la date : {}", date);

        // Calcul des montants pour chaque type d'opération
        // BigDecimal caisse = this.caissesDates.stream().filter(v-> date.equals(getLocalDateForDate(v.getDateVente()))==Boolean.TRUE && v.getTotalRemise()==BigDecimal.ZERO).mapToDouble(vt -> vt.getTotalNet()).
        BigDecimal caisse = this.caissesDates.stream()
                .filter(v -> date.compareTo(getLocalDateForDate(v.getDateVente())) == 0)
                .filter(v -> v.getStatut().equals(StatutVente.TERMINEE)==Boolean.TRUE)
                .map(v -> v.getTotalBrut())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal client = this.versementDates.stream()
                .filter(v -> date.compareTo(getLocalDateForDate(v.getDateVersement())) == 0)
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal photocopies = this.photocopies.stream()
                .filter(v -> date.compareTo(v.getDateReception()) == 0)
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal resources = BigDecimal.ZERO;
        BigDecimal remise = this.caissesDates.stream()
                .filter(v -> date.compareTo(getLocalDateForDate(v.getDateVente())) == 0)
                .filter(v -> v.getStatut().equals(StatutVente.TERMINEE)==Boolean.TRUE)
                .filter(v -> v.getTotalRemise().compareTo(BigDecimal.ZERO) > 0)
                .map(v -> v.getTotalRemise())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Création du contrôle de vente
        ControleVente controle = ControleVente.builder()
                .date(date)
                .caisse(caisse)
                .client(client)
                .photocopies(photocopies)
                .resources(resources)
                .remise(remise)
                .moisId(mois.getId())
                .moisLibelle(mois.getCode())
                .anneeId(entreprise.getAnnee().getId())
                .anneeValeur(entreprise.getAnnee().getId())
                .build();

        // Calcul du total
        controle.calculateTotal();

        return controle;
    }

    /**
     * Collecte toutes les dates ayant des opérations pour un mois donné.
     * Combine les dates de toutes les sources (caisse, versements, photocopies,
     * ressources).
     *
     * @param mois Le mois comptable
     * @param entreprise L'entreprise
     * @return Ensemble de toutes les dates avec opérations
     */
    private Set<LocalDate> collectAllDatesWithOperations(Mois mois, Entreprise entreprise, Long boutiqueid) {
        allDates = new HashSet<>();

        // Dates des opérations de caisse
        caissesDates = venteRepository.listeAllVenteByBoutique(entreprise.getAnnee().getId(), boutiqueid,mois.getNumero());
        allDates.addAll(caissesDates.stream().map(vt -> getLocalDateForDate(vt.getDateVente())).toList());

        // Dates des versements (via requête groupée)
        versementDates = versementRepository.listeClientVersementByDateVersement(boutiqueid, entreprise.getAnnee().getId(),mois.getNumero());
        allDates.addAll(versementDates.stream()
                .map(vcl -> getLocalDateForDate(vcl.getDateVersement())).toList());

        // Dates des photocopies
        photocopies = photocopieRepository.listePhotocopieByBoutiqueByAnnee(entreprise.getAnnee().getId(), boutiqueid,mois.getNumero());
        photocopies.forEach(p -> allDates.add(p.getDateReception()));
//photocopies.forEach(p -> {
//    if (p.getDateReception() != null) {
//        LocalDate date = extractDate(p.getDateReception());
//        if (date != null) {
//            allDates.add(date);
//        }
//    }
//});

//        // Dates des ressources
//        List<Object[]> resourceDates = resourceRepository.getTotalsByDateForMois(mois, entreprise);
//        resourceDates.forEach(result -> allDates.add((LocalDate) result[0]));
        // Dates des remises
        log.debug("Collecte de {} dates distinctes avec opérations", allDates.size());

        return allDates;
    }

    private LocalDate extractDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDate.of(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth()
        );
    }

    /**
     * Génère le résumé global d'un mois. Calcule les totaux de toutes les
     * recettes.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @return Le résumé du mois
     * @throws ResourceNotFoundException Si le mois ou l'entreprise n'existe pas
     */
    public ControleVenteSummaryDTO getSummary(Long moisId, int anneeid) {
        log.info("Génération du résumé - Mois: {}, Entreprise: {}", moisId, anneeid);

//        Mois mois = moisRepository.findById(moisId)
//                .orElseThrow(() -> new ResourceNotFoundException("Mois non trouvé avec l'ID : " + moisId));

//        Entreprise entreprise = entrepriseRepository.findByAnneeId(anneeid)
//                .orElseThrow(() -> new ResourceNotFoundException("Entreprise non trouvée avec l'ID : " + anneeid));

        // Calcul des totaux mensuels
        BigDecimal totalCaisse = this.caissesDates.stream()
                // .filter(v -> date.equals(getLocalDateForDate(v.getDateVente())))
               // .filter(v -> v.getTotalRemise().equals(BigDecimal.ZERO) == BigDecimal.)
                .map(v -> v.getTotalBrut())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalClient = this.versementDates.stream()
                //.filter(v -> date.equals(getLocalDateForDate(v.getDateVersement())))
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPhotocopies = this.photocopies.stream()
                //.filter(v -> date.equals(v.getDateReception()))
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalResources = BigDecimal.ZERO;
        BigDecimal totalRemises = this.caissesDates.stream()
                //.filter(v -> date.equals(getLocalDateForDate(v.getDateVente())))
                .filter(v -> v.getTotalRemise().compareTo(BigDecimal.ZERO) > 0)
                .map(v -> v.getTotalNet())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcul du total général
        BigDecimal totalGeneral = totalCaisse
                .add(totalClient)
                .add(totalPhotocopies)
                .add(totalResources);

        BigDecimal totalNet = totalGeneral.subtract(totalRemises);

        // Calcul du nombre de jours avec des opérations
        // Set<LocalDate> datesWithOperations = collectAllDatesWithOperations(mois, entreprise);
        int nombreJours = this.allDates.size();

        // Calcul de la moyenne journalière
        BigDecimal moyenneJournaliere = nombreJours > 0
                ? totalNet.divide(BigDecimal.valueOf(nombreJours), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Date minimale
        LocalDate dateMin = allDates.stream()
                .min(LocalDate::compareTo)
                .orElse(null);

// Date maximale
        LocalDate dateMax = allDates.stream()
                .max(LocalDate::compareTo)
                .orElse(null);

        return ControleVenteSummaryDTO.builder()
                .totalCaisse(totalCaisse)
                .totalClient(totalClient)
                .totalPhotocopies(totalPhotocopies)
                .totalResources(totalResources)
                .totalRemises(totalRemises)
                .totalGeneral(totalGeneral)
                .totalNet(totalNet)
                .nombreJours(nombreJours)
                .moyenneJournaliere(moyenneJournaliere)
                .periodeDebut(dateMin)
                .periodeFin(dateMax)
              //  .moisId(mois.getId())
               // .moisLibelle(mois.getCode())
                .build();
    }

    public LocalDate getLocalDateForDate(Date date) {
        return IdleDate.getDateForLocalDate(date);
    }

    /**
     * Génère le contrôle des ventes entre deux dates.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @return Liste des contrôles de vente filtrés
     */
    public List<ControleVenteDTO> generateControleVentesForPeriod(
            Long moisId, Long boutiqueid, int anneeid, LocalDate dateDebut, LocalDate dateFin) {

        log.info("Génération du contrôle pour la période : {} - {}", dateDebut, dateFin);

        // Générer tous les contrôles du mois
        List<ControleVenteDTO> allControles = generateControleVentes(moisId, anneeid, boutiqueid);

        // Filtrer par période si nécessaire
        if (dateDebut != null && dateFin != null) {
            return allControles.stream()
                    .filter(c -> !c.getDate().isBefore(dateDebut) && !c.getDate().isAfter(dateFin))
                    .collect(Collectors.toList());
        } else if (dateDebut != null) {
            return allControles.stream()
                    .filter(c -> !c.getDate().isBefore(dateDebut))
                    .collect(Collectors.toList());
        } else if (dateFin != null) {
            return allControles.stream()
                    .filter(c -> !c.getDate().isAfter(dateFin))
                    .collect(Collectors.toList());
        }

        return allControles;
    }

    /**
     * Convertit une entité ControleVente en DTO.
     *
     * @param controle L'entité à convertir
     * @return Le DTO correspondant
     */
    private ControleVenteDTO toDTO(ControleVente controle) {
        return ControleVenteDTO.builder()
                .date(controle.getDate())
                .caisse(controle.getCaisse())
                .client(controle.getClient())
                .photocopies(controle.getPhotocopies())
                .resources(controle.getResources())
                .remise(controle.getRemise())
                .total(controle.getTotal())
                .totalNet(controle.getTotalNet())
                .moisId(controle.getMoisId())
                .moisLibelle(controle.getMoisLibelle())
                .anneeId(controle.getAnneeId())
                .anneeValeur(controle.getAnneeValeur())
                .build();
    }
}
