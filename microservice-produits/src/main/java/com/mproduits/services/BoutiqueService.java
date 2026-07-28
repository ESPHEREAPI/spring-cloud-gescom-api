package com.mproduits.services;


import com.mproduits.model.Boutique;
import com.mproduits.repositories.BoutiqueRepositories;
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
public class BoutiqueService {
    
    private final BoutiqueRepositories boutiqueRepository;
    
    public Page<Boutique> findAll(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return boutiqueRepository.findBySearch(search, pageable);
        }
        return boutiqueRepository.findAll(pageable);
    }
    
    public List<Boutique> findAll() {
        return boutiqueRepository.findAll();
    }
    
    public Optional<Boutique> findById(Long id) {
        return boutiqueRepository.findById(id);
    }
    
    public Boutique save(Boutique boutique) {
        return boutiqueRepository.save(boutique);
    }
    
    public Boutique update(Long id, Boutique boutique) {
        if (!boutiqueRepository.existsById(id)) {
            throw new RuntimeException("Boutique not found with id: " + id);
        }
        boutique.setId(id);
        return boutiqueRepository.save(boutique);
    }
    
    public void deleteById(Long id) {
        if (!boutiqueRepository.existsById(id)) {
            throw new RuntimeException("Boutique not found with id: " + id);
        }
        boutiqueRepository.deleteById(id);
    }
}