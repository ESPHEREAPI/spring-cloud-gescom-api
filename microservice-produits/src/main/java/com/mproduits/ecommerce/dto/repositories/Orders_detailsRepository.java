/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.repositories;

import com.mproduits.ecommerce.dto.entites.Orders_details;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 *
 * @author USER01
 */
@Repository
public interface Orders_detailsRepository  extends JpaRepository<Orders_details, Long>{
    @Query("SELECT od FROM Orders_details od WHERE od.orders.numero= :nc")
      public List<Orders_details> listeDetailsOrders(@Param(value ="nc") String  numero);
    
}
