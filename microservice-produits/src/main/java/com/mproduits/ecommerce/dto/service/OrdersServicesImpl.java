/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.DTO.OrdersDTO;

import com.mproduits.ecommerce.dto.entites.Orders;
import com.mproduits.ecommerce.dto.entites.Orders_details;
import com.mproduits.ecommerce.dto.mappers.ProduitMapperImpl;
import com.mproduits.ecommerce.dto.repositories.ClientOrderRepository;
import com.mproduits.ecommerce.dto.repositories.OrdersRepository;
import com.mproduits.ecommerce.dto.repositories.Orders_detailsRepository;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Vente;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.security.TenantContext;
import com.mproduits.services.BarcodeService;
import com.mproduits.services.EntrepriseService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author USER01
 */
@Service
@AllArgsConstructor
public class OrdersServicesImpl implements OrdersServices {
    
    private final OrdersRepository ordersRepository;
    private final Orders_detailsRepository orders_ordersRepository;
    private final ProduitMapperImpl dtoMapper;
       
    private final ClientOrderRepository clientOrderRepository;
    private final BarcodeService barcodeServiceImpl;
    private final VenteRepositories venteRepositories;
    private final EntrepriseRepositories entrepriseRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;
    @Autowired
    MapperDtoImpl mapperImpl;
    
    @Override
    public String getNumero_commande(int annee) {
        List<Orders> allOrders = ordersRepository.findAll();
        long nbre = 0L;
        if (allOrders.isEmpty()) {
            nbre++;
        } else {
            nbre = allOrders.size() + 1;
        }
        
        return "" + annee + "" + nbre;
        
    }
    
    @Override
    public List<Orders_details> listeCommandes(Orders orders) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    @Transactional
    public OrdersDTO addCommande(OrdersDTO ordersDTO) {
//        List<Orders_details> listeOrdersDetails = new ArrayList<>();
//        String numero = this.getNumero_commande(ordersDTO.getProducts().get(0).getProduct().getAnneeId());
//        
//        while (ordersRepository.findByNumero(numero) != null) {            
//            numero = this.getNumero_commande(ordersDTO.getProducts().get(0).getProduct().getAnneeId());
//        }
//        Orders orders = new Orders();
//        orders.setClientOrder(clientOrderRepository.save(ordersDTO.getClient()));
//        orders.setNumero(numero);
//        orders.setMontant(ordersDTO.getTotalAmount());
//        orders.setDate_enregistrement(ordersDTO.getDate());
//        orders.setHeure(ordersDTO.getDate());
//        this.ordersRepository.save(orders);
//        listeOrdersDetails = ordersDTO.getProducts().stream()
//                .map(od -> dtoMapper.formOrders_detailsDTO(od, orders)).collect(Collectors.toList());
//        
//        orders_ordersRepository.saveAll(listeOrdersDetails);
//        return dtoMapper.formOrders(orders);
        Vente v = this.barcodeServiceImpl.valideVenteEnCours(ordersDTO);
        
        return this.mapperImpl.mapperOrdersDtoVenteDto(v);
    }
    
    @Override
    public List<OrdersDTO> listesOrdersByUsers(String user) {
        Entreprise e=entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        List<OrdersDTO> listesOrdersByUsers = this.venteRepositories.allVentesByUsernameEcom(user,e.getAnnee().getId()).stream()
                .map(od -> this.mapperImpl.mapperOrdersDtoVenteDto(od))
                .collect(Collectors.toList());
        return listesOrdersByUsers;
    }
    
}
