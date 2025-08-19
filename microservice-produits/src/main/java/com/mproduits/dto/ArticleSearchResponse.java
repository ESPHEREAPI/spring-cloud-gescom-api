/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;




/**
 *
 * @author USER01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
//@Schema(description = "Réponse de recherche paginée pour les articles")
public class ArticleSearchResponse {
    
    //@Schema(description = "Liste des articles pour la page courante")
    private List<ProduitDto> content;
    
   // @Schema(description = "Nombre total d'éléments", example = "10000")
    private Long totalElements;
    
    //@Schema(description = "Nombre total de pages", example = "500")
    private Integer totalPages;
    
    //@Schema(description = "Numéro de la page courante (commence à 0)", example = "0")
    private Integer page;
    
   // @Schema(description = "Taille de la page", example = "20")
    private Integer size;
    
    //@Schema(description = "Indique si c'est la première page", example = "true")
    private Boolean first;
    
   // @Schema(description = "Indique si c'est la dernière page", example = "false")
    private Boolean last;
    
    // Constructeurs
    public ArticleSearchResponse() {}
    
    public ArticleSearchResponse(List<ProduitDto> articles, Long totalElements, 
                               Integer totalPages, Integer currentPage, Integer pageSize,
                               Boolean first, Boolean last) {
        this.content = articles;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = currentPage;
        this.size = pageSize;
        this.first = first;
        this.last = last;
    }
    
}
