package com.mproduits.services;


import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Typeclient;
import com.mproduits.repositories.TypeClientRepository;
import com.mproduits.security.TenantContext;
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
public class TypeClientService {

    private final TypeClientRepository typeClientRepository;
    private final TenantContext tenantContext;

    /** La compagnie n'est jamais fournie par le client - toujours derivee du token JWT de l'appelant. */
    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public Page<Typeclient> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return typeClientRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return typeClientRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Typeclient> findAll() {
        return typeClientRepository.findByCompagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Typeclient> findById(Long id) {
        return typeClientRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Typeclient save(Typeclient typeClient) {
        typeClient.setCompagnieId(compagnieCourante());
        return typeClientRepository.save(typeClient);
    }

    public Typeclient update(Long id, Typeclient typeClient) {
        Typeclient existant = typeClientRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Type client not found with id: " + id));
        typeClient.setId(id);
        typeClient.setCompagnie(existant.getCompagnie());
        return typeClientRepository.save(typeClient);
    }

    public void deleteById(Long id) {
        Typeclient existant = typeClientRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Type client not found with id: " + id));
        typeClientRepository.deleteById(existant.getId());
    }
}
