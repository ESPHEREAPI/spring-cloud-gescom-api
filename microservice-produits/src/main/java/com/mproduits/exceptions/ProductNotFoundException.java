/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.exceptions;

/**
 *
 * @author USER01
 */

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Produit introuvable.");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
