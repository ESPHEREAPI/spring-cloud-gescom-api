/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.DTO;

import com.mproduits.ecommerce.dto.entites.ClientOrder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;


/**
 *
 * @author USER01
 */
@Data
public class OrdersDTO {

    long id;
    ClientOrder client;
    List<Orders_detailsDTO> products = new ArrayList<>();
    BigDecimal totalAmount;
    Date date;
    Date heure;
    Date payement;
    int annee_id;
    String statut;

}
