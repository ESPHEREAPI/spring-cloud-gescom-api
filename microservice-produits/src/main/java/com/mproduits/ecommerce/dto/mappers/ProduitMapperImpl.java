/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.mappers;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;
import com.mproduits.ecommerce.dto.DTO.Orders_detailsDTO;

import com.mproduits.ecommerce.dto.PaniersDTO;
import com.mproduits.ecommerce.dto.PointVenteDTO;
import com.mproduits.ecommerce.dto.PrixarticlesDTO;
import com.mproduits.ecommerce.dto.ProduitDTO;
import com.mproduits.ecommerce.dto.entites.Articles;
import com.mproduits.ecommerce.dto.entites.Orders;
import com.mproduits.ecommerce.dto.entites.Orders_details;
import com.mproduits.ecommerce.dto.entites.Paniers;
import com.mproduits.ecommerce.dto.repositories.ArticlesRepository;
import com.mproduits.ecommerce.dto.repositories.Orders_detailsRepository;
import com.mproduits.model.Categories;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import java.io.Serializable;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


/**
 *
 * @author USER01
 */
@Service
@Data
@AllArgsConstructor
public class ProduitMapperImpl implements Serializable {

    /*
    convertion de entite vers dto
     */
    private Orders_detailsRepository orders_detailsRepository;
    private ArticlesRepository articlesRepository;

    public ProduitDTO formProduit(Produit produit) {
        ProduitDTO produitDTO = new ProduitDTO();
        BeanUtils.copyProperties(produit, produitDTO);
        return produitDTO;
    }
    
    public CategoriesDTO formCategories(Categories categories) {
        CategoriesDTO categoriesDTO = new CategoriesDTO();
        BeanUtils.copyProperties(categories, categoriesDTO);
        return categoriesDTO;
    }
    
    public PointVenteDTO formPointVente(PointVente pointvente) {
        PointVenteDTO pointventeDTO = new PointVenteDTO();
        ProduitDTO produitDTO = formProduit(pointvente.getProduit());
        BeanUtils.copyProperties(pointvente, pointventeDTO);
        pointventeDTO.setProduitDTO(produitDTO);
        return pointventeDTO;
    }
    
    public PrixarticlesDTO formPrixarticles(PrixArticles prixarticles) {
        PrixarticlesDTO prixarticlesDTO = new PrixarticlesDTO();
        ProduitDTO produitDTO = formProduit(prixarticles.getPointVente().getProduit());
        PointVenteDTO pointVenteDTO = formPointVente(prixarticles.getPointVente());
        pointVenteDTO.setProduitDTO(produitDTO);
        BeanUtils.copyProperties(prixarticles, prixarticlesDTO);
        prixarticlesDTO.setPointVenteDTO(pointVenteDTO);
        return prixarticlesDTO;
        
    }
    
    public PaniersDTO formPaniers(Paniers paniers) {
        PaniersDTO paniersDTO = new PaniersDTO();
        ProduitDTO produitDTO = formProduit(paniers.getPrixArticlesId().getPointVente().getProduit());
        
        PointVenteDTO pointVenteDTO = formPointVente(paniers.getPrixArticlesId().getPointVente());
        pointVenteDTO.setProduitDTO(produitDTO);
        PrixarticlesDTO prixarticlesDTO = formPrixarticles(paniers.getPrixArticlesId());
        prixarticlesDTO.setPointVenteDTO(pointVenteDTO);
        BeanUtils.copyProperties(paniers, paniersDTO);
        paniersDTO.setPrixarticlesDTO(prixarticlesDTO);
        return paniersDTO;
        
    }

    /*
    convertion de dto vers entite
     */
    public Produit formProduitDTO(ProduitDTO produitDTO) {
        Produit produit = new Produit();
        BeanUtils.copyProperties(produitDTO, produit);
        return produit;
    }
    
    public Categories formCategoriesDTO(CategoriesDTO categoriesDTO) {
        Categories categories = new Categories();
        BeanUtils.copyProperties(categoriesDTO, categories);
        return categories;
    }
    
    public PointVente formPointVenteDTO(PointVenteDTO pointventeDTO) {
        PointVente pointvente = new PointVente();
        Produit produit = formProduitDTO(pointventeDTO.getProduitDTO());
        BeanUtils.copyProperties(pointventeDTO, pointvente);
        pointvente.setProduit(produit);
        return pointvente;
    }
    
    public PrixArticles formPrixarticlesDTO(PrixarticlesDTO prixarticlesDTO) {
        PrixArticles prixarticles = new PrixArticles();
        Produit produit = formProduitDTO(prixarticlesDTO.getPointVenteDTO().getProduitDTO());
        
        PointVente pointvente = formPointVenteDTO(prixarticlesDTO.getPointVenteDTO());
        pointvente.setProduit(produit);
        BeanUtils.copyProperties(prixarticlesDTO, prixarticles);
        prixarticles.setPointVente(pointvente);
        return prixarticles;
        
    }
    
    public Paniers formPaniersDTO(PaniersDTO paniersDTO) {
        Paniers paniers = new Paniers();
        Produit produit = formProduitDTO(paniersDTO.getPrixarticlesDTO().getPointVenteDTO().getProduitDTO());
        
        PointVente pointVente = formPointVenteDTO(paniersDTO.getPrixarticlesDTO().getPointVenteDTO());
        pointVente.setProduit(produit);
        PrixArticles prixarticles = formPrixarticlesDTO(paniersDTO.getPrixarticlesDTO());
        
        prixarticles.setPointVente(pointVente);
        BeanUtils.copyProperties(paniersDTO, paniers);
        
        paniers.setPrixArticlesId(prixarticles);
        return paniers;
        
    }

    public OrdersDTO formOrders(Orders orders) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setId(Long.parseLong(orders.getNumero()));
        ordersDTO.setClient(orders.getClientOrder());
        ordersDTO.setTotalAmount(orders.getMontant());
        ordersDTO.setProducts(this.orders_detailsRepository.listeDetailsOrders(orders.getNumero()).stream()
                .map(od -> formOrders_details(od)).collect(Collectors.toList()));
        ordersDTO.setDate(orders.getDate_enregistrement());
        ordersDTO.setHeure(orders.getHeure());
        ordersDTO.setPayement(orders.getDate_payement());
        return ordersDTO;
        
    }

    public Orders_detailsDTO formOrders_details(Orders_details orders_details) {
        Orders_detailsDTO orders_detailsDTO = new Orders_detailsDTO();
        System.out.println(orders_details.getId_details());
        System.out.println(orders_details.getId_prix_articles());
        try {
            orders_detailsDTO.setProduct(articlesRepository.findById(orders_details.getId_prix_articles()).get());
        } catch (Exception e) {
            Articles articles=new Articles();
            articles.setActif(true);
            articles.setId(orders_details.getId_prix_articles());
            articles.setLibelle(orders_details.getLibelle());
            articles.setPrixUnitaire(orders_details.getPrix_Unitaire());
            articles.setQuantite(orders_details.getQuantite_caddy());
           orders_detailsDTO.setProduct(articles);
            
        }
        
        orders_detailsDTO.setPrice(orders_details.getPrix_Unitaire());
        orders_detailsDTO.setQuantite(orders_details.getQuantite_caddy());
        
        return orders_detailsDTO;
    }
    
      public Orders_details formOrders_detailsDTO(Orders_detailsDTO orders_detailsDTO,Orders orders) {
        Orders_details orders_details = new Orders_details();
        orders_details.setId_prix_articles(orders_detailsDTO.getProduct().getId());
        orders_details.setLibelle(orders_detailsDTO.getProduct().getLibelle());
        orders_details.setOrders(orders);
        orders_details.setQuantite_caddy(orders_detailsDTO.getQuantite());
        orders_details.setPrix_Unitaire(orders_detailsDTO.getPrice());
       
        
        return orders_details;
    }
    
}
