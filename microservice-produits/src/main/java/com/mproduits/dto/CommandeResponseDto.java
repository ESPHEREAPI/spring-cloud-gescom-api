/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Commande;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class CommandeResponseDto {
    private boolean success;
    private String message;
    private Commande commande;
    private String error;
    
    public static CommandeResponseDto success(String message, Commande commande) {
        CommandeResponseDto response = new CommandeResponseDto();
        response.setSuccess(true);
        response.setMessage(message);
        response.setCommande(commande);
        return response;
    }
    
    public static CommandeResponseDto error(String error) {
        CommandeResponseDto response = new CommandeResponseDto();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}
