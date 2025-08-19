/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Quantité en stock insuffisante.");
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}