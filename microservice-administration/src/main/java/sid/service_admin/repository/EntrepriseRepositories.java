/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.model.Entreprise;
import sid.service_admin.model.EntreprisePK;

/**
 *
 * @author USER01
 */
public interface EntrepriseRepositories extends JpaRepository<Entreprise, EntreprisePK>{
    Entreprise findByEntreprisePK_CompagnieIdAndActif(Long compagnieId, Boolean actif);

    /**
     * Variante liste : la cle primaire (anneeId, compagnieId) empeche deux
     * exercices actifs pour la MEME annee, mais rien n'empeche deux
     * exercices d'annees differentes d'etre actif=true simultanement pour
     * la meme compagnie (ex: EntrepriseService#obtenirOuCreerExerciceActif,
     * cote microservice-produits, appele en concurrence sur une compagnie
     * flambant neuve sans qu'aucun exercice actif n'existe encore). Dans ce
     * cas findByEntreprisePK_CompagnieIdAndActif plante avec
     * IncorrectResultSizeDataAccessException - utiliser cette methode pour
     * tout appelant qui doit rester utilisable meme si ce doublon existe
     * (voir AuthController, qui bloquerait TOUT utilisateur de la compagnie
     * concernee au login sinon).
     */
    List<Entreprise> findAllByEntreprisePK_CompagnieIdAndActif(Long compagnieId, Boolean actif);
}
