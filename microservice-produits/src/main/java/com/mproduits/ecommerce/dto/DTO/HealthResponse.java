/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.DTO;
 import java.time.LocalDateTime;
/**
 *
 * @author USER01
 */
// Classe DTO pour la réponse
public class HealthResponse {
    private String status;
    private String message;
    private LocalDateTime timestamp;
    
    // Constructeur, getters, setters
    public HealthResponse(String status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}
