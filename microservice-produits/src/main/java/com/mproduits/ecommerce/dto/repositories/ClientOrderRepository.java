/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.repositories;

import com.mproduits.ecommerce.dto.entites.ClientOrder;
import com.mproduits.ecommerce.dto.entites.Orders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 *
 * @author USER01
 */
public interface ClientOrderRepository extends JpaRepository<ClientOrder, Long>{
    @Query("SELECT o FROM Orders o WHERE o.clientOrder.usernane= :usernane")
    public List<Orders> listeOrdersByUsername(@Param(value ="usernane")String usernane);
    
}
