/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.exceptions;

import lombok.Data;

/**
 *
 * @author USER01
 */
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor @NoArgsConstructor
public class ErrorDetails {
     private LocalDateTime timestamp;
    private String message;
    private String details;
    private int status;
    
}
