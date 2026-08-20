package com.mproduits.repositories;

import com.mproduits.model.ProduitPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitPhotoRepository extends JpaRepository<ProduitPhoto, Long> {
}
