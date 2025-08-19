/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.DTO.OrdersDTO;

import com.mproduits.ecommerce.dto.entites.Orders;
import com.mproduits.ecommerce.dto.entites.Orders_details;
import java.util.List;


/**
 *
 * @author USER01
 */
public interface OrdersServices {
    public String getNumero_commande(int annee);
    public List<Orders_details> listeCommandes(Orders orders);
    public OrdersDTO addCommande(OrdersDTO ordersDTO);
    public List<OrdersDTO> listesOrdersByUsers(String user);
    
    
    
    
}
