/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.DTO;

import com.mproduits.ecommerce.dto.entites.Articles;
import java.math.BigDecimal;
import lombok.Data;


/**
 *
 * @author USER01
 */
@Data
public class Orders_detailsDTO {

    private Articles product;
    BigDecimal price;
   BigDecimal quantite;

}
