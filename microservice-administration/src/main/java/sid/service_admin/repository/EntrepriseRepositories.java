/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.Entreprise;
import sid.service_admin.model.EntreprisePK;

/**
 *
 * @author USER01
 */
public interface EntrepriseRepositories extends JpaRepository<Entreprise, EntreprisePK>{
    Entreprise findByActif(Boolean actif);
}
