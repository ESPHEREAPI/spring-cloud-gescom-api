/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.repositories;

import com.mproduits.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author USER01
 */
public interface PhotoRepository extends JpaRepository<Photo, Long>{
    public Photo findByArticle(long article);
    
}
