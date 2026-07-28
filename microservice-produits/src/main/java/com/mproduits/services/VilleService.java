package com.mproduits.services;


import com.mproduits.model.Ville;
import com.mproduits.repositories.VilleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VilleService {
    
    private final VilleRepository villeRepository;
    
    public Page<Ville> findAll(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return villeRepository.findBySearch(search, pageable);
        }
        return villeRepository.findAll(pageable);
    }
    
    public List<Ville> findAll() {
        return villeRepository.findAll();
    }
    
    public Optional<Ville> findById(Long id) {
        return villeRepository.findById(id);
    }
    
    public Ville save(Ville ville) {
        return villeRepository.save(ville);
    }
    
    public Ville update(Long id, Ville ville) {
        if (!villeRepository.existsById(id)) {
            throw new RuntimeException("Ville not found with id: " + id);
        }
        ville.setId(id);
        return villeRepository.save(ville);
    }
    
    public void deleteById(Long id) {
        if (!villeRepository.existsById(id)) {
            throw new RuntimeException("Ville not found with id: " + id);
        }
        villeRepository.deleteById(id);
    }
}