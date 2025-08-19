///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package gateway_proxy;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// *
// * @author USER01
// */
//@Configuration
//public class GatewayConfig {
//    
////    @Bean
////    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
////        return builder.routes()
////                .route("service1", r -> r
////                        .path("/gateway-proxy/microservice-produits/**")
////                        .filters(f -> f
////                                .stripPrefix(2)
////                                .addRequestHeader("X-Source", "api-gateway"))
////                        .uri("http://localhost:9001"))
////                .route("service2", r -> r
////                        .path("/gateway-proxy/suivi/**")
////                        .filters(f -> f
////                                .stripPrefix(2)
////                                .addRequestHeader("X-Source", "api-gateway"))
////                        .uri("http://localhost:9006"))
////                .route("service3", r -> r
////                        .path("/gateway-proxy/api/**")
////                        .filters(f -> f
////                                .stripPrefix(2)
////                                .addRequestHeader("X-Source", "api-gateway"))
////                        .uri("http://localhost:9002"))
////                .build();
////    }
////    
//    
//     @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("service1", r -> r
//                        .path("/gateway-proxy/microservice-produits/**")
//                        .filters(f -> f
//                                .stripPrefix(2)
//                                .addRequestHeader("X-Source", "api-gateway"))
//                        .uri("http://localhost:9001"))
//                .route("service2", r -> r
//                        .path("/gateway-proxy/suivi/**")
//                        .filters(f -> f
//                                .stripPrefix(2)
//                                .addRequestHeader("X-Source", "api-gateway"))
//                        .uri("http://localhost:9006"))
//                .route("service3", r -> r
//                        .path("/gateway-proxy/api/**")
//                        .filters(f -> f
//                                .stripPrefix(2)
//                                .addRequestHeader("X-Source", "api-gateway"))
//                        .uri("http://localhost:9002"))
//                .build();
//    }
//}
