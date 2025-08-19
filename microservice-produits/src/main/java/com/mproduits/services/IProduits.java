/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Produit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author USER01
 */
public interface IProduits {

    public Page<ProduitDto> getAllProduits(Entreprise entreprise, Pageable pageable);

    public List<ProduitDto> searchByLibelleOrReference(String query, Entreprise entreprise);

    public Produit findById(Long id);

    public ProduitDto findByReference(String reference);

    public Produit findByLibelle(String libelle);

    public List<ProduitDto> getAllProduit();

    public Produit save(Produit produit);

  
}
