/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package microservice_expedition.controller;

import java.io.Serializable;
import java.util.Optional;
import microservice_expedition.dao.ExpeditionRepository;
import microservice_expedition.dto.ExpeditionDto;
import microservice_expedition.model.Expedition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
public class ExpeditionRestController implements Serializable {
    
    private ExpeditionRepository expeditionRepository;
    
    public ExpeditionRestController(ExpeditionRepository expeditionRepository) {
        this.expeditionRepository = expeditionRepository;
    }
    //    public Expedition creerExpedition(@RequestBody ExpeditionDto ex) {
//        Expedition exp = new Expedition();
//        exp.setId(ex.getId());
//        exp.setIdCommande(ex.getIdCommande());
//        exp.setEtat(ex.getEtat());
//        this.expeditionRepository.save(exp);
//       return exp;
//
//    }
    @PostMapping("/create-expedition")

    public ResponseEntity<Expedition> creerExpedition(@RequestBody Expedition ex) {
        // Pour une nouvelle expédition, ne pas définir l'ID manuellement
        Expedition nouvelleExpedition = new Expedition();
        // nouvelleExpedition.setId(ex.getId()); // Supprimer cette ligne pour une création
        nouvelleExpedition.setIdCommande(ex.getIdCommande());
        nouvelleExpedition.setEtat(ex.getEtat());
        
        try {
            Expedition expeditionSauvegardee = expeditionRepository.save(nouvelleExpedition);
            return ResponseEntity.status(HttpStatus.CREATED).body(expeditionSauvegardee);
        } catch (Exception e) {
            // Log l'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/expedition/{id}")
    public ResponseEntity<Expedition> selectExpedition(@PathVariable(name = "id") int idexpedition) {
        
        try {
            Long idExp = Integer.valueOf(idexpedition).longValue();
            Expedition expeditionSauvegardee = this.expeditionRepository.findById(idExp).orElseThrow();
            // Expedition expeditionSauvegardee = expeditionRepository.save(e);
            return ResponseEntity.status(HttpStatus.FOUND).body(expeditionSauvegardee);
        } catch (Exception ex) {
            // Log l'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/expedition-new/{id}")
    public Expedition checkExpedition(@PathVariable(name = "id") int idexpedition) {
        Expedition expedition;
        try {
            Long idExp = Integer.valueOf(idexpedition).longValue();
            Expedition expeditionSauvegardee = this.expeditionRepository.findById(idExp).orElseThrow();
            // Expedition expeditionSauvegardee = expeditionRepository.save(e);
            expedition=new Expedition();
            expedition.setId(expeditionSauvegardee.getId());
            expedition.setEtat(expeditionSauvegardee.getEtat());
            expedition.setIdCommande(expeditionSauvegardee.getIdCommande());
            //return ResponseEntity.status(HttpStatus.FOUND).body(expeditionSauvegardee);
            return expedition;
        } catch (Exception ex) {
            // Log l'erreur
            return new Expedition();
        }
    }
    
    @PutMapping("/update-expedition")
    
    public ResponseEntity<Expedition> updateExpedition(@RequestBody Expedition ex) {
        
        try {
            Expedition expeditionSauvegardee = this.expeditionRepository.findById(ex.getId()).orElseThrow();
            expeditionSauvegardee.setEtat(ex.getEtat());
            expeditionSauvegardee.setIdCommande(ex.getIdCommande());
            expeditionSauvegardee = expeditionRepository.save(expeditionSauvegardee);
            return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).body(expeditionSauvegardee);
        } catch (Exception e) {
            // Log l'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
