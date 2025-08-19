/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class ApiResponse<T> {
     private boolean success;
    private String message;
    private T data;
    private List<String> errors;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
