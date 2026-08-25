/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
//    @ExceptionHandler(MetierException.class)
//    public ResponseEntity<Map<String, String>> handleMetierException(MetierException ex) {
//        log.error("Erreur métier: {}", ex.getMessage());
//        
//        Map<String, String> response = new HashMap<>();
//        response.put("success", "false");
//        response.put("message", ex.getMessage());
//        
//        return ResponseEntity.badRequest().body(response);
//    }
//    
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidationException(
//        MethodArgumentNotValidException ex) {
//        
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", false);
//        response.put("message", "Erreurs de validation");
//        response.put("errors", errors);
//        
//        return ResponseEntity.badRequest().body(response);
//    }
//    
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
//        log.error("Erreur serveur", ex);
//        
//        Map<String, String> response = new HashMap<>();
//        response.put("success", "false");
//        response.put("message", "Erreur serveur: " + ex.getMessage());
//        
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
    
     /**
     * ✅ CORRECTION CRITIQUE : Gestion spécifique de HttpMediaTypeNotAcceptableException
     * Cette erreur se produit quand Spring ne peut pas sérialiser la réponse
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex) {
        
        log.error("Erreur MediaType Not Acceptable: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Le serveur ne peut pas produire le format demandé");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        
        // ✅ IMPORTANT : Forcer le Content-Type à application/json
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), "", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDetails> handleConflictException(ConflictException ex) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), "", HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ArticlesIndisponiblesException.class)
    public ResponseEntity<Map<String, Object>> handleArticlesIndisponibles(ArticlesIndisponiblesException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("produitsIndisponibles", ex.getArticles());
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(com.mproduits.security.TenantScopeException.class)
    public ResponseEntity<ErrorDetails> handleTenantScopeException(com.mproduits.security.TenantScopeException ex) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), "", HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    /**
     * Gestion générique des autres exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        log.error("Erreur serveur", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Erreur serveur: " + ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
    
}
