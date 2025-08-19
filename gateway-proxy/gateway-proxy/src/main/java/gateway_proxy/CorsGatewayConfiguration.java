/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gateway_proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * @author USER01
 */
@Configuration
public class CorsGatewayConfiguration {
//     @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//        
//        // Définir une seule origine, pas plusieurs
//        corsConfig.addAllowedOrigin("http://localhost:4200");
//        corsConfig.addAllowedOrigin("http://localhost:8080");
//        
//        // Pour permettre les cookies si nécessaire
//        corsConfig.setAllowCredentials(true);
//        
//        // Autres configurations
//        corsConfig.addAllowedHeader("*");
//        corsConfig.addAllowedMethod("*");
//        
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//        
//        return new CorsWebFilter( source);
//    }
//    

//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        // Permettre spécifiquement l'origine qui génère l'erreur
//        corsConfig.addAllowedOrigin("http://easycom-app");
//        corsConfig.addAllowedOrigin("http://easycom-app:80");
//        corsConfig.addAllowedOrigin("http://localhost:3000");
//        corsConfig.addAllowedOrigin("http://localhost:8080");
//        corsConfig.addAllowedOrigin("http://localhost:4200");
//        //corsConfig.addAllowedOrigin("http://localhost:9002");
//
//        // Pour permettre les cookies - IMPORTANT pour les requêtes authentifiées
//        corsConfig.setAllowCredentials(true);
//
//        // Configurer les headers autorisés - nécessaire pour les requêtes préliminaires (preflight)
//        corsConfig.addAllowedHeader("*");
//        corsConfig.addAllowedHeader("Authorization");
//        corsConfig.addAllowedHeader("Content-Type");
//        corsConfig.addAllowedHeader("Accept");
//        // Ajoutez ces headers exposés dans votre configuration CORS
//
//        corsConfig.addExposedHeader("Content-Length");
//        corsConfig.addExposedHeader("Date");
//        corsConfig.addExposedHeader("Server");
//
//        // Configurer les méthodes autorisées
//        corsConfig.addAllowedMethod("*");
//
//        // Exposer les headers pour permettre l'accès côté client
//        corsConfig.addExposedHeader("Authorization");
//        corsConfig.addExposedHeader("Access-Control-Allow-Origin");
//
//        // Configurer le temps d'expiration du cache de pré-vérification en secondes
//        corsConfig.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return new CorsWebFilter(source);
//    }

//     @Bean
//     @Order(-1)
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//        
//        // Configuration simplifiée et corrigée
//        corsConfig.addAllowedOriginPattern("http://localhost:*"); // Permet tous les ports localhost
//        corsConfig.addAllowedOriginPattern("http://easycom-app*"); // Permet vos apps Docker
//        
//        // OU si vous préférez être plus spécifique :
//        // corsConfig.addAllowedOrigin("http://localhost:4200");
//        // corsConfig.addAllowedOrigin("http://localhost:3000");
//        // corsConfig.addAllowedOrigin("http://localhost:8080");
//        // corsConfig.addAllowedOrigin("http://easycom-app");
//        // corsConfig.addAllowedOrigin("http://easycom-app:80");
//        
//        // Credentials - nécessaire pour les requêtes authentifiées
//        corsConfig.setAllowCredentials(true);
//        
//        // Headers autorisés
//        corsConfig.addAllowedHeader("*");
//        
//        // Méthodes autorisées
//        corsConfig.addAllowedMethod("*");
//        
//        // Headers exposés (optionnel)
//        corsConfig.addExposedHeader("Authorization");
//        corsConfig.addExposedHeader("Content-Type");
//        corsConfig.addExposedHeader("Accept");
//        
//        // Cache preflight
//        corsConfig.setMaxAge(3600L);
//        
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//        
//        return new CorsWebFilter(source);
//    }

 @Bean
    @Order(-1)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // ✅ SOLUTION 1: Utiliser addAllowedOriginPattern (recommandé)
        corsConfig.addAllowedOriginPattern("http://localhost:*");
        corsConfig.addAllowedOriginPattern("https://localhost:*");
        corsConfig.addAllowedOriginPattern("http://easycom-app*");
         corsConfig.addAllowedOriginPattern("http://192.168.123.*:*");
          corsConfig.addAllowedOriginPattern("http://192.168.1.*:*");
                  corsConfig.addAllowedOriginPattern("http://192.168.123.*");
                  corsConfig.addAllowedOriginPattern("http://192.168.1.*");
                   corsConfig.addAllowedOriginPattern("http://localhost:4200");
        // ✅ SOLUTION 2: Ou spécifier les origines exactes (plus sécurisé)
        // corsConfig.addAllowedOrigin("http://localhost:4200");
        // corsConfig.addAllowedOrigin("http://localhost:3000");
        // corsConfig.addAllowedOrigin("http://localhost:8080");
        
        // ✅ Credentials autorisés
        corsConfig.setAllowCredentials(true);
        
        // ✅ Headers autorisés - spécifiez les headers nécessaires
        corsConfig.addAllowedHeader("Authorization");
        corsConfig.addAllowedHeader("Content-Type");
        corsConfig.addAllowedHeader("Accept");
        corsConfig.addAllowedHeader("X-Requested-With");
        corsConfig.addAllowedHeader("Cache-Control");
        
        // ✅ Méthodes autorisées
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedMethod("DELETE");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedMethod("PATCH");
        
        // ✅ Headers exposés
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("Content-Type");
        
        // ✅ Cache preflight
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }

    // ✅ AJOUT IMPORTANT: Bean pour gérer les requêtes OPTIONS
    @Bean
    public WebFilter corsWebFilter2() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            
            if (CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                
                // Ajouter les headers CORS manuellement si nécessaire
                String origin = request.getHeaders().getOrigin();
                if (origin != null && (
                    origin.startsWith("http://localhost:") || 
                    origin.startsWith("https://localhost:") ||
                    origin.startsWith("http://easycom-app:")||
                    origin.startsWith("http://127.0.0.1:")   ||
                         origin.startsWith("http://192.168.123.")||
                        origin.startsWith("http://192.168.1.")||
                        origin.startsWith("http://localhost:4200")
                )) {
                    headers.add("Access-Control-Allow-Origin", origin);
                    headers.add("Access-Control-Allow-Credentials", "true");
                }
                
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
                    headers.add("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept,X-Requested-With,Cache-Control");
                    headers.add("Access-Control-Max-Age", "3600");
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            
            return chain.filter(ctx);
        };
    }
}


    

