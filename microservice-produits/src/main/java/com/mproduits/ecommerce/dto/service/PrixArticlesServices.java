/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.PrixarticlesDTO;
import java.util.List;



/**
 *
 * @author USER01
 */
public interface PrixArticlesServices {

    List<PrixarticlesDTO> listeProduits();

    public List<PrixarticlesDTO> searchProduitByLibelle(String keyword);

    public List<PrixarticlesDTO> searchProduitByCategorie(long idCategorie);

    public List<CategoriesDTO> listeCategorieHaveProduit();


}
