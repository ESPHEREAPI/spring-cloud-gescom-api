/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.UserDTO;
import com.mproduits.model.Personne;
import com.mproduits.model.Profil;
import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface PersonneRepositories extends JpaRepository<Personne, Long> {

    Optional<Personne> findByUserName(String userName);

    // Lookup par username scope compagnie - a utiliser a la place de
    // findByUserName() partout ou le username vient d'une requete client
    // (evite qu'un utilisateur d'une compagnie A fasse reference a une
    // personne appartenant a une compagnie B, ex: attribution du vendeur
    // d'une vente).
    Optional<Personne> findByUserNameAndBoutique_Compagnie_Id(String userName, Long compagnieId);

    Optional<Personne> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Personne p WHERE p.profilid = :profil AND p.compteActif = true")
    List<Personne> findActiveUserByProfil(@Param("profil") Profil profil);

    // Filtre par CODE de profil (pas par une entite Profil resolue au
    // prealable) : la table profil n'a pas de colonne compagnie dans ce
    // module - chaque compagnie a sa propre ligne portant le meme code
    // (ex. "CAISSIER"), donc plusieurs lignes existent en base avec ce
    // code des qu'il y a plusieurs compagnies. Filtrer par boutique.id
    // suffit a isoler la bonne compagnie (une boutique = une compagnie).
    @Query("SELECT p FROM Personne p WHERE p.profilid.code = :profilCode AND p.compteActif = true AND p.boutique.id = :boutiqueId")
    List<Personne> findActiveUserByProfilAndBoutiqueId(@Param("profilCode") String profilCode, @Param("boutiqueId") Long boutiqueId);

    // Variante compagnie entiere (toutes boutiques confondues) - un
    // admin/gerant qui n'est rattache a aucune boutique precise (role
    // transverse a la compagnie) n'a pas de boutiqueId exploitable pour
    // findActiveUserByProfilAndBoutiqueId ci-dessus, ce qui lui renvoyait
    // toujours une liste de caissiers vide (voir Historique Caisse).
    @Query("SELECT p FROM Personne p WHERE p.profilid.code = :profilCode AND p.compteActif = true AND p.boutique.compagnie.id = :compagnieId")
    List<Personne> findActiveUserByProfilAndCompagnieId(@Param("profilCode") String profilCode, @Param("compagnieId") Long compagnieId);

}
