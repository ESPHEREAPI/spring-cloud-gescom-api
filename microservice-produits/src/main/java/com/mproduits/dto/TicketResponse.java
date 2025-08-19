/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
@Builder
public class TicketResponse {
     private String filePath;
    private String fileName;
    private Boolean success;
    private String message;
    private String ticketNumber;
    private LocalDateTime createdAt;
    
}
