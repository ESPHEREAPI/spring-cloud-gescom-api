package com.mproduits.services;

import com.mproduits.dto.ControleVenteDTO;

import com.mproduits.dto.ControleVente;
import com.mproduits.dto.ControleVenteSummaryDTO;
import com.mproduits.enums.StatutVente;
import com.mproduits.enums.TypeMois;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Photocopie;
import com.mproduits.model.Ressource;
import com.mproduits.model.Vente;
import com.mproduits.model.VersementClient;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.EntrepriseRepository;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PhotocopieRepository;
import com.mproduits.repositories.RessourceRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.repositories.VersementClientRepository;
import com.mproduits.security.TenantContext;
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
    private final RessourceRepositories ressourceRepository;
    private final BoutiqueRepositories boutiqueRepository;
    private final TenantContext tenantContext;

    /**
     * Une boutique, plusieurs boutiques, ou aucune (= toute la compagnie
     * courante, resolue ici en la liste de toutes ses boutiques). Un
     * BoutiqueAccessGuard.verifierBoutiquesUtilisateur() est cense avoir
     * deja valide la demande avant d'arriver ici.
     */
    private List<Long> resolveBoutiqueIds(List<Long> boutiqueIds) {
        if (boutiqueIds != null && !boutiqueIds.isEmpty()) {
            return boutiqueIds;
        }
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return boutiqueRepository.findByCompagnie_Id(compagnieId).stream()
                .map(Boutique::getId)
                .collect(Collectors.toList());
    }

    /**
     * Toutes les opérations d'un mois donné, consolidées en mémoire pour un
     * seul appel. Passée en paramètre/valeur de retour plutôt qu'en champ
     * d'instance : ce service est un singleton Spring, des champs d'instance
     * seraient partagés et écrasés entre des requêtes concurrentes de
     * compagnies différentes.
     */
    private record ControleVenteData(
            List<Vente> caissesDates,
            List<VersementClient> versementDates,
            List<Photocopie> photocopies,
            List<Ressource> ressources,
            Set<LocalDate> allDates) {
    }

    /**
     * Exercice de l'annee demandee pour la compagnie courante - jamais
     * partage entre compagnies (voir EntrepriseRepository#findByEntreprisePK_AnneeIdAndEntreprisePK_CompagnieId).
     */
    private Entreprise entrepriseDeLAnnee(int anneeid) {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return entrepriseRepository.findByEntreprisePK_AnneeIdAndEntreprisePK_CompagnieId(anneeid, compagnieId)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise non trouvée avec l'ID : " + anneeid));
    }

    /**
     * Mois demande pour l'annee de l'entreprise, cree a la volee s'il n'existe
     * pas encore. Contrairement a PhotocopieService/ServiceCommande, ce
     * service doit pouvoir consulter un mois sans activite Photocopie/Commande
     * prealable (ex: seulement des Ressource/Vente) - le faire echouer avec
     * une ResourceNotFoundException (mappee en 500 par le GlobalExceptionHandler,
     * pas en 404) cassait l'ecran Controle Vente des qu'aucun autre module
     * n'avait encore materialise le Mois du mois courant.
     */
    @Transactional
    protected Mois moisDeLAnnee(Entreprise entreprise, int numero) {
        return moisRepository.findOneByAnneeAndNumero(entreprise.getAnnee().getId(), numero)
                .orElseGet(() -> {
                    Mois nouveau = new Mois();
                    nouveau.setAnnee(entreprise.getAnnee());
                    nouveau.setNumero(numero);
                    String libelle = TypeMois.values()[numero - 1].name();
                    nouveau.setCode(libelle);
                    nouveau.setMois(libelle);
                    return moisRepository.save(nouveau);
                });
    }

    /**
     * Génère le contrôle des ventes pour un mois donné. Consolide toutes les
     * opérations par jour.
     *
     * @param moisId L'identifiant du mois
     * @param anneeid L'année de l'exercice
     * @param boutiqueIds Les boutiques concernées (vide/null = toute la compagnie)
     * @return Liste des contrôles de vente par jour
     * @throws ResourceNotFoundException Si le mois ou l'entreprise n'existe pas
     */
    public List<ControleVenteDTO> generateControleVentes(Long moisId, int anneeid, List<Long> boutiqueIds) {
        log.info("Génération du contrôle des ventes - Mois: {}, Entreprise: {}", moisId, anneeid);

        // Récupération et validation de l'entreprise (scopée à la compagnie courante)
        Entreprise entreprise = entrepriseDeLAnnee(anneeid);

        // Récupération (ou création) du mois demandé
        Mois mois = moisDeLAnnee(entreprise, moisId.intValue());

        // Récupération de toutes les opérations pour ce mois/ces boutiques
        ControleVenteData data = collectAllDatesWithOperations(mois, entreprise, resolveBoutiqueIds(boutiqueIds));

        // Génération des contrôles de vente pour chaque date
        List<ControleVente> controles = data.allDates().stream()
                .map(date -> generateControleVenteForDate(date, mois, entreprise, data))
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
     * @param data Les opérations du mois déjà chargées
     * @return Le contrôle de vente pour cette date
     */
    private ControleVente generateControleVenteForDate(LocalDate date, Mois mois, Entreprise entreprise, ControleVenteData data) {
        log.debug("Génération du contrôle pour la date : {}", date);

        BigDecimal caisse = data.caissesDates().stream()
                .filter(v -> date.compareTo(getLocalDateForDate(v.getDateVente())) == 0)
                .filter(v -> v.getStatut().equals(StatutVente.TERMINEE)==Boolean.TRUE)
                .map(v -> v.getTotalBrut())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal client = data.versementDates().stream()
                .filter(v -> date.compareTo(getLocalDateForDate(v.getDateVersement())) == 0)
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal photocopies = data.photocopies().stream()
                .filter(v -> date.compareTo(v.getDateReception()) == 0)
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal resources = data.ressources().stream()
                .filter(r -> date.compareTo(r.getDateRessource()) == 0)
                .map(Ressource::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remise = data.caissesDates().stream()
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
     * Collecte toutes les opérations d'un mois donné. Combine toutes les
     * sources (caisse, versements, photocopies, ressources).
     *
     * @param mois Le mois comptable
     * @param entreprise L'entreprise
     * @return Toutes les opérations et l'ensemble des dates avec opérations
     */
    private ControleVenteData collectAllDatesWithOperations(Mois mois, Entreprise entreprise, List<Long> boutiqueIds) {
        Set<LocalDate> allDates = new HashSet<>();

        // Dates des opérations de caisse
        List<Vente> caissesDates = venteRepository.listeAllVenteByBoutiques(entreprise.getAnnee().getId(), boutiqueIds,mois.getNumero());
        allDates.addAll(caissesDates.stream().map(vt -> getLocalDateForDate(vt.getDateVente())).toList());

        // Dates des versements (via requête groupée)
        List<VersementClient> versementDates = versementRepository.listeClientVersementByDateVersementBoutiques(boutiqueIds, entreprise.getAnnee().getId(),mois.getNumero());
        allDates.addAll(versementDates.stream()
                .map(vcl -> getLocalDateForDate(vcl.getDateVersement())).toList());

        // Dates des photocopies
        List<Photocopie> photocopies = photocopieRepository.listePhotocopieByBoutiquesByAnnee(entreprise.getAnnee().getId(), boutiqueIds,mois.getNumero());
        photocopies.forEach(p -> allDates.add(p.getDateReception()));

        // Dates des ressources (revenus hors-vente)
        LocalDate debutMois = LocalDate.of(entreprise.getAnnee().getId(), mois.getNumero(), 1);
        LocalDate finMois = debutMois.withDayOfMonth(debutMois.lengthOfMonth());
        List<Ressource> ressources = ressourceRepository.findByBoutiquesAndPeriode(boutiqueIds, debutMois, finMois);
        ressources.forEach(r -> allDates.add(r.getDateRessource()));

        log.debug("Collecte de {} dates distinctes avec opérations", allDates.size());

        return new ControleVenteData(caissesDates, versementDates, photocopies, ressources, allDates);
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
     * @param boutiqueIds Les boutiques concernées (vide/null = toute la compagnie)
     * @param anneeid L'année de l'exercice
     * @return Le résumé du mois
     * @throws ResourceNotFoundException Si le mois ou l'entreprise n'existe pas
     */
    public ControleVenteSummaryDTO getSummary(Long moisId, List<Long> boutiqueIds, int anneeid) {
        log.info("Génération du résumé - Mois: {}, Entreprise: {}", moisId, anneeid);

        Entreprise entreprise = entrepriseDeLAnnee(anneeid);
        Mois mois = moisDeLAnnee(entreprise, moisId.intValue());

        ControleVenteData data = collectAllDatesWithOperations(mois, entreprise, resolveBoutiqueIds(boutiqueIds));

        // Calcul des totaux mensuels
        BigDecimal totalCaisse = data.caissesDates().stream()
                .map(v -> v.getTotalBrut())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalClient = data.versementDates().stream()
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPhotocopies = data.photocopies().stream()
                .map(v -> v.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalResources = data.ressources().stream()
                .map(Ressource::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRemises = data.caissesDates().stream()
                .filter(v -> v.getTotalRemise().compareTo(BigDecimal.ZERO) > 0)
                .map(v -> v.getTotalNet())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcul du total général
        BigDecimal totalGeneral = totalCaisse
                .add(totalClient)
                .add(totalPhotocopies)
                .add(totalResources);

        BigDecimal totalNet = totalGeneral.subtract(totalRemises);

        int nombreJours = data.allDates().size();

        // Calcul de la moyenne journalière
        BigDecimal moyenneJournaliere = nombreJours > 0
                ? totalNet.divide(BigDecimal.valueOf(nombreJours), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate dateMin = data.allDates().stream()
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate dateMax = data.allDates().stream()
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
                .build();
    }

    public LocalDate getLocalDateForDate(Date date) {
        return IdleDate.getDateForLocalDate(date);
    }

    /**
     * Génère le contrôle des ventes entre deux dates.
     *
     * @param moisId L'identifiant du mois
     * @param boutiqueIds Les boutiques concernées (vide/null = toute la compagnie)
     * @param anneeid L'année de l'exercice
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @return Liste des contrôles de vente filtrés
     */
    public List<ControleVenteDTO> generateControleVentesForPeriod(
            Long moisId, List<Long> boutiqueIds, int anneeid, LocalDate dateDebut, LocalDate dateFin) {

        log.info("Génération du contrôle pour la période : {} - {}", dateDebut, dateFin);

        // Générer tous les contrôles du mois
        List<ControleVenteDTO> allControles = generateControleVentes(moisId, anneeid, boutiqueIds);

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
