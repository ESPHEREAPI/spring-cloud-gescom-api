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
public class StockResponseDto {
    private List<StockItemDto> data;
    private int totalRecords;
    private boolean success;
    private String message;
}
