package com.mproduits.services;


import com.entreprise.recette.dto.PhotocopieSummaryDTO;
import com.mproduits.dto.PhotocopieDTO;
import com.mproduits.enums.TypeMois;
import com.mproduits.exceptions.MetierException;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.mappers.PhotocopieMapper;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Personne;
import com.mproduits.model.Photocopie;
import com.mproduits.repositories.BoutiqueRepositories;
//import com.mproduits.model.Photocopie;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PersonneRepositories;
import com.mproduits.repositories.PhotocopieRepository;
import com.mproduits.utiles.IdleDate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.ErrorResponseException;

/**
 * Service métier pour la gestion des photocopies.
 * Contient toute la logique métier et les règles de validation.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PhotocopieService {

    private final PhotocopieRepository photocopieRepository;
    private final EntrepriseRepositories entrepriseRepository;
    private final BoutiqueRepositories boutiqueRepositories;
     private final PersonneRepositories personneRepositories;
    private final PhotocopieMapper photocopieMapper;//findByEntrepriseAndNumero
     private final MoisRepositories moisRepositories;

    /**
     * Crée une nouvelle photocopie.
     * Valide les données métier avant la création.
     *
     * @param dto Les données de la photocopie à créer
     * @return Le DTO de la photocopie créée
     * @throws ResourceNotFoundException Si l'entreprise ou le mois n'existe pas
     * @throws BusinessException Si les règles métier ne sont pas respectées
     */
    public PhotocopieDTO create(PhotocopieDTO dto) {
        log.info("Création d'une nouvelle photocopie : {}", dto.getLibelle());

        // Récupération et validation de l'entreprise
        Entreprise entreprise = entrepriseRepository.findByActif(Boolean.TRUE);
         
               Boutique boutique=boutiqueRepositories.findById(dto.getBoutiqueid())
                       .orElseThrow(() -> new ResourceNotFoundException(
                        "Boutique non trouvée avec l'ID : " + dto.getBoutiqueid()));
               Personne personne=personneRepositories.findByUserName(dto.getUsername())
                       .orElseThrow(() -> new ResourceNotFoundException(
                        "username non trouvée avec l'ID : " + dto.getUsername()));

        int mois =IdleDate.getMonth(new Date());
        Mois moisexist=moisRepositories.findOneByAnneeAndNumero(entreprise.getAnnee().getId(), mois).orElseThrow(() -> new MetierException("mois n existe pas"));
        if (moisexist==null ||  moisexist.getId()==null) {
            moisexist=new Mois();
            moisexist.setAnnee(entreprise.getAnnee());
            moisexist.setCode(getNumeroMois(mois).name());
            moisexist.setNumero(mois);
            moisexist.setMois(getNumeroMois(mois).name());
           moisexist= moisRepositories.save(moisexist);
        }


        // Création de l'entité
        Photocopie photocopie = photocopieMapper.toEntity(dto);
        photocopie.setEntreprise(entreprise);
        photocopie.setMois(moisexist);
        photocopie.setCreatedBy(dto.getUsername());
        photocopie.setBoutique(boutique);
        photocopie.setPersonne(personne);

        // Sauvegarde
        Photocopie savedPhotocopie = photocopieRepository.save(photocopie);
        log.info("Photocopie créée avec succès - ID: {}", savedPhotocopie.getId());

        return photocopieMapper.toDTO(savedPhotocopie);
    }

    /**
     * Met à jour une photocopie existante.
     *
     * @param id L'identifiant de la photocopie à modifier
     * @param dto Les nouvelles données
     * @return Le DTO de la photocopie modifiée
     * @throws ResourceNotFoundException Si la photocopie n'existe pas
     * @throws BusinessException Si les règles métier ne sont pas respectées
     */
    public PhotocopieDTO update(Long id, PhotocopieDTO dto) {
        log.info("Mise à jour de la photocopie ID: {}", id);

        // Récupération de l'entité existante
        Photocopie existingPhotocopie = photocopieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photocopie non trouvée avec l'ID : " + id));
        
       
        if (Boolean.TRUE.equals(existingPhotocopie.getIsDeleted())) {
            throw new Error("Impossible de modifier une photocopie supprimée");
        }

        // Validation du mois
//        Mois mois = existingPhotocopie.getMois();
//        if (Boolean.TRUE.equals(mois.getIsClosed())) {
//            throw new BusinessException("Le mois est clôturé, impossible de modifier");
//        }

//        // Validation de la date si elle a changé
//        if (!existingPhotocopie.getDateReception().equals(dto.getDateReception())) {
//            validateDateReception(dto.getDateReception(), mois);
//        }

        // Mise à jour des champs
        photocopieMapper.updateEntityFromDTO(existingPhotocopie, dto);
        existingPhotocopie.setUpdatedBy(dto.getUsername());

        // Sauvegarde
        Photocopie updatedPhotocopie = photocopieRepository.save(existingPhotocopie);
        log.info("Photocopie mise à jour avec succès - ID: {}", id);

        return photocopieMapper.toDTO(updatedPhotocopie);
    }

    /**
     * Récupère une photocopie par son identifiant.
     *
     * @param id L'identifiant de la photocopie
     * @return Le DTO de la photocopie
     * @throws ResourceNotFoundException Si la photocopie n'existe pas
     */
    @Transactional(readOnly = true)
    public PhotocopieDTO findById(Long id) {
        log.debug("Recherche de la photocopie ID: {}", id);

        Photocopie photocopie = photocopieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photocopie non trouvée avec l'ID : " + id));

        return photocopieMapper.toDTO(photocopie);
    }

    /**
     * Récupère toutes les photocopies d'un mois avec pagination.
     *
     * @param moisId L'identifiant du mois
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     * @throws ResourceNotFoundException Si le mois n'existe pas
     */
    @Transactional(readOnly = true)
    public Page<PhotocopieDTO> findByMois(int anneeid,Long boutiqueid,String username, Pageable pageable) {
        log.debug("Recherche des photocopies du mois ID: {}", anneeid);

      

        Page<Photocopie> photocopies = photocopieRepository.findByMoisAndNotDeleted(anneeid,boutiqueid,username, pageable);

        return photocopies.map(photocopieMapper::toDTO);
    }

    /**
     * Récupère les photocopies avec filtres.
     *
     * @param moisId Identifiant du mois
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @param libelle Libellé à rechercher (optionnel)
     * @param pageable Paramètres de pagination
     * @return Une page de photocopies filtrées
     */
    @Transactional(readOnly = true)
    public Page<PhotocopieDTO> findWithFilters(int anneeid,Long boutiqueid,String username, LocalDate dateDebut, 
                                                LocalDate dateFin, String libelle, 
                                                Pageable pageable) {
        log.debug("Recherche avec filtres - Mois: {}, Libellé: {}", anneeid, libelle);

        

        Page<Photocopie> photocopies;

        // Application des filtres
        if (libelle != null && !libelle.trim().isEmpty()) {
            photocopies = photocopieRepository.searchByLibelleInMois(libelle, anneeid,boutiqueid, pageable);
        } else {
            photocopies = photocopieRepository.findByMoisAndNotDeleted(anneeid,boutiqueid,username, pageable);
        }

        return photocopies.map(photocopieMapper::toDTO);
    }

    /**
     * Supprime logiquement une photocopie.
     * La photocopie n'est pas supprimée physiquement mais marquée comme supprimée.
     *
     * @param id L'identifiant de la photocopie à supprimer
     * @throws ResourceNotFoundException Si la photocopie n'existe pas
     * @throws BusinessException Si les règles métier ne sont pas respectées
     */
    public void delete(Long id) {
        log.info("Suppression de la photocopie ID: {}", id);

        Photocopie photocopie = photocopieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photocopie non trouvée avec l'ID : " + id));

        if (Boolean.TRUE.equals(photocopie.getIsDeleted())) {
            throw new Error("La photocopie est déjà supprimée");
        }

       

        // Suppression logique
       // photocopie.markAsDeleted(DtoInstantiatingConverter.());
        photocopieRepository.save(photocopie);

        log.info("Photocopie supprimée logiquement - ID: {}", id);
    }

    /**
     * Restaure une photocopie supprimée logiquement.
     *
     * @param id L'identifiant de la photocopie à restaurer
     * @return Le DTO de la photocopie restaurée
     * @throws ResourceNotFoundException Si la photocopie n'existe pas
     * @throws BusinessException Si la photocopie n'est pas supprimée
     */
    public PhotocopieDTO restore(Long id) {
        log.info("Restauration de la photocopie ID: {}", id);

        Photocopie photocopie = photocopieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photocopie non trouvée avec l'ID : " + id));

//        if (!Boolean.TRUE.equals(photocopie.getIsDeleted())) {
//            throw new BusinessException("La photocopie n'est pas supprimée");
//        }

       // photocopie.restore();
        Photocopie restoredPhotocopie = photocopieRepository.save(photocopie);

        log.info("Photocopie restaurée avec succès - ID: {}", id);

        return photocopieMapper.toDTO(restoredPhotocopie);
    }

    /**
     * Calcule le résumé des photocopies pour un mois donné.
     *
     * @param moisId L'identifiant du mois
     * @return Le DTO contenant les statistiques
     * @throws ResourceNotFoundException Si le mois n'existe pas
     */
    @Transactional(readOnly = true)
    public PhotocopieSummaryDTO getSummary(int anneeid,Long boutiqueid,String username) {
        log.debug("Calcul du résumé pour le anneeid ID: {}", anneeid);

//        Mois mois = moisRepository.findById(moisId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Mois non trouvé avec l'ID : " + moisId));

        // Récupération de toutes les photocopies du mois
        List<Photocopie> photocopies = photocopieRepository.findByMoisAndDateReceptionBetween(anneeid,boutiqueid, LocalDate.MIN, LocalDate.MAX,username);
              

        // Calculs statistiques
        BigDecimal totalMontant = photocopies.stream()
                .map(Photocopie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreEntrees = photocopies.size();

        BigDecimal montantMoyen = nombreEntrees > 0
                ? totalMontant.divide(BigDecimal.valueOf(nombreEntrees), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal montantMin = photocopies.stream()
                .map(Photocopie::getMontant)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal montantMax = photocopies.stream()
                .map(Photocopie::getMontant)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return PhotocopieSummaryDTO.builder()
                .totalMontant(totalMontant)
                .nombreEntrees(nombreEntrees)
                .periodeDebut(LocalDate.MIN)
                .periodeFin(LocalDate.MAX)
                .montantMoyen(montantMoyen)
                .montantMin(montantMin)
                .montantMax(montantMax)
               // .moisId(mois.getId())
               // .moisLibelle(mois.getLibelle())
                .build();
    }

    /**
     * Valide que la date de réception est comprise dans le mois.
     *
     * @param dateReception La date à valider
     * @param mois Le mois comptable
     * @throws BusinessException Si la date n'est pas valide
     */
    private void validateDateReception(LocalDate dateReception, Mois mois) {
//        if (!mois.containsDate(dateReception)) {
//            throw new BusinessException(
//                    String.format("La date de réception doit être comprise entre %s et %s",
//                            mois.getDateDebut(), mois.getDateFin()));
//        }
//
//        if (dateReception.isAfter(LocalDate.now())) {
//            throw new BusinessException("La date de réception ne peut pas être dans le futur");
//        }
    }

    /**
     * Récupère le nom d'utilisateur de l'utilisateur connecté.
     * Utilise Spring Security pour récupérer l'utilisateur courant.
     *
     * @return Le nom d'utilisateur ou "system" si non authentifié
     */
//    private String getCurrentUsername() {
////        try {
////            return SecurityContextHolder.getContext().getAuthentication().getName();
////        } catch (Exception e) {
////            log.warn("Impossible de récupérer l'utilisateur connecté, utilisation de 'system'");
////            return "system";
////        }
//    }
    
     public TypeMois getNumeroMois(int numero) {
   
    switch (numero) {
      case 1:
        return TypeMois.JANVIER;
       // break;
      case 2:
        return TypeMois.FEVRIER;
     //   break;
      case 3:
      return TypeMois.MARS;
    //    break;
      case 4:
        return TypeMois.AVRIL;
     //   break;
      case 5:
          return TypeMois.MAI;
      //  break;
      case 6:
          return TypeMois.JUIN;
     //   break;
      case 7:
       return TypeMois.JUILLET;
      //  break;
      case 8:
        return TypeMois.AOUT;
       // break;
      case 9:
         return TypeMois.SEPTEMBRE;
      //  break;
      case 10:
          return TypeMois.OCTOBRE;
       // break;
      case 11:
         return TypeMois.NOVEMBRE;
      //  break;
      case 12:
        return TypeMois.DECEMBRE;
       // break;
    } 
      return TypeMois.JANVIER;
  }
}
