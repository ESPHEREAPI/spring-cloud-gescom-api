/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gateway_proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author USER01
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
     private static final Logger LOGGER = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        LOGGER.info("Request interceptée : {} {}", method, path);
        
        // Ajouter un header personnalisé
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Custom-Header", "gateway-proxy")
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    LOGGER.info("Réponse envoyée pour: {} {}", method, path);
                }));
    }

    @Override
    public int getOrder() {
        return -1; // Ordre élevé pour exécuter ce filtre parmi les premiers
    }
}
