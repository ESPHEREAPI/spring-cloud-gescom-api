package sid.service_admin.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.CompagnieDTO;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateCompagnieResultDTO;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.dto.UpdateCompagnieDTO;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.exceptions.ConflictException;
import sid.service_admin.exceptions.ResourceNotFoundException;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.PersonneRepository;

/**
 * Creation/gestion des compagnies. La creation d'une compagnie cree aussi
 * son administrateur compagnie, dans la meme transaction (pas de compagnie
 * orpheline sans admin).
 */
@Service
public class CompagnieService {

    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final AdminAccountService adminAccountService;
    private final AuditLogService auditLogService;

    public CompagnieService(CompagnieRepository compagnieRepository, PersonneRepository personneRepository,
            AdminAccountService adminAccountService, AuditLogService auditLogService) {
        this.compagnieRepository = compagnieRepository;
        this.personneRepository = personneRepository;
        this.adminAccountService = adminAccountService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CreateCompagnieResultDTO createCompagnieWithAdmin(CreateCompagnieDTO dto, String createdBy) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BadRequestException("Le nom de la compagnie est obligatoire");
        }
        if (compagnieRepository.existsByNom(dto.getNom())) {
            throw new ConflictException("Une compagnie avec ce nom existe deja : " + dto.getNom());
        }

        Compagnie compagnie = new Compagnie(dto.getNom(), dto.getTypeCommerce());
        compagnie.setAdresse(dto.getAdresse());
        compagnie.setTel(dto.getTel());
        compagnie.setEmail(dto.getEmail());
        compagnie.setCreatedBy(createdBy);
        Compagnie saved = compagnieRepository.save(compagnie);

        // Cree l'admin compagnie dans la meme transaction : si ca echoue, la
        // compagnie ne doit pas rester orpheline sans administrateur.
        AccountCreationResult adminResult = adminAccountService.createCompanyAdmin(saved, dto, createdBy);

        auditLogService.log("COMPAGNIE_CREEE", "Compagnie", saved.getId(), "Compagnie creee : " + saved.getNom());

        CreateCompagnieResultDTO result = new CreateCompagnieResultDTO();
        result.setCompagnie(toDTO(saved));
        result.setAdmin(adminResult.getUser());
        result.setGeneratedAdminPassword(adminResult.getGeneratedPassword());
        return result;
    }

    @Transactional(readOnly = true)
    public List<CompagnieDTO> listAll() {
        return compagnieRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompagnieDTO getById(Long id) {
        return toDTO(compagnieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + id)));
    }

    @Transactional(readOnly = true)
    public CompagnieDTO getOwn(String username) {
        Personne personne = personneRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + username));
        if (personne.getCompagnie() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'est rattache a aucune compagnie");
        }
        return toDTO(personne.getCompagnie());
    }

    @Transactional
    public CompagnieDTO update(Long id, UpdateCompagnieDTO dto) {
        Compagnie compagnie = compagnieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compagnie non trouvee : " + id));

        if (dto.getNom() != null) {
            compagnie.setNom(dto.getNom());
        }
        if (dto.getTypeCommerce() != null) {
            compagnie.setTypeCommerce(dto.getTypeCommerce());
        }
        if (dto.getActif() != null) {
            compagnie.setActif(dto.getActif());
        }
        if (dto.getAdresse() != null) {
            compagnie.setAdresse(dto.getAdresse());
        }
        if (dto.getTel() != null) {
            compagnie.setTel(dto.getTel());
        }
        if (dto.getEmail() != null) {
            compagnie.setEmail(dto.getEmail());
        }

        return toDTO(compagnieRepository.save(compagnie));
    }

    private CompagnieDTO toDTO(Compagnie compagnie) {
        CompagnieDTO dto = new CompagnieDTO();
        BeanUtils.copyProperties(compagnie, dto);
        return dto;
    }
}
