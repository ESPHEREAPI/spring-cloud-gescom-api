/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.NotificationClient;
import java.util.List;

/**
 *
 * @author USER01
 */
public interface NotificationClientService {
     NotificationClient envoyer(NotificationClient notification);
    List<NotificationClient> findByClientId(Long clientId);
}
