/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.NotificationClient;
import com.mproduits.repositories.NotificationClientRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class NotificationClientServiceImpl implements NotificationClientService {
    private final NotificationClientRepository notificationRepository;

    public NotificationClientServiceImpl(NotificationClientRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationClient envoyer(NotificationClient notification) {
        // log ou appel API SMS/EMAIL ici
        return notificationRepository.save(notification);
    }

    @Override
    public List<NotificationClient> findByClientId(Long clientId) {
        return notificationRepository.findByClientId(clientId);
    }
}

