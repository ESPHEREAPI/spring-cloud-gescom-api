/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.NotificationClient;
import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface NotificationClientRepository extends JpaRepository<NotificationClient, Long>{
    
    @Query("SELECT n FROM NotificationClient n  WHERE n.client.id= :clientId")
    List<NotificationClient>findByClientId(@Param("clientId")Long clientId);
    
}
