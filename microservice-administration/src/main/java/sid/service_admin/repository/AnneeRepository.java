/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Annee;

/**
 *
 * @author USER01
 */
@Repository
public interface AnneeRepository extends JpaRepository<Annee, Integer>{
    public Annee findById(int id);
}
