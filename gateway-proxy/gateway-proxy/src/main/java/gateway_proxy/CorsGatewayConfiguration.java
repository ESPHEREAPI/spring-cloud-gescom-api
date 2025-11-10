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
    
    @Bean
    @Order(-1)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // ✅ CORRECTION PRINCIPALE : Ajouter les origines HTTPS
        corsConfig.addAllowedOriginPattern("http://localhost:*");
        corsConfig.addAllowedOriginPattern("https://localhost:*");
        corsConfig.addAllowedOriginPattern("http://easycom-app*");
        corsConfig.addAllowedOriginPattern("http://192.168.123.*:*");
        corsConfig.addAllowedOriginPattern("http://192.168.1.*:*");
        
        // ✅ SOLUTION PRINCIPALE : Ajouter les origines HTTPS pour votre IP
        corsConfig.addAllowedOrigin("http://77.68.94.193");
        corsConfig.addAllowedOrigin("https://77.68.94.193");  // ⭐ CRITIQUE : Cette ligne manquait
        corsConfig.addAllowedOriginPattern("http://77.68.94.*:*");
        corsConfig.addAllowedOriginPattern("https://77.68.94.*:*");  // ⭐ CRITIQUE : Cette ligne manquait
        
        // ✅ Credentials autorisés
        corsConfig.setAllowCredentials(true);

        // ✅ Headers autorisés
        corsConfig.addAllowedHeader("*");

        // ✅ Méthodes autorisées
        corsConfig.addAllowedMethod("*");

        // ✅ Headers exposés
        corsConfig.addExposedHeader("*");

        // ✅ Cache preflight
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    // ✅ CORRECTION du WebFilter pour gérer HTTPS
    @Bean
    public WebFilter corsWebFilter2() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            ServerHttpResponse response = ctx.getResponse();
            HttpHeaders headers = response.getHeaders();

            // Récupérer l'origine de la requête
            String origin = request.getHeaders().getOrigin();
            
            // ✅ CORRECTION: Vérifier toutes les origines possibles INCLUDING HTTPS
            boolean isAllowedOrigin = origin != null && (
                origin.startsWith("http://localhost:") ||
                origin.startsWith("https://localhost:") ||
                origin.startsWith("http://easycom-app") ||
                origin.startsWith("http://127.0.0.1:") ||
                origin.startsWith("https://127.0.0.1:") ||
                origin.startsWith("http://192.168.123.") ||
                origin.startsWith("http://192.168.1.") ||
                origin.equals("http://77.68.94.193") ||
                origin.equals("https://77.68.94.193") ||   // ⭐ CRITIQUE : Cette ligne manquait
                origin.startsWith("http://77.68.94.") ||
                origin.startsWith("https://77.68.94.")     // ⭐ CRITIQUE : Cette ligne manquait
            );

            if (isAllowedOrigin) {
                headers.add("Access-Control-Allow-Origin", origin);
                headers.add("Access-Control-Allow-Credentials", "true");
                headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD");
                headers.add("Access-Control-Allow-Headers", "*");
                headers.add("Access-Control-Expose-Headers", "*");
                headers.add("Access-Control-Max-Age", "3600");
            }

            // Gérer les requêtes OPTIONS (preflight)
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            return chain.filter(ctx);
        };
    }
}