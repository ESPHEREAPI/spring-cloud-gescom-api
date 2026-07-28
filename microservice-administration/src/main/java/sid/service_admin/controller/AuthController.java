/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.controller;

/**
 *
 * @author USER01
 */
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sid.service_admin.dto.LoginRequest;
import sid.service_admin.dto.UserCreateDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.dto.UserSessionDTO;
import sid.service_admin.mapper.MapperDtoImpl;
import sid.service_admin.model.Annee;
import sid.service_admin.model.Boutique;
import sid.service_admin.model.Entreprise;
import sid.service_admin.model.Mois;
import sid.service_admin.repository.AnneeRepository;
import sid.service_admin.repository.BoutiqueRepository;
import sid.service_admin.repository.EntrepriseRepositories;
import sid.service_admin.repository.MoisRepository;

import sid.service_admin.service.UserService;
import sid.service_admin.utils.IdleDate;

@RestController
//@RequestMapping("/api")
//@CrossOrigin(origins = "*")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    MapperDtoImpl mapperDtoImpl;
    @Autowired
    AnneeRepository anneeRepository;
    @Autowired
    MoisRepository moisRepositories;
    @Autowired
    EntrepriseRepositories entrepriseRepositories;
       @Autowired
       BoutiqueRepository boutiqueRepository;

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpSession session) {

        UserDTO user = userService.authetification(loginRequest);
        UserSessionDTO userSessionDTO = mapperDtoImpl.mapUserSessionDTOByuserDTO(user);
        int nombre = IdleDate.getMonth(new Date());
        int dateCurent = IdleDate.getYear(new Date());
        Annee an = anneeRepository.findById(dateCurent);
        if(an==null){
            an=new Annee();
            an.setId(dateCurent);
            an.setCode(""+dateCurent);
            an.setLibelle("Annee "+dateCurent);
            anneeRepository.save(an);
        }
        //date = new Date();
       
        Mois mois = moisRepositories.findOneByAnneeAndNumero(dateCurent, nombre);
        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
       userSessionDTO.setAnneeid(e.getAnnee().getId());
        session.setAttribute("mois", mois);
        session.setAttribute("entreprise", e); // si nécessaire
        return ResponseEntity.ok(userSessionDTO);

    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // Supprime toutes les données de session
        return ResponseEntity.ok("Déconnexion réussie");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateDTO userCreateDTO) {
        UserDTO user = userService.createUser(userCreateDTO);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all-users")
    public List<UserDTO> allUers() {
        List<UserDTO> allUsersList = userService.getAllUsers();
        return allUsersList;
    }
    
    @GetMapping("/all-boutiques")
    public ResponseEntity<List<Boutique>> allBoutiques(){
        List<Boutique>boutiques=boutiqueRepository.findAll();
           return ResponseEntity.ok(boutiques);
    }
    
}
