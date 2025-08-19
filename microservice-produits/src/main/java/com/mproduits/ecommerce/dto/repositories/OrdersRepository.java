/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.repositories;

import com.mproduits.ecommerce.dto.entites.Orders;
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
public interface OrdersRepository extends JpaRepository<Orders, Long>{
    @Query("SELECT (o.numero) FROM Orders o ")
    public long nbrRow();
    public Orders findByNumero(String numero);
     @Query("SELECT od FROM Orders od WHERE od.clientOrder.usernane= :username")
      public List<Orders> listeDetailsOrders(@Param(value ="username") String  username);
   
    
}
