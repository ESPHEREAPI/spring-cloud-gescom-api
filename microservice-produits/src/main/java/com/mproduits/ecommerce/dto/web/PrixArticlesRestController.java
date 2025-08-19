/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.web;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.PrixarticlesDTO;
import com.mproduits.ecommerce.dto.service.PrixArticlesServices;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 *
 * @author USER01
 */
@RestController
//@CrossOrigin("*")
@RequestMapping("/microservice-produits/e-com")
public class PrixArticlesRestController {

    private PrixArticlesServices prixArticlesServices;

    public PrixArticlesRestController(PrixArticlesServices prixArticlesServices) {
        this.prixArticlesServices = prixArticlesServices;
    }

    @GetMapping("/produits")
    public List<PrixarticlesDTO> listeProduits() {
        return this.prixArticlesServices.listeProduits();
    }

    @GetMapping("/produits/search")
    List<PrixarticlesDTO> listeProduitsByLibelle(@RequestParam(name = "keyword", defaultValue = "") String keyword) {

        return this.prixArticlesServices.searchProduitByLibelle(keyword);
    }

    @GetMapping("/produits/categories/{id}")

    List<PrixarticlesDTO> listeProduitsByCategories(@PathVariable(name = "id") Long idcategorie) {
        return this.prixArticlesServices.searchProduitByCategorie(idcategorie);
    }

    @GetMapping("/produits/categories")
    List<CategoriesDTO> listecategories() {
        return this.prixArticlesServices.listeCategorieHaveProduit();
    }
  
}
