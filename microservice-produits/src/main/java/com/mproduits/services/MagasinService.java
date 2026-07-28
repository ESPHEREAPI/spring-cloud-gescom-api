/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;


import com.mproduits.model.Magasin;
import com.mproduits.repositories.MagasinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MagasinService {
    
    private final MagasinRepository magasinRepository;
    
    public Page<Magasin> findAll(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return magasinRepository.findBySearch(search, pageable);
        }
        return magasinRepository.findAll(pageable);
    }
    
    public Optional<Magasin> findById(Long id) {
        return magasinRepository.findById(id);
    }
    
    public Magasin save(Magasin magasin) {
        return magasinRepository.save(magasin);
    }
    
    public Magasin update(Long id, Magasin magasin) {
        if (!magasinRepository.existsById(id)) {
            throw new RuntimeException("Magasin not found with id: " + id);
        }
        magasin.setId(id);
        return magasinRepository.save(magasin);
    }
    
    public void deleteById(Long id) {
        if (!magasinRepository.existsById(id)) {
            throw new RuntimeException("Magasin not found with id: " + id);
        }
        magasinRepository.deleteById(id);
    }
}
    

