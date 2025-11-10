/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */
public class MetierException extends RuntimeException {
    
    public MetierException(String message) {
        super(message);
    }
    
    public MetierException(String message, Throwable cause) {
        super(message, cause);
    }
}
