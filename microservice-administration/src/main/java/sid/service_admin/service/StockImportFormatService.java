package sid.service_admin.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.StockImportFormatDTO;
import sid.service_admin.enums.ChampImportStock;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Personne;
import sid.service_admin.model.StockImportFormat;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.repository.StockImportFormatRepository;

/**
 * Format de restauration de stock, personnalise par chaque administrateur de
 * compagnie (voir StockImportFormat). Suit exactement le pattern
 * get-or-create de CompagnieParametresService.
 */
@Service
public class StockImportFormatService {

    private final StockImportFormatRepository stockImportFormatRepository;
    private final PersonneRepository personneRepository;

    public StockImportFormatService(StockImportFormatRepository stockImportFormatRepository,
            PersonneRepository personneRepository) {
        this.stockImportFormatRepository = stockImportFormatRepository;
        this.personneRepository = personneRepository;
    }

    @Transactional
    public StockImportFormatDTO getOwn(String username) {
        return toDTO(getOrCreate(compagnieDe(username)));
    }

    @Transactional
    public StockImportFormatDTO updateOwn(String username, StockImportFormatDTO dto) {
        validate(dto);
        StockImportFormat format = getOrCreate(compagnieDe(username));
        format.setColonnes(new ArrayList<>(dto.getColonnes()));
        return toDTO(stockImportFormatRepository.save(format));
    }

    private Compagnie compagnieDe(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        return personne.getCompagnie();
    }

    private StockImportFormat getOrCreate(Compagnie compagnie) {
        return stockImportFormatRepository.findByCompagnie_Id(compagnie.getId())
                .orElseGet(() -> stockImportFormatRepository.save(new StockImportFormat(compagnie)));
    }

    /**
     * REFERENCE et QUANTITE sont fonctionnellement obligatoires (voir
     * ChampImportStock) - sans elles, un fichier genere depuis ce format ne
     * permettrait ni de retrouver le produit, ni de savoir quelle quantite
     * appliquer. BOUTIQUE est optionnelle mais ne peut apparaitre qu'une
     * fois (sinon une ligne aurait plusieurs boutiques cibles).
     */
    private void validate(StockImportFormatDTO dto) {
        List<ChampImportStock> colonnes = dto.getColonnes();
        if (colonnes == null || colonnes.isEmpty()) {
            throw new BadRequestException("Le format doit contenir au moins une colonne");
        }
        for (ChampImportStock champ : ChampImportStock.values()) {
            long occurrences = colonnes.stream().filter(c -> c == champ).count();
            if (occurrences > 1) {
                throw new BadRequestException("La colonne " + champ + " ne peut apparaitre qu'une seule fois");
            }
        }
        if (!colonnes.contains(ChampImportStock.REFERENCE)) {
            throw new BadRequestException("Le format doit inclure la colonne Reference (elle identifie le produit)");
        }
        if (!colonnes.contains(ChampImportStock.QUANTITE)) {
            throw new BadRequestException("Le format doit inclure la colonne Quantite");
        }
    }

    private StockImportFormatDTO toDTO(StockImportFormat format) {
        StockImportFormatDTO dto = new StockImportFormatDTO();
        dto.setColonnes(new ArrayList<>(format.getColonnes()));
        return dto;
    }
}
