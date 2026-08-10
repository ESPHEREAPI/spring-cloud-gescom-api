package sid.service_admin.service;

import java.math.BigDecimal;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.CompagnieParametresDTO;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.CompagnieParametres;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.CompagnieParametresRepository;
import sid.service_admin.repository.PersonneRepository;

/**
 * "Option Entreprise" : parametrage libre-service (securite/connexion,
 * fiscalite par defaut, format ticket/facture) d'une compagnie par son
 * administrateur. Voir CompagnieParametres pour le detail des champs et les
 * limites actuelles (pas encore exploite par la generation de documents).
 */
@Service
public class CompagnieParametresService {

    private final CompagnieParametresRepository compagnieParametresRepository;
    private final PersonneRepository personneRepository;

    public CompagnieParametresService(CompagnieParametresRepository compagnieParametresRepository,
            PersonneRepository personneRepository) {
        this.compagnieParametresRepository = compagnieParametresRepository;
        this.personneRepository = personneRepository;
    }

    @Transactional
    public CompagnieParametresDTO getOwn(String username) {
        return toDTO(getOrCreate(compagnieDe(username)));
    }

    @Transactional
    public CompagnieParametresDTO updateOwn(String username, CompagnieParametresDTO dto) {
        validate(dto);
        CompagnieParametres parametres = getOrCreate(compagnieDe(username));
        BeanUtils.copyProperties(dto, parametres, "id", "compagnie");
        return toDTO(compagnieParametresRepository.save(parametres));
    }

    private Compagnie compagnieDe(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        return personne.getCompagnie();
    }

    private CompagnieParametres getOrCreate(Compagnie compagnie) {
        return compagnieParametresRepository.findByCompagnie_Id(compagnie.getId())
                .orElseGet(() -> compagnieParametresRepository.save(new CompagnieParametres(compagnie)));
    }

    private void validate(CompagnieParametresDTO dto) {
        if (dto.getPwdLongueurMin() != null && dto.getPwdLongueurMin() < 4) {
            throw new BadRequestException("La longueur minimale du mot de passe doit etre d'au moins 4 caracteres");
        }
        if (dto.getTentativesMaxAvantVerrouillage() != null && dto.getTentativesMaxAvantVerrouillage() < 1) {
            throw new BadRequestException("Le nombre de tentatives avant verrouillage doit etre d'au moins 1");
        }
        if (dto.getTauxTvaDefaut() != null && dto.getTauxTvaDefaut().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le taux de TVA par defaut ne peut pas etre negatif");
        }
        if (dto.getTicketLargeurMm() != null && dto.getTicketLargeurMm() < 30) {
            throw new BadRequestException("La largeur du ticket de caisse doit etre d'au moins 30 mm");
        }
        if (dto.getFactureDevise() != null && dto.getFactureDevise().isBlank()) {
            throw new BadRequestException("La devise de facturation ne peut pas etre vide");
        }
        if (dto.getFactureEcheanceJoursDefaut() != null && dto.getFactureEcheanceJoursDefaut() < 0) {
            throw new BadRequestException("Le delai de paiement par defaut ne peut pas etre negatif");
        }
        if (dto.getBonAchatDureeValiditeJours() != null && dto.getBonAchatDureeValiditeJours() < 1) {
            throw new BadRequestException("La duree de validite du bon d'achat doit etre d'au moins 1 jour");
        }
    }

    private CompagnieParametresDTO toDTO(CompagnieParametres parametres) {
        CompagnieParametresDTO dto = new CompagnieParametresDTO();
        BeanUtils.copyProperties(parametres, dto);
        return dto;
    }
}
