package com.mproduits.dto;

/**
 * Projection pour une valeur de specification d'un produit - jamais
 * l'entite Specificationarticles brute, qui serialiserait tout le
 * Produit lie (meme reflexe de securite/performance que partout ailleurs
 * dans ce codebase face aux entites lourdes).
 */
public record SpecificationArticleDTO(Long id, Long produitId, Long specifiqueId, String specifiqueLibelle, String valeur) {
}
