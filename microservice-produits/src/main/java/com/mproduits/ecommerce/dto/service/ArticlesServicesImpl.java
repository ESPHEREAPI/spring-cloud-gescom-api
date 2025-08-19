/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;

import com.mproduits.ecommerce.dto.entites.Articles;
import com.mproduits.ecommerce.dto.mappers.ProduitMapperImpl;
import com.mproduits.ecommerce.dto.repositories.ArticlesRepository;
import com.mproduits.ecommerce.dto.repositories.CategoriesRepository;
import com.mproduits.ecommerce.dto.repositories.PhotoRepository;
import com.mproduits.model.Categories;
import com.mproduits.model.Photo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 *
 * @author USER01
 */
@Service

@Transactional

@Slf4j
//@CrossOrigin("*")
public class ArticlesServicesImpl implements ArticlesServices {

    private ArticlesRepository articlesRepository;

    private PhotoRepository photoRepository;
    private CategoriesRepository categoriesRepository;
    private ProduitMapperImpl dtoMapper;
    private OrdersServicesImpl ordersServices;

    public ArticlesServicesImpl(ArticlesRepository articlesRepository, PhotoRepository photoRepository,
            CategoriesRepository categoriesRepository, ProduitMapperImpl dtoMapper, OrdersServicesImpl ordersServices) {
        this.articlesRepository = articlesRepository;
        //this.societesRepository = societesRepository;
        this.photoRepository = photoRepository;
        this.categoriesRepository = categoriesRepository;
        this.dtoMapper = dtoMapper;
        this.ordersServices = ordersServices;
    }

    @Override
    public List<Articles> listesArticles() {
        return articlesRepository.findAll().subList(0, 5);
        //return articlesRepository.allArticles();
    }

    @Override
    public List<Articles> searchArticlesByReference(String keyword) {

        return articlesRepository.searchArticles(keyword);
    }

    @Override
    public List<Articles> searchArticlesByCategorie(long idCategorie) {
        return articlesRepository.searchArticlesBycategories(idCategorie);
    }

 

    @Override
    public List<Articles> searchArticlesContainsReference(String keyword) {
        return articlesRepository.findByReferenceContains(keyword);
    }

    @Override
    public byte[] getPhoto(Long id) throws Exception {
    Optional<Articles> optionalArticle = articlesRepository.findById(id);
    if (optionalArticle.isEmpty()) {
        return getFallbackImage(); // si l’article n'existe pas
    }

    Articles pr = optionalArticle.get();

    // Supposons que pr.getProduit() retourne un identifiant (BigInteger ou autre)
    Photo photo = photoRepository.findByArticle(pr.getId());
    String photoName = (photo != null) ? photo.getPhotoName() : null;

    String basePath = System.getProperty("user.home") + "/cloud-config-api/bookshop_photo/articles/";
    Path photoPath = Paths.get(basePath + (photoName != null ? photoName : "unknown.png"));

    if (!Files.exists(photoPath)) {
        return getFallbackImage();
    }

    return Files.readAllBytes(photoPath);
}

private byte[] getFallbackImage() throws IOException {
    Path fallbackPath = Paths.get(System.getProperty("user.home") + "/cloud-config-api/bookshop_photo/articles/unknown.png");

    if (Files.exists(fallbackPath)) {
        return Files.readAllBytes(fallbackPath);
    }

    // Si l'image par défaut n'existe pas non plus, retourne tableau vide ou une exception
    throw new FileNotFoundException("Image inconnue non trouvée à l'emplacement prévu : " + fallbackPath);
}
    @Override
    public Articles articleById(Long id) {
        return articlesRepository.findById(id).get();
    }

    @Override
    public void uploadPhoto(MultipartFile file, Long id) {
        try {
            String ext = file.getContentType().split("\\/")[1];
            System.out.println("extension ficihier" + ext);
            Articles prod = articlesRepository.findById(id).get();
            if (prod == null) {
                return;
            }
            Photo ph = photoRepository.findByArticle(prod.getId());
            if (ph == null) {
                ph = new Photo();
                ph.setPhotoName(id + "." + ext);
                ph.setArticle((id));
            } else {
                ph.setPhotoName(id + "." + ext);
                ph.setArticle((id));
            }

            Files.write(Paths.get(System.getProperty("user.home") + "/cloud-config-api/bookshop_photo/articles/" + ph.getPhotoName()), file.getBytes());
            photoRepository.save(ph);
        } catch (IOException ex) {
            Logger.getLogger(ArticlesServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public CategoriesDTO categorieById(Long id) {
        Categories cat = categoriesRepository.findById(id).get();
        CategoriesDTO categoriesDTO = dtoMapper.formCategories(cat);
        return categoriesDTO;
    }

    @Override
    public List<Articles> searchArticlesByCategorie(long idCategorie, String keyword) {
        if (idCategorie == 0L) {
            return articlesRepository.searchArticles(keyword);
        }
        return articlesRepository.searchArticlesBycategories(idCategorie, keyword);
    }

    @Override
    public OrdersDTO addOrders(OrdersDTO ordersDTO) {
        return ordersServices.addCommande(ordersDTO);
    }

}
