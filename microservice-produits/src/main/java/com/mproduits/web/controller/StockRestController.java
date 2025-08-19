/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.StockUpdateRequest;
import com.mproduits.dto.StockUpdateResponse;
import com.mproduits.exceptions.InsufficientStockException;
import com.mproduits.services.StockService;
import com.mproduits.web.exceptions.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
public class StockRestController {
    
    @Autowired
    private StockService stockService;

    @PostMapping("/update-after-sale")
    @Transactional
    public ResponseEntity<?> updateStockAfterSale(@RequestBody StockUpdateRequest request) {
        try {
            StockUpdateResponse response = stockService.updateStockAfterSale(
                request.getProductId(), 
                request.getQuantity()
            );
            return ResponseEntity.ok(response);
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest()
                .body( new InsufficientStockException("Stock insuffisant pour l'article id : " + request.getProductId()));

        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
                //.body(new ErrorResponse("INTERNAL_ERROR Erreur lors de la mise à jour du stock") {
//                @Override
//                public HttpStatusCode getStatusCode() {
//                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//                }
//
//                @Override
//                public ProblemDetail getBody() {
//                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//                }
//            });
        }
    }

    @GetMapping("/check-availability/{productId}/{requestedQuantity}")
    public ResponseEntity<Boolean> checkStockAvailability(
            @PathVariable Long productId, 
            @PathVariable Integer quantity) {
        try {
            boolean available = stockService.isStockAvailable(productId, quantity);
            return ResponseEntity.ok(available);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/current/{productId}")
    public ResponseEntity<Integer> getCurrentStock(@PathVariable Long productId) {
        try {
            Integer currentStock = stockService.getCurrentStock(productId);
            return ResponseEntity.ok(currentStock);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

//    @PostMapping("/adjust")
//    @Transactional
//    public ResponseEntity<?> adjustStock(@RequestBody StockUpdateRequest request) {
//        try {
//            StockUpdateResponse response = stockService.adjustStock(
//                request.getProductId(), 
//                request.getQuantity(), 
//                request.getReason()
//            );
//            return ResponseEntity.ok(response);
//        } catch (ProductNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse("INTERNAL_ERROR Erreur lors de l'ajustement du stock"));
//        }
//    }

//    @PostMapping("/revert")
//    @Transactional
//    public ResponseEntity<?> revertStockUpdate(@RequestBody StockUpdateRequest request) {
//        try {
//            StockUpdateResponse response = stockService.revertStockUpdate(
//                request.getProductId(), 
//                request.getQuantity()
//            );
//            return ResponseEntity.ok(response);
//        } catch (ProductNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse("INTERNAL_ERROR  Erreur lors de l'annulation"));
//        }
//    }
}
