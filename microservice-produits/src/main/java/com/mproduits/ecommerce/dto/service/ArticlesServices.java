/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;

import com.mproduits.ecommerce.dto.entites.Articles;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;


/**
 *
 * @author USER01
 */
public interface ArticlesServices {

    List<Articles> listesArticles();

    public List<Articles> searchArticlesByReference(String keyword);

    public List<Articles> searchArticlesContainsReference(String keyword);

    public List<Articles> searchArticlesByCategorie(long idCategorie, String keyword);

    public List<Articles> searchArticlesByCategorie(long idCategorie);

    public OrdersDTO addOrders(OrdersDTO ordersDTO);
    



    public byte[] getPhoto(Long id) throws Exception;

    public Articles articleById(Long id);

    public void uploadPhoto(MultipartFile file, Long id);

    public CategoriesDTO categorieById(Long id);
}
