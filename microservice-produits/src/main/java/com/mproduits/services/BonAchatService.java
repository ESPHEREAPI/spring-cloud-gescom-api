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

import java.math.BigDecimal;
import java.util.Date;
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

        // Empeche de remettre a zero un bon deja consomme (double depense) ou de
        // gonfler artificiellement le credit disponible via un montant fictif.
        java.math.BigDecimal montantTotal = bonAchat.getMontantTotal();
        java.math.BigDecimal montantUtilise = bonAchat.getMontantUtilise();
        if (montantTotal == null || montantTotal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant total doit etre superieur a zero");
        }
        if (montantUtilise == null || montantUtilise.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant utilise ne peut pas etre negatif");
        }
        if (montantUtilise.compareTo(montantTotal) > 0) {
            throw new BadRequestException("Le montant utilise ne peut pas depasser le montant total");
        }
        // Un PUT generaliste ne doit jamais pouvoir FAIRE BAISSER la consommation
        // deja enregistree (ex: remettre a zero un bon deja utilise pour le
        // rendre a nouveau utilisable) - seule une hausse (nouvelle consommation)
        // ou une valeur inchangee est autorisee ici.
        if (montantUtilise.compareTo(existant.getMontantUtilise()) < 0) {
            throw new BadRequestException("Le montant utilise ne peut pas etre diminue via cette operation");
        }

        existant.setCodeBon(bonAchat.getCodeBon());
        existant.setMontantTotal(montantTotal);
        existant.setMontantUtilise(montantUtilise);
        existant.setDateExpiration(bonAchat.getDateExpiration());
        existant.setActif(bonAchat.isActif());
        return bonAchatRepository.save(existant);
    }

    // Consommation d'un bon d'achat comme moyen de paiement d'une vente -
    // distinct du PUT generique (update ci-dessus) qui ne fait que valider
    // un montantUtilise fourni par le client sans jamais garantir qu'il
    // correspond a une vraie transaction de paiement.
    public BonAchat consommer(String codeBon, BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant a consommer sur le bon d'achat doit etre superieur a zero");
        }
        BonAchat bon = bonAchatRepository.findByCodeBonAndClientBonAchat_Compagnie_Id(codeBon, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Bon d'achat introuvable: " + codeBon));

        if (!bon.isActif()) {
            throw new BadRequestException("Ce bon d'achat n'est plus actif");
        }
        if (bon.getDateExpiration() != null && bon.getDateExpiration().before(new Date())) {
            throw new BadRequestException("Ce bon d'achat a expire");
        }

        BigDecimal disponible = bon.getMontantTotal().subtract(bon.getMontantUtilise());
        if (montant.compareTo(disponible) > 0) {
            throw new BadRequestException("Solde insuffisant sur le bon d'achat " + codeBon
                    + " (disponible: " + disponible + ")");
        }

        bon.setMontantUtilise(bon.getMontantUtilise().add(montant));
        return bonAchatRepository.save(bon);
    }

    public void deleteById(Long id) {
        BonAchat existant = bonAchatRepository.findByIdAndClientBonAchat_Compagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Bon d'achat non trouve avec l'ID: " + id));
        bonAchatRepository.deleteById(existant.getId());
    }
}
