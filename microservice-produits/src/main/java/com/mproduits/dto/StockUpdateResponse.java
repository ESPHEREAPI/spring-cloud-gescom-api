/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Builder
@Data

public class StockUpdateResponse {
    private boolean success;
    private String message;
    private Integer newStock;
    private Long productId;
}
