/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.Caissedto;
import com.mproduits.dto.ClientDto;
import com.mproduits.dto.ProduitDto;
import com.mproduits.dto.TicketRequest;
import com.mproduits.dto.UserDTO;
import com.mproduits.dto.VenteDto;
import com.mproduits.ecommerce.dto.DTO.OrdersDTO;
import com.mproduits.ecommerce.dto.DTO.Orders_detailsDTO;
import com.mproduits.ecommerce.dto.entites.Orders;
import com.mproduits.enums.StatutVente;
import com.mproduits.enums.TypePaiement;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Barcodeproduit;
import com.mproduits.model.Client;
import com.mproduits.model.Entreprise;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Mois;
import com.mproduits.model.Paiement;
import com.mproduits.model.Personne;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.model.Profil;
import com.mproduits.model.Property;
import com.mproduits.model.Vente;
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.ClientBonAchatRepositories;
import com.mproduits.repositories.ClientRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PaiementRepositories;
import com.mproduits.repositories.PersonneRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.ProfilRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.utiles.GlobalFonctions;
import com.mproduits.utiles.IdleDate;
import com.mproduits.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author USER01
 */
@Service
public class BarcodeService implements Serializable {

    BarcodeproduitRepositories barcodeproduitRepositories;

    PrixArticlesRepositories prixArticlesRepositories;
    EntrepriseRepositories entrepriseRepositories;
    VenteRepositories venteRepositories;
    PersonneRepositories personneRespositories;
    ClientBonAchatRepositories clientBonAchatRepositories;
    LigneVenteRepositories ligneVenteRepositories;
    PaiementRepositories paiementRepositories;
    PointVenteRepositories pointVenteRepositories;
    ProduitRepositories produitRepositories;
    ClientRepositories clientRepositories;
    @Autowired
    MapperDtoImpl mapper;
    @Autowired
    TicketCaisseService ticketCaisseService;
    @Autowired
    MoisRepositories moisRepositories;
    //  @Autowired
    //  PersonneRepositories personneRepositories;
    @Autowired
    ProfilRepositories profilRepositories;

    @Autowired
    public BarcodeService(BarcodeproduitRepositories barcodeproduitRepositories, PrixArticlesRepositories prixArticlesRepositories, EntrepriseRepositories entrepriseRepositories, VenteRepositories venteRepositories,
            PersonneRepositories personneRespositories, LigneVenteRepositories ligneVenteRepositories, PaiementRepositories paiementRepositories, PointVenteRepositories pointVenteRepositories,
            ProduitRepositories produitRepositories, ClientRepositories clientRepositories) {
        this.barcodeproduitRepositories = barcodeproduitRepositories;
        this.prixArticlesRepositories = prixArticlesRepositories;
        this.entrepriseRepositories = entrepriseRepositories;
        this.venteRepositories = venteRepositories;
        this.personneRespositories = personneRespositories;
        this.ligneVenteRepositories = ligneVenteRepositories;
        this.paiementRepositories = paiementRepositories;
        this.pointVenteRepositories = pointVenteRepositories;
        this.produitRepositories = produitRepositories;
        this.clientRepositories = clientRepositories;
    }

    public ProduitDto chargeProduitByBarcode(String codeBar) {
        String barcode = GlobalFonctions.getCodeBare(codeBar);
        Barcodeproduit bar = barcodeproduitRepositories.findByCodeBard(barcode);
        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        if (bar != null && bar.getId() != null) {
            Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, bar.getProduit());
            return this.chargeValue(pa.get());
        }
        return new ProduitDto();
    }

    private ProduitDto chargeValue(PrixArticles pa) {
        ProduitDto pdto = mapper.mapperProduitDto(pa.getPointVente().getProduit());
        pdto.setBarcode(pa.getCodeBar());
        pdto.setPrixVenteNet(pa.getPrixVenteNet());
        System.out.println("article" + pa.getPointVente().getProduit().getLibelle() + " prix ttc " + pa.getPrixVenteTTC());
        pdto.setPrixVenteTTC((pa.getPrixVenteTTC() == BigDecimal.ZERO || pa.getPrixVenteTTC() == null) ? pa.getPrixVenteNet() : pa.getPrixVenteTTC());
        System.out.println("article" + pa.getPointVente().getProduit().getLibelle() + " prix ttc " + pa.getPrixVenteTTC() + "mis a jouir ");
        pdto.setStockFinal(pa.getPointVente().getStockFinalTheorie());
        pdto.setTva(pa.getTva());
        pdto.setRemise(pa.getRemise());
        return pdto;
    }

    /**
     * Récupère les articles les plus populaires pour l'affichage initial Cache
     * pendant 10 minutes
     */
    @Cacheable(value = "topArticles", unless = "#result.isEmpty()")
    public List<ProduitDto> getTopArticles() {
        PageRequest limit = PageRequest.of(0, 10000); // page 0, 10 000 éléments max
        List<PrixArticles> articles = prixArticlesRepositories.findTopActifWithStockFinalPositive(limit);
        // List<PrixArticles> articles = prixArticlesRepositories.findTop10000ByActifTrueOrderByDateCreationDesc();
        return articles.stream()
                .map(p -> chargeValue(p))
                .collect(Collectors.toList());
    }

    //rechercher un article a partir de la reference
    public ProduitDto getArticlesByReference(String reference) {
        Optional<PrixArticles> prixarticle = prixArticlesRepositories.getPrixAticlesByReferenceProduit(reference);
        if (prixarticle.isPresent()) {
            ProduitDto pdo = chargeValue(prixarticle.get());
            return pdo;
        }
        return null;
    }

    public ProduitDto getArticlesByProduitId(Long produitid) {
        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<Produit> p = this.produitRepositories.findById(produitid);
        Optional<PrixArticles> prixarticle = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p.get());
        if (prixarticle.isPresent()) {
            ProduitDto pdo = chargeValue(prixarticle.get());
            return pdo;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long valideVente(VenteDto venteDto) {
        // on creer directement la vente 
        //verifions l existance du usert insert
        Client saveClient = null;
        Vente vente = null;
        try {
            Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
            Optional<Personne> userEntite = personneRespositories.findByUserName(venteDto.getUserinsert());
            if (userEntite.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + venteDto.getUserinsert());
            }

            Personne userinsert = userEntite.get();

            //  if (TypePaiement.valueOf(venteDto.getTypePaiement()) == TypePaiement.BON_ACHAT) {
            Optional<Client> cl = clientRepositories.findByNom(venteDto.getClient().getNom());
            if (cl.isEmpty()) {
                Client client = new Client();
                client.setEmail(venteDto.getClient().getEmail());
                client.setNom(venteDto.getClient().getNom());
                client.setTelephone(venteDto.getClient().getTelephone());
                client.setFidelite(true);
                saveClient = clientRepositories.save(client);
            } else {
                saveClient = cl.get();
            }

            // }
            vente = new Vente();
            vente.setDateVente(venteDto.getDate());
            vente.setEntreprise(e);
            vente.setNumeroTicket(venteDto.getNumeroTicket());
            vente.setStatut(StatutVente.TERMINEE);
            vente.setTotalBrut(venteDto.getMontantTotal());
            vente.setTotalNet(venteDto.getMontantNet());
            vente.setTotalRemise(venteDto.getRemise());
            vente.setTotalrecu(venteDto.getMontantRecu());
            vente.setVendeur(userinsert);
            if (saveClient != null && saveClient.getId() != null) {
                vente.setClient(saveClient);
            }

            Vente saveVente = venteRepositories.save(vente);
            venteDto.getItems().stream()
                    .map(it -> createLigenVente(it, saveVente))
                    .collect(Collectors.toList());

            //enregitrement paiement de la vente 
            Paiement paiement = new Paiement();
            paiement.setDatePaiement(new Date());
            paiement.setMontant(venteDto.getMontantNet());
            paiement.setTypePaiement(TypePaiement.valueOf(venteDto.getTypePaiement()));
            paiement.setReference(venteDto.getNumeroTicket());
            paiement.setVente(saveVente);
            paiementRepositories.save(paiement);

            return saveVente.getId();
        } catch (Exception e) {
            // le rollback est automatique avec @Transactional si exception est levée
            throw new RuntimeException("Échec de l'enregistrement de la vente : " + e.getMessage(), e);
        }

    }

    public Long valideVente(VenteDto venteDto, long numerocommande) {
        // on creer directement la vente 
        //verifions l existance du usert insert
        Client saveClient = null;
        Vente vente = null;
        try {
            Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
            Optional<Personne> userEntite = personneRespositories.findByUserName(venteDto.getUserinsert());
            if (userEntite.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + venteDto.getUserinsert());
            }

            Personne userinsert = userEntite.get();

            //  if (TypePaiement.valueOf(venteDto.getTypePaiement()) == TypePaiement.BON_ACHAT) {
            Optional<Client> cl = clientRepositories.findByNom(venteDto.getClient().getNom());
            if (cl.isEmpty()) {
                Client client = new Client();
                client.setEmail(venteDto.getClient().getEmail());
                client.setNom(venteDto.getClient().getNom());
                client.setTelephone(venteDto.getClient().getTelephone());
                client.setFidelite(true);
                saveClient = clientRepositories.save(client);
            } else {
                saveClient = cl.get();
            }

            // }
            vente = venteRepositories.findByNumeroTicketAndStatutForCommande(numerocommande, StatutVente.EN_COURS, e.getAnnee().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable pour le ticket " + numerocommande));
            vente.setDateVente(venteDto.getDate());
            vente.setEntreprise(e);
            vente.setNumeroTicket(venteDto.getNumeroTicket());
            vente.setStatut(StatutVente.TERMINEE);
            vente.setTotalBrut(venteDto.getMontantTotal());
            vente.setTotalNet(venteDto.getMontantNet());
            vente.setTotalRemise(venteDto.getRemise());
            vente.setTotalrecu(venteDto.getMontantRecu());
            vente.setVendeur(userinsert);
            if (saveClient != null && saveClient.getId() != null) {
                vente.setClient(saveClient);
            }

            Vente saveVente = venteRepositories.save(vente);
            //recuperation des lignes pour insertion avec statut terminer

            List<LigneVente> ligneVentesRference = venteDto.getItems().stream()
                    .map(it -> mapper.mapperCcaissedtoByLigneDTO(it))
                    .collect(Collectors.toList());
            //System.out.println("liste reference :");
            //ligneVentesRference.forEach(System.out::print);

            // on recupere les ligne dejaenregister dans la partie commeande
            List<LigneVente> listeLigneVenteSource = ligneVenteRepositories.findByVente(vente);
           // System.out.println("liste source :");
           // listeLigneVenteSource.forEach(System.out::print);

            this.synchroniserListesInPlaceAlternative(ligneVentesRference, listeLigneVenteSource,vente);
            //System.out.println("liste source sinchroniser :");
            //listeLigneVenteSource.forEach(System.out::print);
            
           // this.updatelignevente(listeLigneVenteSource);

            //enregitrement paiement de la vente 
            Paiement paiement = paiementRepositories.findByVente(saveVente)
                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable pour le ticket " + saveVente.getId()));
            paiement.setDatePaiement(new Date());
            paiement.setMontant(venteDto.getMontantNet());
            paiement.setTypePaiement(TypePaiement.valueOf(venteDto.getTypePaiement()));
            paiement.setReference(venteDto.getNumeroTicket());
            paiement.setVente(saveVente);
            paiementRepositories.save(paiement);

            return saveVente.getId();
        } catch (Exception e) {
            // le rollback est automatique avec @Transactional si exception est levée
            throw new RuntimeException("Échec de l'enregistrement de la vente : " + e.getMessage(), e);
        }

    }

    public void updatelignevente(List<LigneVente> allVentes) {
        // Utiliser un iterator pour pouvoir supprimer en toute sécurité
        Iterator<LigneVente> iterator = allVentes.iterator();
        LigneVente lv;
        while (iterator.hasNext()) {
            LigneVente ligneSource = iterator.next();
            String cle = genererCle(ligneSource);
            Optional<LigneVente> lgv = ligneVenteRepositories.findByVenteAndProduit(ligneSource.getVente(), ligneSource.getProduit());
            if (lgv.isEmpty()) {
                lv = new LigneVente();
                lv.setPrixUnitaire(ligneSource.getPrixUnitaire());
                lv.setProduit(ligneSource.getProduit());
                lv.setQuantite(ligneSource.getQuantite());
                lv.setTotalLigne(ligneSource.getQuantite().multiply(ligneSource.getPrixUnitaire()));
                lv.setVente(ligneSource.getVente());
                ligneVenteRepositories.save(lv);

            } else {
                lv = lgv.get();
                lv.setPrixUnitaire(ligneSource.getPrixUnitaire());
                lv.setProduit(ligneSource.getProduit());
                lv.setQuantite(ligneSource.getQuantite());
                lv.setTotalLigne(ligneSource.getQuantite().multiply(ligneSource.getPrixUnitaire()));
                lv.setVente(ligneSource.getVente());
                ligneVenteRepositories.save(lv);
            }

        }
    }

    @Transactional(rollbackFor = Exception.class)

    public Vente valideVenteEnCours(OrdersDTO ordersDTO) {
        // on creer directement la vente 
        //verifions l existance du usert insert
        Client saveClient = null;
        Vente vente = null;
        // VenteDto venteDto=null;
        try {

            //  venteDto =mapper.mapperVenteDtoOrdersDto(ordersDTO, pv, numerocomande, cde, Usernane);
            Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
            Optional<Personne> userEntite = personneRespositories.findByUserName(ordersDTO.getClient().getUsernane());
            if (userEntite.isEmpty()) {
                throw new ResourceNotFoundException("User not found with id: " + ordersDTO.getClient().getUsernane());
            }

            Personne userinsert = userEntite.get();

            //  if (TypePaiement.valueOf(venteDto.getTypePaiement()) == TypePaiement.BON_ACHAT) {
            Optional<Client> cl = clientRepositories.findByNom(ordersDTO.getClient().getName());
            if ((!"".equals(ordersDTO.getClient().getName()) || ordersDTO.getClient().getName() != null) && cl.isEmpty()) {
                Client client = new Client();
                client.setEmail(ordersDTO.getClient().getEmail());
                client.setNom(ordersDTO.getClient().getName());
                client.setTelephone(ordersDTO.getClient().getPhoneNumber());
                client.setFidelite(true);
                saveClient = clientRepositories.save(client);
            } else {
                saveClient = cl.get();
            }

            String numero = this.getNumero_commande(e.getAnnee().getId());

            while (venteRepositories.findByEntrepriseAndNumeroTicket(e, numero).isPresent()) {
                numero = this.getNumero_commande(e.getAnnee().getId());
            }
            // }
            vente = new Vente();
            vente.setDateVente(ordersDTO.getDate());
            vente.setEntreprise(e);

            vente.setStatut(StatutVente.EN_COURS);
            vente.setTotalBrut(ordersDTO.getTotalAmount());
            vente.setTotalNet(ordersDTO.getTotalAmount());
            // vente.setTotalRemise(venteDto.getRemise());
            // vente.setTotalrecu(venteDto.getMontantRecu());
            vente.setNumeroTicket(numero);
            vente.setNumerocommande(Long.parseLong(numero));
            vente.setVendeur(userinsert);
            vente.setUserecom(ordersDTO.getClient().getUsernane());
            if (saveClient != null && saveClient.getId() != null) {
                vente.setClient(saveClient);
            }

            Vente saveVente = venteRepositories.save(vente);

            ordersDTO.getProducts().stream()
                    .map(it -> createLigenVenteEnCours(it, saveVente))
                    .collect(Collectors.toList());

            //enregitrement paiement de la vente 
            Paiement paiement = new Paiement();
            paiement.setDatePaiement(new Date());
            paiement.setMontant(ordersDTO.getTotalAmount());
            paiement.setTypePaiement(TypePaiement.ESPECES);
            paiement.setReference(saveVente.getNumeroTicket());
            paiement.setVente(saveVente);
            paiementRepositories.save(paiement);
            // if (venteRepositories.findById(saveVente.getId()).isEmpty()) {
            //     saveVente.setId(null);

            // }
            return saveVente;
        } catch (Exception e) {
            // le rollback est automatique avec @Transactional si exception est levée
            throw new RuntimeException("Échec de l'enregistrement de la vente : " + e.getMessage(), e);
        }

    }

    public String getNumero_commande(int annee) {
        List<Vente> allVente = venteRepositories.findAll();
        long nbre = 0L;
        if (allVente.isEmpty()) {
            nbre++;
        } else {
            nbre = allVente.size() + 1;
        }

        return "" + annee + "" + nbre;

    }

    private LigneVente createLigenVente(Caissedto caissedto, Vente v) {

        try {
            LigneVente ligneVente = new LigneVente();

            ligneVente.setPrixUnitaire(caissedto.getPrixUnitaire());
            ligneVente.setQuantite(caissedto.getQuantite());
            ligneVente.setTotalLigne(caissedto.getMontantTotal());
            ligneVente.setVente(v);
            ligneVente.setProduit(mapper.mapperProduit(caissedto.getArticle()));
            LigneVente ligneSave = ligneVenteRepositories.save(ligneVente);

            //destockage articles 
            Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(v.getEntreprise(), ligneVente.getProduit());
            if (pa.isEmpty()) {
                throw new RuntimeException("Article introuvable");
            }
            if (pa.get().getPointVente().getStockFinalTheorie().intValue() < caissedto.getQuantite().intValue()) {
                throw new RuntimeException("Stock insuffisant");
            }
            BigDecimal sortie = pa.get().getPointVente().getSortiProduit().add(caissedto.getQuantite());
            BigDecimal stockFinal = pa.get().getPointVente().getStockFinalTheorie().subtract(caissedto.getQuantite());
            pa.get().getPointVente().setSortiProduit(sortie);
            pa.get().getPointVente().setStockFinalTheorie(stockFinal);
            pointVenteRepositories.save(pa.get().getPointVente());

            return ligneSave;
        } catch (Exception e) {
            // le rollback est automatique avec @Transactional si exception est levée
            throw new RuntimeException("Échec de l'enregistrement de la vente : " + e.getMessage(), e);
        }

    }

    private LigneVente createLigenVenteEnCours(Orders_detailsDTO caissedto, Vente v) {

        try {
            LigneVente ligneVente = new LigneVente();

            ligneVente.setPrixUnitaire(caissedto.getPrice());
            ligneVente.setQuantite(caissedto.getQuantite());
            ligneVente.setTotalLigne(caissedto.getPrice().multiply(caissedto.getQuantite()));
            // ligneVente.setVente(v);
            ligneVente.setProduit(produitRepositories.findById(caissedto.getProduct().getProduit()).get());
            LigneVente ligneSave;

            //destockage articles 
            Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(v.getEntreprise(), ligneVente.getProduit());
            if (pa.isEmpty()) {
                throw new RuntimeException("Article introuvable");
            }
            if (pa.get().getPointVente().getStockFinalTheorie().intValue() < caissedto.getQuantite().intValue()) {
                System.out.println("produit: " + pa.get().getPointVente().getProduit().getLibelle());
                System.out.println("stock final: " + pa.get().getPointVente().getStockFinalTheorie().intValue());
                System.out.println("quantite commande: " + caissedto.getQuantite().intValue());
                venteRepositories.delete(v);
                throw new RuntimeException("Stock insuffisant");
            }
            BigDecimal sortie = pa.get().getPointVente().getSortiProduit().add(caissedto.getQuantite());
            BigDecimal stockFinal = pa.get().getPointVente().getStockFinalTheorie().subtract(caissedto.getQuantite());
            pa.get().getPointVente().setSortiProduit(sortie);
            pa.get().getPointVente().setStockFinalTheorie(stockFinal);
            pointVenteRepositories.save(pa.get().getPointVente());
            //Vente saveVente = venteRepositories.save(v);
            ligneVente.setVente(v);

            ligneSave = ligneVenteRepositories.save(ligneVente);

            return ligneSave;
        } catch (Exception e) {
            // le rollback est automatique avec @Transactional si exception est levée
            throw new RuntimeException("Échec de l'enregistrement de la vente : " + e.getMessage(), e);
        }

    }

    public Vente getVenteById(Long venteid) {
        Optional<Vente> v = venteRepositories.findById(venteid);
        if (v.isEmpty()) {
            throw new ResourceNotFoundException("vente  not found with id: " + venteid);
        }
        return v.get();
    }

    public List<LigneVente> listeLigneVenteByVente(Vente v) {
        return ligneVenteRepositories.findByVente(v);
    }

    public Paiement getPaiementByVente(Vente v) {
        return paiementRepositories.findByVente(v).get();
    }

    public void createTicaisseTXT(Vente v) throws IOException {
        Mois m = getMoisByNumero();
        TicketRequest ticketRequest = TicketRequest.builder()
                .lignesVente(ligneVenteRepositories.findByVente(v))
                .remboursementAvecBonAchat(Boolean.FALSE)
                .typePaiement(paiementRepositories.findByVente(v).get().getTypePaiement())
                .mois(m.getMois())
                .vente(v)
                .property(new Property())
                .build();

        ticketCaisseService.generateTicket(ticketRequest);

    }

    public Mois getMoisByNumero() {
        int annee = IdleDate.getYear(new Date());
        int numero = IdleDate.getMonth(new Date());

        Mois m = moisRepositories.findOneByAnneeAndNumero(annee, numero);
        return m;
    }

    public List<ClientDto> allClient() {
        List<ClientDto> listesClientDto = clientRepositories.findAll().stream()
                .map(cl -> mapper.mapperClientByClientDto(cl))
                .collect(Collectors.toList());
        return listesClientDto;
    }

    public Client createClientCaisse(ClientDto clientDto) {
        Optional<Client> client = clientRepositories.findByNom(clientDto.getNom());
        Client nouveauClient = null;
        if (client.isEmpty()) {
            Client cl = new Client();
            cl.setNom(clientDto.getNom());
            cl.setTelephone(clientDto.getTelephone());
            cl.setEmail(clientDto.getEmail());
            nouveauClient = clientRepositories.save(cl);
        }
        return nouveauClient;

    }

    public List<UserDTO> listeCaissiers() {
        String profile = "CAISSE";
        Profil p = profilRepositories.findByCode(profile);
        List<UserDTO> allCaissier = personneRespositories.findActiveUserByProfil(p).stream()
                .filter(pers -> pers.getCompteActif() == true && "CAISSE".equals(pers.getProfilid().getCode()))
                .map(users -> cresateByUserDTO(users))
                .collect(Collectors.toList());
        return allCaissier;

    }

    private UserDTO cresateByUserDTO(Personne p) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(p.getUserName());
        userDTO.setFirstName(p.getNom());
        userDTO.setLastname(p.getPrenom());
        return userDTO;

    }

    /**
     * Version alternative qui modifie directement la liste source
     *
     * @param listeReference La liste de référence
     * @param listeSource La liste source à modifier (sera modifiée directement)
     */
//    public void synchroniserListesInPlace(List<LigneVente> listeReference,
//            List<LigneVente> listeSource) {
//
//        // Créer une map de la liste de référence
//        Map<String, LigneVente> mapReference = listeReference.stream()
//                .collect(Collectors.toMap(
//                        this::genererCle,
//                        ligne -> ligne,
//                        (existing, replacement) -> replacement
//                ));
//
//        // Utiliser un iterator pour pouvoir supprimer en toute sécurité
//        Iterator<LigneVente> iterator = listeSource.iterator();
//
//        while (iterator.hasNext()) {
//            LigneVente ligneSource = iterator.next();
//            String cle = genererCle(ligneSource);
//
//            if (mapReference.containsKey(cle)) {
//                // Remplacer par l'élément de référence
//                LigneVente ligneReference = mapReference.get(cle);
//                int index = listeSource.indexOf(ligneSource);
//                listeSource.set(index, ligneReference);
//            } else {
//                // Supprimer si n'existe pas dans la référence
//                ligneVenteRepositories.delete(ligneSource);
//                iterator.remove();
//            }
//        }
//    }

    
    /**
 * Alternative plus sûre si vous voulez vraiment remplacer les objets
 */
public void synchroniserListesInPlaceAlternative(List<LigneVente> listeReference,
            List<LigneVente> listeSource, Vente vente) {
    
    // Supprimer toutes les anciennes lignes
    List<LigneVente> lignesToDelete = new ArrayList<>(listeSource);
    for (LigneVente ligne : lignesToDelete) {
        ligneVenteRepositories.delete(ligne);
    }
    listeSource.clear();
    
    // Ajouter les nouvelles lignes avec la bonne relation
    for (LigneVente ligneReference : listeReference) {
        // Créer une nouvelle instance pour éviter les problèmes de relation
        LigneVente nouvelleLigne = new LigneVente();
        nouvelleLigne.setQuantite(ligneReference.getQuantite());
        nouvelleLigne.setPrixUnitaire(ligneReference.getPrixUnitaire());
        nouvelleLigne.setRemise(ligneReference.getRemise());
        nouvelleLigne.setTotalLigne(ligneReference.getTotalLigne());
        nouvelleLigne.setProduit(ligneReference.getProduit());
        nouvelleLigne.setVente(vente); // Relation correcte
        ligneVenteRepositories.save(nouvelleLigne);
        
        listeSource.add(nouvelleLigne);
    }
}
    /**
     * Génère une clé unique pour identifier une LigneVente Vous pouvez adapter
     * cette méthode selon vos critères de comparaison
     *
     * @param ligne La ligne de vente
     * @return Une clé unique sous forme de String
     */
    private String genererCle(LigneVente ligne) {
        if (ligne == null) {
            return "null";
        }

        // Option 1: Utiliser l'ID si disponible
        String cle = "null";
        if (ligne.getProduit() != null && ligne.getProduit().getId() != null) {
            cle = "" + ligne.getProduit().getId();
        }

        return cle.toString();
    }
}
