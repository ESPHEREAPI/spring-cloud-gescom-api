/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */
public class ErrorResponse extends RuntimeException {
     public ErrorResponse() {
        super("Quantité en stock insuffisante.");
    }

    public ErrorResponse(String message) {
        super(message);
    }
}
