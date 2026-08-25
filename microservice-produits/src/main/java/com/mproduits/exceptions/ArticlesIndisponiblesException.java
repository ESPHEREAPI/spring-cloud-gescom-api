package com.mproduits.exceptions;

import java.util.List;
import lombok.Getter;

/**
 * Levee par le checkout public (EcomCheckoutController) quand un ou
 * plusieurs articles du panier ne peuvent pas etre commandes (rupture de
 * stock ou produit retire de la boutique entre l'affichage du catalogue et
 * la confirmation) - voir GlobalExceptionHandler pour la reponse HTTP
 * structuree qui en decoule.
 */
@Getter
public class ArticlesIndisponiblesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<ArticleIndisponibleDTO> articles;

    public ArticlesIndisponiblesException(String message, List<ArticleIndisponibleDTO> articles) {
        super(message);
        this.articles = articles;
    }
}
