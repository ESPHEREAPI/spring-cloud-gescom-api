/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.VenteDto;
import com.mproduits.enums.StatutVente;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Entreprise;
import com.mproduits.model.LigneVente;
import com.mproduits.model.PointVente;
import com.mproduits.model.Vente;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.repositories.VenteSpecification;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
@RequiredArgsConstructor
public class VenteService {

    private final VenteRepositories venteRepository;
    private final LigneVenteRepositories ligneVenteRepositories;
    private final EntrepriseRepositories entrepriseRepositories;
    private final PointVenteRepositories pointVenteRepositories;
    @Autowired
    MapperDtoImpl mapperDtoImpl;

    public Page<VenteDto> getVentes(int page, int size, String statut, LocalDateTime dateDebut, LocalDateTime dateFin, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateVente").descending());
        Specification<Vente> spec = Specification.where(null);

        if (statut != null && !statut.isEmpty()) {
            spec = spec.and(VenteSpecification.hasStatut(StatutVente.valueOf(statut)));
        }

        if (dateDebut != null && dateFin != null) {
            try {
                //  Date debut = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
                // Date fin = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
                spec = spec.and(VenteSpecification.dateVenteBetween(dateDebut, dateFin));
            } catch (Exception e) {
                throw new RuntimeException("Format de date invalide. Attendu: yyyy-MM-dd");
            }
        }

        // Ajout du filtre de recherche
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(VenteSpecification.searchByNumeroTicketOrUser(search.trim()));
        }

        // ⚠️ page des entités Vente
        Page<Vente> ventePage = venteRepository.findAll(spec, pageable);

        // ✔️ conversion de contenu vers DTO
        List<VenteDto> venteDtos = ventePage.getContent().stream()
                //.filter(ve-> (ve.getVendeur().getUserName().equals(vendeur)==true))
                .map(v -> mapperDtoImpl.mapperVentByVenteDto(v))
                .collect(Collectors.toList());

        // ✔️ reconstruction de la page avec les DTOs
        return new PageImpl<>(venteDtos, pageable, ventePage.getTotalElements());
    }

    public VenteDto updateStatut(Long id, String username, String statut) {
        Vente vente = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée"));

        vente.setStatut(StatutVente.valueOf(statut));

        //gestion de restauration du stock si le statut est annuler
        if (statut == null ? StatutVente.ANNULEE.name() == null : statut.equals(StatutVente.ANNULEE.name())) {
            vente.setUsername_statut_annuler(username);
            restaurationStock(vente);
        }
        Vente saveVente = venteRepository.save(vente);
        System.out.println("satatut:" + saveVente.getStatut().toString());
        return this.mapperDtoImpl.mapperVentByVenteDto(saveVente);
    }

    private void restaurationStock(Vente vente) {
        List<LigneVente> lignesVentes = ligneVenteRepositories.findByVente(vente);
        PointVente pv;

        lignesVentes.forEach(lg
                -> {
            PointVente pvs = pointVenteRepositories.findLatestByProduitAndEntreprise(lg.getProduit(), vente.getEntreprise())
                    .orElseThrow(() -> new RuntimeException("pointe de vente non trouver"));
            BigDecimal sortie = pvs.getSortiProduit().subtract(lg.getQuantite());
           // BigDecimal entree = pvs.getEntreeProduit().add(lg.getQuantite());
            BigDecimal finalStock = pvs.getStockFinalTheorie().add(lg.getQuantite());
            pvs.setSortiProduit(sortie);
          //  pvs.setEntreeProduit(entree);
            pvs.setStockFinalTheorie(finalStock);
            pointVenteRepositories.save(pvs);

        }
        );

    }

    public VenteDto getVente(Long id) {
        Vente v = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée"));
        return this.mapperDtoImpl.mapperVentByVenteDto(v);
    }

    public VenteDto getVente(String numeTicket) {
        System.err.println("numero ticket:" + numeTicket);
        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);

        Optional<Vente> vente = venteRepository.findByEntrepriseAndNumeroTicket(e, numeTicket);

        //  .orElseThrow(() -> new EntityNotFoundException("Vente introuvable pour le ticket " + numeTicket));
        return vente.isEmpty() ? null : this.mapperDtoImpl.mapperVentByVenteDto(vente.get());

    }

    public VenteDto getVenteForECom(long numerocommande) {
        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        System.err.println("numero ticket:" + numerocommande);
        Vente vente = venteRepository.findByNumeroTicketAndStatutForCommande(numerocommande, StatutVente.EN_COURS, e.getAnnee().getId())
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable pour le ticket " + numerocommande));
        return vente == null ? null : this.mapperDtoImpl.mapperVentByVenteDto(vente);

    }
}
