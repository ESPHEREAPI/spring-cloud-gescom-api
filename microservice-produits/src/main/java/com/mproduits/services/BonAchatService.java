package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.BonAchat;
import com.mproduits.model.ClientBonAchat;
import com.mproduits.repositories.BonAchatRepositories;
import com.mproduits.repositories.ClientBonAchatRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BonAchatService {

    private final BonAchatRepositories bonAchatRepository;
    private final ClientBonAchatRepositories clientBonAchatRepository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<BonAchat> findAll() {
        return bonAchatRepository.findByClientBonAchat_Compagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<BonAchat> findById(Long id) {
        return bonAchatRepository.findByIdAndClientBonAchat_Compagnie_Id(id, compagnieCourante());
    }

    public BonAchat create(BonAchat bonAchat) {
        Long compagnieId = compagnieCourante();

        // Le client bon d'achat est cree/rattache a la compagnie courante -
        // jamais celui fourni tel quel par le client (qui pourrait pointer
        // vers un ClientBonAchat d'une autre compagnie).
        ClientBonAchat clientFourni = bonAchat.getClientBonAchat();
        if (clientFourni == null) {
            throw new BadRequestException("Client requis pour un bon d'achat");
        }
        ClientBonAchat client;
        if (clientFourni.getId() != null) {
            Long clientId = clientFourni.getId();
            client = clientBonAchatRepository.findByIdAndCompagnie_Id(clientId, compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Client non trouve avec l'ID: " + clientId));
        } else {
            clientFourni.setCompagnieId(compagnieId);
            client = clientBonAchatRepository.save(clientFourni);
        }

        bonAchat.setClientBonAchat(client);
        bonAchat.setId(null);
        if (bonAchat.getMontantUtilise() == null) {
            bonAchat.setMontantUtilise(java.math.BigDecimal.ZERO);
        }
        return bonAchatRepository.save(bonAchat);
    }

    public BonAchat update(Long id, BonAchat bonAchat) {
        BonAchat existant = bonAchatRepository.findByIdAndClientBonAchat_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Bon d'achat non trouve avec l'ID: " + id));

        existant.setCodeBon(bonAchat.getCodeBon());
        existant.setMontantTotal(bonAchat.getMontantTotal());
        existant.setMontantUtilise(bonAchat.getMontantUtilise());
        existant.setDateExpiration(bonAchat.getDateExpiration());
        existant.setActif(bonAchat.isActif());
        return bonAchatRepository.save(existant);
    }

    public void deleteById(Long id) {
        BonAchat existant = bonAchatRepository.findByIdAndClientBonAchat_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Bon d'achat non trouve avec l'ID: " + id));
        bonAchatRepository.deleteById(existant.getId());
    }
}
