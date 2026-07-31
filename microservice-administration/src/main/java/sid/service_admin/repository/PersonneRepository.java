/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;

import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Personne;
//import sid.service_admin.model.User;

/**
 *
 * @author USER01
 */
@Repository
public interface PersonneRepository extends JpaRepository<Personne, Long>{
     Optional<Personne> findByUserName(String userName);
     /** Login insensible a la casse (l'utilisateur peut taper "Jdupont" ou "jdupont") - le mot de passe, lui, reste sensible a la casse. */
     Optional<Personne> findByUserNameIgnoreCase(String userName);
     Optional<Personne> findByEmail(String email);
     boolean existsByEmail(String email);
      @Query("SELECT p  FROM Personne p  WHERE p.userName = :userName and p.password= :password")
    public Personne getAuthentification(@Param("userName") String  userName, @Param("password") String  password);
     @Query("SELECT p FROM Personne p WHERE p.userName = :userName AND p.password = :password")
    Personne findByUserNameAndassword(@Param("userName") String userName, @Param("password") String password);

    // Hierarchie multi-compagnies
    Optional<Personne> findFirstByRoleid_Name(String roleName);
    List<Personne> findByRoleid_Name(String roleName);
    List<Personne> findByCompagnie_Id(Long compagnieId);
    long countByCompagnie_Id(Long compagnieId);
    List<Personne> findByRoleid_NameAndCreatedBy(String roleName, String createdBy);
}
