/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.model.NotificationClient;
import com.mproduits.services.NotificationClientService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits/notification")

public class NotificationClientController {

    private final NotificationClientService notificationService;
@Autowired
    public NotificationClientController(NotificationClientService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationClient> envoyer(@RequestBody NotificationClient notification) {
        return ResponseEntity.ok(notificationService.envoyer(notification));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<NotificationClient>> findByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(notificationService.findByClientId(clientId));
    }
}

