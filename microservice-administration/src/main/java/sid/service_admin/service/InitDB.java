/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.service;

import java.util.Collection;
import java.util.List;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.model.Pays;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Personne;
import sid.service_admin.model.Religion;
import sid.service_admin.model.Roles;
import sid.service_admin.model.Titre;

/**
 *
 * @author USER01
 */
public interface InitDB {

    public void addIndicatifPays();

    public Collection<Religion> getAllReligions();

    public Collection<Titre> getAllTitres();

    public void addMenuToAdmin();

    public Collection<Pays> getAllPays();

    public List<Roles> createRoles();

    public List<Permission> createPermission();

    //public UserDTO createAdmin();

    public UserDTO getAdmin();

//     public Collection<Regime> getAllRegime();
//     public Collection<Jour> getAllJour();
//     public ConfigGrilleHoraire getConfigGrilleHoraire();
//    public Collection<Modulesecurite> getAllModuleSecurite();

////     public Collection<Groupe> getAllGroupe(); 
//
//    public Collection<Menu> getMenuByModuleVente();
//
//    public Collection<Menu> getMenuByModuleParametrage();
//
//    public Collection<Menu> getMenuByModuleSecurite();
//
//    public Collection<Menu> getMenuByModuleStock();
//
//    public Collection<Menu> getMenuByModulePhotocopie();
//
//    public Collection<Menu> getMenuByModuleComptabilite();
////     public Collection<Menu> getMenuByModuleEnseignant();
//
////    public Collection<Menu> getMenuByModuleDiscipline();
//    public Collection<Menu> getMenuByModuleAdministration();
//
////    public Collection<Menu> getMenuByModuleBudget();
//    public Collection<Menu> getMenuByModuleMaintenance();
//    public Collection<Menu> getMenuByModuleNotification();
//
//    public Collection<Menu> getMenuByPaie();
//
//
//    public void updateDateEnregistrementForPrixArticle();
}
