package com.mproduits.services;

import com.entreprise.historique.dto.HistoriquePhotocopieDTO;
import com.mproduits.dto.HistoriquePhotocopieSummaryDTO;
import com.mproduits.dto.UserDTO;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Personne;
import com.mproduits.model.Photocopie;
import com.mproduits.repositories.EntrepriseRepository;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PersonneRepositories;
import com.mproduits.repositories.PhotocopieRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service métier pour l'historique des photocopies.
 * Gère la consultation et le filtrage des opérations de photocopie.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class HistoriquePhotocopieService {

    private final PhotocopieRepository photocopieRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final MoisRepositories moisRepository;
    private final PersonneRepositories personneRepository;
    @Autowired
    MapperDtoImpl mapperDtoImpl;

    /**
     * Récupère les dates disponibles pour un mois donné.
     * Utilisé pour remplir le sélecteur de date dans l'interface.
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @return Liste des dates avec des opérations
     * @throws ResourceNotFoundException Si l'entreprise ou le mois n'existe pas
     */
    public List<LocalDate> getAvailableDates(int anneeid, Long moisId,Long boutiqueid) {
        log.info("Récupération des dates disponibles - Entreprise: {}, Mois: {}", 
                anneeid, moisId);

        Entreprise entreprise = findEntrepriseOrThrow(anneeid);
        Mois mois = findMoisOrThrow(moisId);

        List<LocalDate> dates = photocopieRepository.findDistinctDatesByMois(entreprise,boutiqueid, mois);
        
        log.debug("Nombre de dates trouvées : {}", dates.size());
        
        return dates;
    }

    /**
     * Génère l'historique des photocopies pour une date donnée.
     * Supporte le mode multi-caisse (filtrage par caissier).
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @param date Date sélectionnée
     * @param personneId Identifiant du caissier (optionnel, mode multi-caisse)
     * @param multiCaisse Mode multi-caisse activé
     * @param pageable Paramètres de pagination
     * @return Page de DTOs d'historique
     * @throws ResourceNotFoundException Si une ressource n'existe pas
     */
    public Page<HistoriquePhotocopieDTO> getHistorique(
            int anneeid,
            Long moisId,
            Long boutiqueid,
            LocalDate date,
            String username,
            Boolean multiCaisse,
            Pageable pageable) {
        
        log.info("Génération de l'historique - Entreprise: {}, Mois: {}, Date: {}, MultiCaisse: {}", 
                anneeid, moisId, date, multiCaisse);

        Entreprise entreprise = findEntrepriseOrThrow(anneeid);
        Mois mois = findMoisOrThrow(moisId);

        Page<Photocopie> photocopies;

//        if (Boolean.TRUE.equals(multiCaisse) && username != null) {
            // Mode multi-caisse : filtrer par caissier
            Personne personne = findPersonneOrThrow(username);
            log.debug("Mode multi-caisse activé - Caissier: {}", personne.getNom());
            
            photocopies = photocopieRepository.findByDateAndMoisAndPersonne(
                    entreprise, mois, date, personne, pageable);
//        } else {
//            // Mode standard : toutes les opérations
//            log.debug("Mode standard - Toutes les opérations");
//            
//            photocopies = photocopieRepository.findByDateAndMois(
//                    entreprise, mois, date,boutiqueid, pageable);
//        }

        log.info("Nombre d'opérations trouvées : {} sur {} total", 
                photocopies.getNumberOfElements(), photocopies.getTotalElements());

        return photocopies.map(this::toDTO);
    }

    /**
     * Calcule le total des montants pour les paramètres donnés.
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @param date Date sélectionnée
     * @param personneId Identifiant du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return Total des montants
     * @throws ResourceNotFoundException Si une ressource n'existe pas
     */
    public BigDecimal calculateTotal(
            int anneeid ,
            Long moisId,
            Long boutiqueid,
            LocalDate date,
            String username,
            Boolean multiCaisse) {
        
        log.info("Calcul du total - Entreprise: {}, Mois: {}, Date: {}", 
                anneeid, moisId, date);

        Entreprise entreprise = findEntrepriseOrThrow(anneeid);
        Mois mois = findMoisOrThrow(moisId);

        BigDecimal total;

//        if (Boolean.TRUE.equals(multiCaisse) && username != null) {
            // Mode multi-caisse
            Personne personne = findPersonneOrThrow(username);
            total = photocopieRepository.calculateTotalByDateAndPersonne(
                    entreprise, mois, date, personne);
//        } else {
//            // Mode standard
//            total = photocopieRepository.calculateTotalByDate(entreprise, mois,boutiqueid, date);
//        }

        log.debug("Total calculé : {}", total);

        return total;
    }

    /**
     * Récupère le résumé/statistiques de l'historique.
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @param date Date sélectionnée
     * @param personneId Identifiant du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return DTO contenant les statistiques
     */
    public HistoriquePhotocopieSummaryDTO getSummary(
            int  anneeid,
            Long moisId,
            LocalDate date,
            Long boutiqueid,
            String username,
            Boolean multiCaisse) {
        
        log.info("Calcul du résumé/statistiques - Entreprise: {}, Mois: {}, Date: {}", 
                anneeid, moisId, date);

        Entreprise entreprise = findEntrepriseOrThrow(anneeid);
        Mois mois = findMoisOrThrow(moisId);
  Personne personne=findPersonneOrThrow(username);
        // Calcul du total
        BigDecimal total = calculateTotal(anneeid, moisId,boutiqueid, date, username, multiCaisse);

        // Compte le nombre d'opérations
        Long count = photocopieRepository.countByDate(entreprise, mois,boutiqueid, date);

        // Calcul de la moyenne
        BigDecimal moyenne = count > 0 
                ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Nom du caissier si filtre actif
        String caissier = null;
        if (Boolean.TRUE.equals(multiCaisse) && username != null) {
             personne = findPersonneOrThrow(username);
            caissier = personne.getNom();
        }

        return HistoriquePhotocopieSummaryDTO.builder()
                .totalMontant(total)
                .nombreOperations(count)
                .montantMoyen(moyenne)
                .dateDebut(date)
                .dateFin(date)
                .moisLibelle(mois.getMois())
                .annee(mois.getAnnee().getId())
                .caissier(caissier)
                .build();
    }

    /**
     * Recherche dans l'historique par libellé.
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @param searchTerm Terme de recherche
     * @param pageable Paramètres de pagination
     * @return Page de résultats
     */
    public Page<HistoriquePhotocopieDTO> searchByLibelle(
            int anneeid,
            Long moisId,
             Long boutiqueid,
            String searchTerm,
            Pageable pageable) {
        
        log.info("Recherche dans l'historique - Terme: {}", searchTerm);

        Entreprise entreprise = findEntrepriseOrThrow(anneeid);
        Mois mois = findMoisOrThrow(moisId);

        Page<Photocopie> results = photocopieRepository.searchByLibelle(
                entreprise, mois,boutiqueid, searchTerm, pageable);

        return results.map(this::toDTO);
    }

    /**
     * Génère un rapport PDF de l'historique.
     *
     * @param entrepriseId Identifiant de l'entreprise
     * @param moisId Identifiant du mois
     * @param date Date sélectionnée
     * @param personneId Identifiant du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return Contenu du PDF en bytes
     */
    public byte[] generatePDFReport(
            Long entrepriseId,
            Long moisId,
            LocalDate date,
            Long personneId,
            Boolean multiCaisse) {
        
        log.info("Génération du rapport PDF - Entreprise: {}, Mois: {}, Date: {}", 
                entrepriseId, moisId, date);

        // TODO: Implémenter la génération PDF avec JasperReports ou iText
        // Pour l'instant, retourner un tableau vide
        return new byte[0];
    }

    /**
     * Convertit une entité Photocopie en DTO.
     *
     * @param photocopie L'entité à convertir
     * @return Le DTO correspondant
     */
    private HistoriquePhotocopieDTO toDTO(Photocopie photocopie) {
        String nomCaissier = null;
        if (photocopie.getPersonne() != null) {
            nomCaissier = photocopie.getPersonne().getNom();
        }

        return HistoriquePhotocopieDTO.builder()
                .id(photocopie.getId())
                .libelle(photocopie.getLibelle())
                .montant(photocopie.getMontant())
                .dateReception(photocopie.getDateReception())
                .heure(photocopie.getCreatedAt())
                .nomCaissier(nomCaissier)
                .reference(photocopie.getCreatedBy())
                //.quantite(photocopie.)
                //.typeOperation(photocopie.getTypeOperation())
                .observations(photocopie.getCommentaire())
                .moisLibelle(photocopie.getMois().getMois())
                .annee(photocopie.getMois().getAnnee().getId())
                .build();
    }

    /**
     * Trouve une entreprise par ID ou lance une exception.
     */
    private Entreprise findEntrepriseOrThrow(int anneeid) {
        return entrepriseRepository.findByAnneeId(anneeid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entreprise non trouvée avec l'ID : " + anneeid));
    }

    /**
     * Trouve un mois par ID ou lance une exception.
     */
    private Mois findMoisOrThrow(Long id) {
        return moisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mois non trouvé avec l'ID : " + id));
    }

    /**
     * Trouve une personne par ID ou lance une exception.
     */
    private Personne findPersonneOrThrow(String username) {
        return personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personne non trouvée avec l'ID : " + username));
    }
    
    public List<Mois>getAllMoisByAnneeForCaissier(int anneeid,String usrname){
        Personne personne =this.findPersonneOrThrow(usrname);
        if ("CAISSE".equals(personne.getProfilid().getCode())) {
            
            return photocopieRepository.listMoisByAnnee(anneeid, usrname);
        }
        return photocopieRepository.listMoisByAnnee(anneeid, personne.getBoutique().getId());
    }
    
    public List<UserDTO>getAllCaisssierByBoutique(Long boutiqueid){
        List<Personne> allCaissierBoutique= photocopieRepository.listCaissier(boutiqueid, "CAISSE");
     return allCaissierBoutique.stream().map(this::personneToUserDto).toList();
    }
    
    private UserDTO personneToUserDto(Personne user){
        return this.mapperDtoImpl.mapToDTO(user);
    }
}
