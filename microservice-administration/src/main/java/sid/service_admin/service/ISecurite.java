/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sid.service_admin.service;

import java.util.Collection;
import java.util.List;
import sid.service_admin.dto.ModuleDTO;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Modulesecurite;
import sid.service_admin.model.Personne;

/**
 *
 * @author USER01
 */
public interface ISecurite {
     Personne getAuthentification(Personne u);
    
    void resetPassword(Personne p, String newPassword);
    
    Modulesecurite getModule(Modulesecurite modulesecurite);
    
    Collection<Modulesecurite> getModuleByUser(Personne u);
    
    Collection<Modulesecurite> getAllModules();
    
    List<ModuleDTO> getModuleForNotUser(Long userid);
    
    Collection<Menu> getMenusByModule(Modulesecurite mod);
    
    Collection<Menu> getMenusbyModuleForUser(Personne u, Modulesecurite m);
    
    Collection<Menu> getMenusForNotModuleUser(Personne u, Modulesecurite m);
    
    void addModuleToUser(Personne u, Modulesecurite m);
    
    void addModulesToUser(Personne u, List<Modulesecurite> mod);
    
    void removeModulesToUser(Personne u, List<Modulesecurite> mod);
    
    void addMenutoUser(Personne u, Menu m);
    
    void removeMenutoUser(Personne u, Menu m);
    
    Collection<Menu> getMenusForUser(Personne u);
    
    void insertUserInLogger(Personne u);
    boolean checkModuleUserExiste(String username,String codemodule);
    boolean checkMenuUserExiste(String username,String codemodule,String codemenu);
}
