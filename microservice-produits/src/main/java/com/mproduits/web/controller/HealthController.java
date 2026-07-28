package com.mproduits.web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/microservice-produits")
public class HealthController {
    
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setMessage("Microservice Produits is running");
        response.setTimestamp(LocalDateTime.now().toString()); // ✅ En String pour éviter les problèmes
        response.setService("microservice-produits");
        
        return ResponseEntity.ok(response);
    }
}

// DTO Simple
class HealthResponse {
    private String status;
    private String message;
    private String timestamp;
    private String service;
    
    // Getters et Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
}