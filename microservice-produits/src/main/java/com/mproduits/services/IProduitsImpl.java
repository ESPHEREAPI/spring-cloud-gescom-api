/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.ProduitDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Annee;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Produit;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.ProduitRepositories;
import jakarta.transaction.Transactional;
import java.awt.PointerInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author USER01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IProduitsImpl implements IProduits{
     private final ProduitRepositories produitRepository;
     private final PointVenteRepositories  pvRepositories;
     private final BoutiqueRepositories btRepositories;
     @Autowired
     MapperDtoImpl mapperDto;

    @Override
    public Page<ProduitDto> getAllProduits(Entreprise entreprise, Pageable pageable) {
         return produitRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public List<ProduitDto> searchByLibelleOrReference(String query, Entreprise entreprise) {
     return produitRepository.findAll()
        
                
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()); 
    
    }

    @Override
    public Produit findById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }

    @Override
    public ProduitDto findByReference(String reference) {
         Produit p= produitRepository.findByReference(reference)
                .orElse(null);
         return this.convertToDto(p);
    }

    @Override
    public Produit findByLibelle(String libelle) {
       return produitRepository.findByLibelle(libelle)
                .orElse(null); 
    }

    @Override
    public List<ProduitDto> getAllProduit() {
    return produitRepository.findAll().stream()
            .filter(pd-> pd.getDeletes()==false)
            .map(this::convertToDto)
            .collect(Collectors.toList()); 
    }

    @Override
   @Transactional
    public Produit save(Produit produit) {
   return produitRepository.save(produit);
    
    }
     private ProduitDto convertToDto(Produit produit) {
        return mapperDto.mapperProduitDto(produit,new Boutique());
    }

     @Override
    public boolean existsByReference(String reference) {
        if (!StringUtils.hasText(reference)) {
            return false;
        }
        return produitRepository.findByReference(reference.trim()).isPresent();
    }

    @Override
    public List<ProduitDto> getAllProduitHaveStock(int boutique,int annee) {
      long  boutiqueid=Long.parseLong(""+boutique);
        Boutique b=btRepositories.findById(boutiqueid).orElse(null);
        System.out.println("boutique.."+b);
      return pvRepositories.listeProduitHaveStockByBoutiqueAndAnnee(b!= null ? b.getId():null, annee,BigDecimal.ZERO).stream()
            .filter(pd-> pd.getDeletes()==false)
            .map(pds-> mapperDto.mapperProduitDto(pds,b))
            .collect(Collectors.toList()); 
    
    }
}
