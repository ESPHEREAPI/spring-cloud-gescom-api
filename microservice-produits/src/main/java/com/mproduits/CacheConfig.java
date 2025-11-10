/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author USER01
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {
    /**
     * Configuration du gestionnaire de cache Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("Configuration du cache manager Caffeine");
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "produits",
            "topArticles",
            "articleAutocomplete",
            "categories",
            "prixProduits"
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        
        return cacheManager;
    }

    /**
     * Configuration Caffeine par défaut
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> 
                    log.debug("Cache éviction: key={}, cause={}", key, cause));
    }

    /**
     * Configuration spécifique pour l'autocomplétion (cache plus court)
     */
    @Bean("autocompleteCache")
    public Caffeine<Object, Object> autocompleteCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Configuration pour les catégories (cache plus long car changent rarement)
     */
    @Bean("categoriesCache")
    public Caffeine<Object, Object> categoriesCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Configuration pour les prix (cache court car peuvent changer)
     */
    @Bean("prixCache")
    public Caffeine<Object, Object> prixCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(500)
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .recordStats();
}
}