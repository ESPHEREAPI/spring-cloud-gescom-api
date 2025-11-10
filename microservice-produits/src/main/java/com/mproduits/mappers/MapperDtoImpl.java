/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.mappers;

import com.mproduits.dto.Caissedto;
import com.mproduits.dto.ClientDto;
import com.mproduits.dto.ProduitDto;
import com.mproduits.dto.DepoteDto;
import com.mproduits.dto.DevisDTO;
import com.mproduits.dto.DevisItemDTO;
import com.mproduits.dto.InventaireDto;
import com.mproduits.dto.MargeVenteDto;
import com.mproduits.dto.PointVenteDto;
import com.mproduits.dto.VenteDto;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;
import com.mproduits.ecommerce.dto.DTO.Orders_detailsDTO;
import com.mproduits.ecommerce.dto.entites.ClientOrder;
import com.mproduits.ecommerce.dto.repositories.ArticlesRepository;
import com.mproduits.model.Client;
import com.mproduits.model.Devis;
import com.mproduits.model.DevisItem;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Magasin;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.Paiement;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixAchat;
import com.mproduits.model.Produit;
import com.mproduits.model.Vente;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.PaiementRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class MapperDtoImpl {
    
    @Autowired
    LigneVenteRepositories ligneVenteRepositories;
    @Autowired
    PaiementRepositories paiementRepositories;
    @Autowired
    PrixAchatRepositories prixAchatRepositories;
    @Autowired
    PrixArticlesRepositories prixArticlesRepositories;
    @Autowired
    PointVenteRepositories pointVenteRepositories;
    @Autowired
    ProduitRepositories produitRepositories;
    @Autowired
    LigneVenteRepositories ligneVenteRepositories1;
    @Autowired
    ArticlesRepository articlesRepository;
    
    public ProduitDto mapperProduitDto(Produit p) {
        ProduitDto produitDto = new ProduitDto();
        BeanUtils.copyProperties(p, produitDto);
        
        return produitDto;
    }
    
    public Produit mapperProduit(ProduitDto produitDto) {
        Produit p = new Produit();
        BeanUtils.copyProperties(produitDto, p);
        
        return p;
    }
    
    public DepoteDto mapperDepot(MagasinFournisseur magasinFournisseur) {
        DepoteDto depoteDto = new DepoteDto();
        depoteDto.setId(magasinFournisseur.getMagasin().getId());
        depoteDto.setCode(magasinFournisseur.getMagasin().getCode());
        depoteDto.setLibelle(magasinFournisseur.getMagasin().getLibelle());
        depoteDto.setFournisseur(magasinFournisseur.getFournisseur());
        depoteDto.setBoutique(magasinFournisseur.getMagasin().getBoutiqueId());
        return depoteDto;
    }
    
    public DepoteDto mapperDepot(Magasin magasinFournisseur) {
        DepoteDto depoteDto = new DepoteDto();
        depoteDto.setId(magasinFournisseur.getId());
        depoteDto.setCode(magasinFournisseur.getCode());
        depoteDto.setLibelle(magasinFournisseur.getLibelle());
        //depoteDto.setFournisseur(magasinFournisseur.getFournisseur());
        depoteDto.setBoutique(magasinFournisseur.getBoutiqueId());
        return depoteDto;
    }
    
    public ClientDto mapperClientByClientDto(Client client) {
        ClientDto clientDto = new ClientDto();
        BeanUtils.copyProperties(client, clientDto);
        return clientDto;
    }
    
    public Client mapperClientDtoByClient(ClientDto clientDto) {
        Client client = new Client();
        BeanUtils.copyProperties(clientDto, client);
        return client;
    }

//    public VenteDto mapperVentByVenteDto(Vente vente) {
//        VenteDto venteDto = new VenteDto();
//        venteDto.setId(vente.getId());
//        venteDto.setClient(vente.getClient());
//        venteDto.setDate(vente.getDateVente());
//        venteDto.setMontantNet(vente.getTotalNet());
//        venteDto.setMontantRecu(vente.getTotalrecu());
//        venteDto.setMontantTotal(vente.getTotalBrut());
//        venteDto.setNumeroTicket(vente.getNumeroTicket());
//        venteDto.setRemise(vente.getTotalRemise());
//        venteDto.setTypePaiement(paiementRepositories.findByVente(vente).get().getTypePaiement().name());
//        venteDto.setUserinsert(vente.getVendeur().getNom());
//        venteDto.setStatut(vente.getStatut().name());
//        venteDto.setNumerocommande(vente.getNumerocommande());
//        //BeanUtils.copyProperties(vente, venteDto);
//        venteDto.setItems(ligneVenteRepositories.findByVente(vente).stream()
//                .map(lg -> mapperLigneVenteByCaisseDto(lg, vente.getNumeroTicket(), paiementRepositories.findByVente(vente).get()))
//                .collect(Collectors.toList()));
//        return venteDto;
//
//    }
//
//    public Caissedto mapperLigneVenteByCaisseDto(LigneVente ligneVente, String numeroticket, Paiement p) {
//        Caissedto caissedto = new Caissedto();
//        caissedto.setArticle(mapperProduitDto(ligneVente.getProduit()));
//        caissedto.setMontantTotal(ligneVente.getTotalLigne());
//        caissedto.setPrixUnitaire(ligneVente.getPrixUnitaire());
//        caissedto.setQuantite(ligneVente.getQuantite());
//        caissedto.setNumeroTicket(numeroticket);
//        caissedto.setTypePaiement(p.getTypePaiement().name());
//
//        //BeanUtils.copyProperties(ligneVente, caissedto);
//        return caissedto;
//    }
//   
    public VenteDto mapperVentByVenteDto(Vente vente) {
        VenteDto venteDto = new VenteDto();
        venteDto.setId(vente.getId());
        venteDto.setClient(vente.getClient());
        venteDto.setDate(vente.getDateVente());
        venteDto.setMontantNet(vente.getTotalNet());
        venteDto.setMontantRecu(vente.getTotalrecu());
        venteDto.setMontantTotal(vente.getTotalBrut());
        venteDto.setNumeroTicket(vente.getNumeroTicket());
        venteDto.setRemise(vente.getTotalRemise());

        // Gestion paiement
        Paiement paiement = paiementRepositories.findByVente(vente).orElse(null);
        if (paiement != null && paiement.getTypePaiement() != null) {
            venteDto.setTypePaiement(paiement.getTypePaiement().name());
        } else {
            venteDto.setTypePaiement(null); // ou "INCONNU"
        }

        // Gestion vendeur
        if (vente.getVendeur() != null) {
            venteDto.setUserinsert(vente.getVendeur().getNom());
        } else {
            venteDto.setUserinsert(null);
        }

        // Gestion statut
        if (vente.getStatut() != null) {
            venteDto.setStatut(vente.getStatut().name());
        } else {
            venteDto.setStatut(null);
        }
        
        venteDto.setNumerocommande(vente.getNumerocommande());

        // Gestion des lignes de vente
        List<Caissedto> items = ligneVenteRepositories.findByVente(vente).stream()
                .map(lg -> mapperLigneVenteByCaisseDto(lg, vente.getNumeroTicket(), paiement))
                .collect(Collectors.toList());
        venteDto.setItems(items);
        
        return venteDto;
    }
    
    public Caissedto mapperLigneVenteByCaisseDto(LigneVente ligneVente, String numeroticket, Paiement paiement) {
        Caissedto caissedto = new Caissedto();
        caissedto.setArticle(mapperProduitDto(ligneVente.getProduit()));
        caissedto.setMontantTotal(ligneVente.getTotalLigne());
        caissedto.setPrixUnitaire(ligneVente.getPrixUnitaire());
        caissedto.setQuantite(ligneVente.getQuantite());
        caissedto.setNumeroTicket(numeroticket);
        
        if (paiement != null && paiement.getTypePaiement() != null) {
            caissedto.setTypePaiement(paiement.getTypePaiement().name());
        } else {
            caissedto.setTypePaiement(null);
        }
        
        return caissedto;
    }
    
    public LigneVente mapperCcaissedtoByLigneDTO(Caissedto caissedto) {
        LigneVente lv = new LigneVente();
        lv.setPrixUnitaire(caissedto.getPrixUnitaire());
        lv.setProduit(mapperProduit(caissedto.getArticle()));
        lv.setQuantite(caissedto.getQuantite());
        lv.setTotalLigne(caissedto.getMontantTotal());
        return lv;
    }
    
    public MargeVenteDto mapperMargeByLigneVente(LigneVente lv, Date datevente) {
        MargeVenteDto margeVenteDto = new MargeVenteDto();
        PrixAchat pachat = null;
        BigDecimal achat = BigDecimal.ZERO;
        margeVenteDto.setP(mapperProduitDto(lv.getProduit()));
        margeVenteDto.setLibelle(lv.getProduit().getLibelle());
        margeVenteDto.setQuantite(lv.getQuantite().intValue());
        margeVenteDto.setPrixVente(lv.getPrixUnitaire().intValue());
        try {
            Date vente = convertirEnFinDeJournee(datevente);
            pachat = prixAchatRepositories.findTopByProduitAndDatedebutLessThanEqualAndDatefinIsNullOrderByDatedebutDesc(lv.getProduit(), vente).get();
            achat = pachat.getPrix();
            margeVenteDto.setPrixachatid(pachat.getId());
        } catch (Exception e) {
            achat = BigDecimal.ZERO;
            
        }
        
        margeVenteDto.setMontant((lv.getPrixUnitaire().multiply(lv.getQuantite())).doubleValue());
        
        margeVenteDto.setAchat(achat.intValue());
        margeVenteDto.setMontanAchat((achat.multiply(lv.getQuantite())).doubleValue());
        margeVenteDto.setMarge(margeVenteDto.getMontant() - margeVenteDto.getMontanAchat());
        
        return margeVenteDto;
    }
    
    public MargeVenteDto mapperMargeByLigneVente(LigneVente lv, Date debut, Date fin) {
        System.out.println("com.mproduits.mappers.MapperDtoImpl.mapperMargeByLigneVente()" + "LigneVente id=" + lv.getId() + ", Produit=" + lv.getProduit().getLibelle());
        MargeVenteDto margeVenteDto = new MargeVenteDto();
        margeVenteDto.setP(mapperProduitDto(lv.getProduit()));
        margeVenteDto.setLibelle(lv.getProduit().getLibelle());
        margeVenteDto.setQuantite(lv.getQuantite().intValue());
        margeVenteDto.setPrixVente(lv.getPrixUnitaire().intValue());
        PrixAchat pachat = prixAchatRepositories.findTopByProduitAndDatedebutBetweenAndDatefinIsNullOrderByDatedebutDesc(lv.getProduit(), convertirEnFinDeJournee(debut), convertirEnFinDeJourneeFin(fin)).orElse(prixAchatRepositories.findLastPrixAchatByProduit(lv.getProduit()).get());
        margeVenteDto.setMontant((lv.getPrixUnitaire().multiply(lv.getQuantite())).doubleValue());
        BigDecimal achat = pachat == null ? BigDecimal.ZERO : pachat.getPrix();
        margeVenteDto.setAchat(achat.intValue());
        margeVenteDto.setMontanAchat((achat.multiply(lv.getQuantite())).doubleValue());
        margeVenteDto.setMarge(margeVenteDto.getMontant() - margeVenteDto.getMontanAchat());
        margeVenteDto.setPrixachatid(pachat == null ? 0l : pachat.getId());
        return margeVenteDto;
    }
    
    private Date convertirEnFinDeJournee(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 000);
        return cal.getTime();
    }
    
    private Date convertirEnFinDeJourneeFin(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    public PointVenteDto mapperPointVentByPointVentDtoi(PointVente pv) {
        PointVenteDto pvdto = new PointVenteDto();
        pvdto.setBoutique(pv.getBoutique());
        pvdto.setProduit(mapperProduitDto(pv.getProduit()));
        pvdto.setStockFinalTheorie(pv.getStockFinalTheorie());
        pvdto.setPrix(prixArticlesRepositories.findActiveByEntrepriseAndPointVente(pv.getEntreprise(), pv).get().getPrixVenteNet());
        System.out.println("stock final " + pv.getStockFinalTheorie());
        return pvdto;
    }
    
    public InventaireDto mapperPointVentByInventaireDtto(PointVente pv) {
        InventaireDto pvdto = new InventaireDto();
        pvdto.setBoutique(pv.getBoutique());
        pvdto.setProduit(mapperProduitDto(pv.getProduit()));
        pvdto.setCategorie(pv.getProduit().getCategorie());
        pvdto.setQuantite(pv.getStockFinalTheorie());
        PrixAchat last = prixAchatRepositories.findLastPrixAchatByProduit(pv.getProduit()).get();
        pvdto.setPrix(last == null ? BigDecimal.ZERO : last.getPrix());
        pvdto.setTotal(pvdto.getQuantite().multiply(pvdto.getPrix()));
        
        return pvdto;
    }
    
    public static ClientDto toDto(Client entity) {
        ClientDto dto = new ClientDto();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setTelephone(entity.getTelephone());
        dto.setEmail(entity.getEmail());
        dto.setAdresse(entity.getAdresse());
        dto.setStatut(entity.getStatut());
        return dto;
    }
    
    public static Client toEntity(ClientDto dto) {
        Client entity = new Client();
        entity.setId(dto.getId());
        entity.setNom(dto.getNom());
        entity.setTelephone(dto.getTelephone());
        entity.setEmail(dto.getEmail());
        entity.setAdresse(dto.getAdresse());
        entity.setStatut(dto.getStatut());
        return entity;
    }
    
    
    
    public  ProduitDto produitById(Long id){
        ProduitDto pdto=new ProduitDto();
      Optional<Produit> pd =  produitRepositories.findById(id);
      if(pd.isPresent()){
          pdto=this.mapperProduitDto(pd.get());
      }
      return pdto;
    }
    
    public OrdersDTO mapperOrdersDtoVenteDto(Vente v) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setTotalAmount(v.getTotalBrut());
        ordersDTO.setAnnee_id(v.getEntreprise().getAnnee().getId());
        ordersDTO.setId(Long.parseLong(v.getNumeroTicket()));
        ClientOrder clientOrder = new ClientOrder();
        if (v.getClient() != null && v.getClient().getId() != null) {
            clientOrder.setEmail(v.getClient().getEmail());
            clientOrder.setId(v.getClient().getId());
            clientOrder.setName(v.getClient().getNom());
            clientOrder.setPhoneNumber(v.getClient().getTelephone());
        }
        clientOrder.setUsernane(v.getVendeur().getUserName());
        ordersDTO.setStatut(v.getStatut().name());
        ordersDTO.setPayement(v.getDateVente());
        List<LigneVente> allLignes = ligneVenteRepositories.findByVente(v);
        ordersDTO.setProducts(allLignes.stream().map(lg -> mapperOrders_detailsDTOtoDto(lg)).collect(Collectors.toList()));
        return ordersDTO;
    }
    
    private Orders_detailsDTO mapperOrders_detailsDTOtoDto(LigneVente lg) {
        Orders_detailsDTO orders_detailsDTO = new Orders_detailsDTO();
        
        orders_detailsDTO.setProduct(articlesRepository.findByProduit(lg.getProduit().getId()));
        orders_detailsDTO.setQuantite(lg.getQuantite());
        orders_detailsDTO.setPrice(lg.getPrixUnitaire());
        
        return orders_detailsDTO;
        
    }
}
