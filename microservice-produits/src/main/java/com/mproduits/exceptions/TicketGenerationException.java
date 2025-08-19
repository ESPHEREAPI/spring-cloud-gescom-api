/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */
public class TicketGenerationException extends RuntimeException  {

    public TicketGenerationException(String message) {
        super(message);
    }

    public TicketGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
    
}
