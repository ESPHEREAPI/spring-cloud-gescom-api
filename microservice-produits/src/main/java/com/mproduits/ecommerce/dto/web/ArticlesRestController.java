/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.web;


import com.mproduits.dto.VenteDto;
import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;

import com.mproduits.ecommerce.dto.entites.Articles;
import com.mproduits.ecommerce.dto.service.ArticlesServices;
import com.mproduits.ecommerce.dto.service.OrdersServices;
import com.mproduits.services.BarcodeService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 *
 * @author USER01
 */
@RestController
@AllArgsConstructor
//@CrossOrigin("*")
@RequestMapping("/microservice-produits")
public class ArticlesRestController {
    private  ArticlesServices articlesServices;
    private OrdersServices ordersServices;
    private BarcodeService barcodeService;
    
      @GetMapping("/e-com/articles")
    public List<Articles> listeProduits() {
        return this.articlesServices.listesArticles();
    }

    @GetMapping("/e-com/articles/search")
    List<Articles> listesArticlesByReference(@RequestParam(name = "keyword", defaultValue = "") String keyword) {

        return this.articlesServices.searchArticlesByReference(keyword);
    }
    
     @GetMapping("/e-com/articles/contains")
    List<Articles> listesArticlesContainsReference(@RequestParam(name = "keyword", defaultValue = "") String keyword) {

        return this.articlesServices.searchArticlesContainsReference(keyword);
    }

    @GetMapping("/e-com/articles/categories/{id}")

    List<Articles> listesArticlesByCategories(@PathVariable(name = "id") Long idcategorie) {
        return this.articlesServices.searchArticlesByCategorie(idcategorie);
    }
    
     @GetMapping("/e-com/articles/categories/search/{id}")

    List<Articles> listesArticlesByCategories(@PathVariable(name = "id") Long idcategorie,@RequestParam(name = "kw") String keyword ) {
        return this.articlesServices.searchArticlesByCategorie(idcategorie,keyword);
    }

   
    /*
    afficher la photot
    */
    @GetMapping(path ="/e-com/articles/photos/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[]getPhoto(@PathVariable(name = "id") Long id) throws Exception{
        return articlesServices.getPhoto(id);
    }
    
    @PostMapping("/e-com/articles/{id}/uploadPhoto")
     public void handleFileUpload( @RequestParam(name = "file") MultipartFile file,@PathVariable(name = "id")Long id) throws Exception{
    articlesServices.uploadPhoto(file, id);
        
    }
     @GetMapping("/e-com/articles/categorie/{id}")
     public CategoriesDTO categorieById(@PathVariable(name = "id")long  id){
         return articlesServices.categorieById(id);
     }
     @PostMapping("/e-com/articles/order")
     public OrdersDTO addOrders(@RequestBody OrdersDTO ordersDTO){
         
     return articlesServices.addOrders(ordersDTO);
     }
     @GetMapping("/e-com/articles/order/{username}")
     public List<OrdersDTO>listesOrderByUsername(@PathVariable(name = "username") String username){
         return ordersServices.listesOrdersByUsers(username);
     }
      @PostMapping("/vente/e-com/order/{numerocommande}")
     public ResponseEntity<Long> addOrders(@PathVariable(name = "numerocommande") long numerocommande,@RequestBody VenteDto  venteDto){
         long vendid=barcodeService.valideVente(venteDto, numerocommande,venteDto.getBoutiqueid());
          return ResponseEntity.ok(vendid);
     }
         
}
