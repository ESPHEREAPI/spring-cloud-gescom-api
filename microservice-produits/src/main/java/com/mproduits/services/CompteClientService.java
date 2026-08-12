package com.mproduits.services;

import com.mproduits.dto.ClientSoldeDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Solde client consolide pour le recouvrement : combien chaque client doit
 * encore (toutes factures confondues), et combien la compagnie attend au
 * total. Distinct de la simple liste de versements que l'ancien ecran
 * Compte Client affichait.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompteClientService {

    private final FactureRepository factureRepository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    private ClientSoldeDTO toDto(Object[] row) {
        return ClientSoldeDTO.builder()
                .clientId((Long) row[0])
                .nom((String) row[1])
                .code((String) row[2])
                .telephone((String) row[3])
                .totalFacture((BigDecimal) row[4])
                .totalPaye((BigDecimal) row[5])
                .soldeRestant((BigDecimal) row[6])
                .nombreFactures((Long) row[7])
                .build();
    }

    /**
     * Soldes de tous les clients de la compagnie courante, du plus gros
     * reste-a-payer au plus petit ("clients a haute redevance").
     */
    public List<ClientSoldeDTO> getSoldesClients() {
        return factureRepository.aggregerSoldesParClient(compagnieCourante()).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Solde d'un client precis. null si le client n'a aucune facture dans
     * cette compagnie (rien a devoir).
     */
    public ClientSoldeDTO getSoldeClient(Long clientId) {
        List<Object[]> rows = factureRepository.aggregerSoldeParClient(clientId, compagnieCourante());
        return rows.isEmpty() ? null : toDto(rows.get(0));
    }

    /**
     * Montant total que la compagnie attend, tous clients confondus.
     */
    public BigDecimal getTotalAttendu() {
        return getSoldesClients().stream()
                .map(ClientSoldeDTO::getSoldeRestant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
