/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.CommandeDto;
import com.mproduits.enums.TypeMois;
import com.mproduits.exceptions.Success;
import com.mproduits.model.Annee;
import com.mproduits.model.Barcodeproduit;
import com.mproduits.model.Boutique;
import com.mproduits.model.Commande;
import com.mproduits.model.DetailCommandeEntree;
import com.mproduits.model.DetailCommandeSortie;
import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import com.mproduits.model.Fournisseur;

import com.mproduits.model.Magasin;
import com.mproduits.model.Mois;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixAchat;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.PrixVente;

import com.mproduits.model.Produit;
import com.mproduits.repositories.AnneeRepository;
import com.mproduits.repositories.BarcodeproduitRepositories;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.DetailCommandeEntreeRepositories;
import com.mproduits.repositories.DetailCommandeSortieRepositories;
import com.mproduits.repositories.EntrepriseRepositories;

import com.mproduits.repositories.FournisseurRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.MoisRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.security.TenantContext;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.PrixVenteRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.utiles.GlobalFonctions;
import com.mproduits.utiles.IdleDate;
import com.mproduits.exceptions.GlobalException;
import com.mproduits.exceptions.MetierException;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author USER01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@AllArgsConstructor
public class ServiceCommande implements IServiceCommande {

    @Autowired
    EntrepriseRepositories entrepriseRepositories;
    @Autowired
    ProduitRepositories produitRepositories;
    @Autowired
    TenantContext tenantContext;
    @Autowired
    MagasinRepositories magasinRepositories;
    @Autowired
    PointVenteRepositories pointVenteRepositories;
    @Autowired
    PrixArticlesRepositories prixArticlesRepositories;
    @Autowired
    CommandeRepositories commandeRepositories;
    @Autowired
    FournisseurRepositories fourniseurRepositories;
    @Autowired
    IParamModule paramModuleService;
    @Autowired
    MoisRepositories moisRepositories;
    @Autowired
    AnneeRepository anneeRepository;
    @Autowired
    DetailCommandeEntreeRepositories detailCommandeEntreeRepositories;
    @Autowired
    DetailCommandeSortieRepositories detailCommandeSortieRepositories;
    @Autowired
    PrixVenteRepositories prixVenteRepositories;
    @Autowired
    PrixAchatRepositories prixAchatRepositories;
    @Autowired
    BarcodeproduitRepositories barcodeproduitRepositories;
    @Autowired
    BoutiqueRepositories boutiqueRepositories;
    @Autowired
    EntrepriseService entrepriseService;

    /**
     * L'exercice actif est toujours celui de la compagnie de l'appelant
     * (derive du TenantContext) - le parametre "actif" est conserve pour
     * compatibilite d'interface mais n'a plus d'effet, un seul exercice actif
     * existe par compagnie a la fois (voir EntrepriseService).
     */
    @Override
    public Entreprise getEntrepriseActif(Boolean actif) {
        return entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

    }

    @Override
    public BigInteger calculateStock(Long produitId, Long depotId, Long boutiqueId, Entreprise entreprise) {
        Optional<Produit> produit = produitRepositories.findByIdAndCompagnie_Id(produitId, tenantContext.currentCompagnieId());
        Optional<Magasin> depot = magasinRepositories.findById(depotId);

        if (boutiqueId != null && produit.isPresent() && depot.isPresent()) {
            // Calcul pour boutique
            Optional<PointVente> pv = pointVenteRepositories.findByProduitAndBoutiqueAndEntreprise(produit.get(), depot.get().getBoutiqueId(), entreprise);

            if (pv.isPresent()) {
                Optional<PrixArticles> pa = prixArticlesRepositories.findActiveByEntrepriseAndPointVente(entreprise, pv.get());

                return pa.isPresent() ? pa.get().getPointVente().getStockFinalTheorie().toBigInteger() : BigInteger.ZERO;
            }
        } else {
            // Calcul pour dépôt
            Optional<Commande> derniere = commandeRepositories.findLastCommandeByProduitAndDepot(produit.get(), depot.get());
            return derniere.isPresent() ? derniere.get().getStockFinalTheorie().toBigInteger() : BigInteger.ZERO;
        }

        return BigInteger.ZERO;

    }

    @Override
    public Commande createCommandeFromRequest(CommandeDto commandeDto, Entreprise entreprise, Mois mois) {
        Commande commande = new Commande();

        commande.setPrixAchat(commandeDto.getPrixAchat());
        commande.setPrixVente(commandeDto.getPrixVente());
        commande.setEntreeProduit(commandeDto.getQuantite());

        // Récupération du produit
        Optional<Produit> produit = produitRepositories.findByIdAndCompagnie_Id(commandeDto.getProduitid(), tenantContext.currentCompagnieId());

        commande.setProduit(produit.get());

        // Récupération du dépôt fournisseur
        Optional<Magasin> depot = magasinRepositories.findById(commandeDto.getDepotid());
        Optional<Fournisseur> fournisseur = fourniseurRepositories.findById(commandeDto.getFournisseurid());
        //DepotFournisseur depotFournisseur = depotFournisseurService.findById(request.getDepotFournisseurId());
        commande.setMagasinid(depot.get());
        commande.setFournisseurid(fournisseur.get());

        // Génération du numéro si auto
        // if (paramModuleService.getParamModule().get) {
        //  commande.setNumero_Repartition(generateCodeCommande());
        //}
        return commande;

    }

    @Override
    public void handleBoutiqueApprovisionnement(Commande commande, CommandeDto commandeDto, Entreprise entreprise, Mois mois) {

        try {
            // Mise à jour du point de vente

            Optional<PointVente> pv = pointVenteRepositories.findByProduitAndBoutiqueAndEntreprise(
                    commande.getProduit(),
                    commande.getMagasinid().getBoutiqueId(),
                    entreprise);

            if (pv.isPresent()) {
                updatePointVenteStock(pv.get());
            }

            // Gestion du code-barres
            Barcodeproduit barcodeProduit = new Barcodeproduit();
            if (commandeDto.getCodeBarre() != null) {
                String codeB = commandeDto.getCodeBarre();
                if (codeB.contains(GlobalFonctions.CARACTER_ASCII)) {
                    codeB = GlobalFonctions.getCodeBare(codeB);
                }
                barcodeProduit.setCodeBard(codeB);

            }

        } catch (Exception e) {
            // log.error("Erreur lors du traitement boutique: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du traitement boutique", e);
        }
    }

    @Override
    public void updatePointVenteStock(PointVente pv) {
        BigDecimal stockFinal = pv.getStockFinalTheorie();
        BigDecimal stockTestFinal = pv.getEntreeProduit()
                .add(pv.getStockInitial())
                .subtract(pv.getSortiProduit());

        if (!stockFinal.subtract(stockTestFinal).equals(BigInteger.ZERO)) {
            pv.setEntreeProduit(stockFinal);
            pv.setSortiProduit(BigDecimal.ZERO);
            pv.setStockInitial(BigDecimal.ZERO);
            pointVenteRepositories.save(pv);
        }

    }

    @Override
    public String generateCodeCommande(HttpSession session) {
        Entreprise e = (Entreprise) session.getAttribute(GlobalFonctions.ENTREPRISE_ACTIF);

        Long count = commandeRepositories.count();
        boolean exists = true;
        String code = null;

        while (exists) {
            StringBuilder sb = new StringBuilder();
            int length = count.toString().length();

            for (int i = 0; i < 5 - length; i++) {
                sb.append("0");
            }
            sb.append(++count);

            code = sb.toString();
            exists = commandeRepositories.existsByAnneeAndNumeroRepartition(e.getAnnee(), code);
        }

        return code;

    }

    @Override
    public Entreprise getEntrepriseFromSession(HttpSession session) {
        Entreprise entreprise = (Entreprise) session.getAttribute(GlobalFonctions.ENTREPRISE_ACTIF);
        if (entreprise == null) {
            throw new RuntimeException("Aucune entreprise active dans la session");
        }
        return entreprise;

    }

    @Override
    public Mois getMoisFromSession() {
        // Charger les données dynamiques
        int moisActuel = IdleDate.getMonth(new Date());
        int anneeActuelle = IdleDate.getYear(new Date());

        Annee an = anneeRepository.findById(anneeActuelle);
        if (an == null) {
            an = new Annee();
            an.setId(anneeActuelle);
            an.setCode("" + anneeActuelle);
            an.setLibelle("Annee " + anneeActuelle);
            anneeRepository.save(an);
        }

        Optional<Mois> mois = moisRepositories.findOneByAnneeAndNumero(anneeActuelle, moisActuel);
        if (mois.isEmpty()) {
            Mois new_mois=new Mois();
            new_mois.setCode(TypeMois.AVRIL.name());
            new_mois.setMois(TypeMois.AVRIL.toString());
            new_mois.setNumero(4);
            new_mois.setAnnee(an);
           return moisRepositories.save(new_mois);
        }
        //Entreprise entreprise = entrepriseRepositories.findByActif(true);

        // Stocker en session
//    session.setAttribute("mois", mois);
//        Mois mois = (Mois) session.getAttribute(GlobalFonctions.MOIS);
//        if (mois == null) {
//            throw new RuntimeException("Aucun mois actif dans la session");
//        }
        return mois.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Commande approvisionner(Commande commande, Mois mois, Entreprise entreprise) {

        try {
            //on verifoit si le produit a deja ete approvisionner 
            DetailCommandeEntree detailCommandeEntree = null;
            PrixVente prixVente = null;
            PrixAchat prixAchat = null;

            Optional<Commande> lastcommande = commandeRepositories.findLastcaommandeByProduit(commande.getMagasinid(), commande.getProduit(), entreprise);
            Commande cde = null;
            if (lastcommande.isPresent()) {
                cde = lastcommande.get();
                BigDecimal quantiteEntree = cde.getEntreeProduit().add(commande.getQuantite());
                BigDecimal stockFinal = cde.getStockFinalTheorie().add(commande.getQuantite());
                cde.setEntreeProduit(quantiteEntree);
                cde.setPrixAchat(commande.getPrixAchat());
                cde.setPrixVente(commande.getPrixVente());
                cde.setStockFinalTheorie(stockFinal);
                cde = commandeRepositories.save(cde);

            } else {

                cde = new Commande();
                cde.setDateReception(new Date());
                cde.setMagasinid(commande.getMagasinid());
                cde.setNumeroRepartition(commande.getNumeroRepartition());
                cde.setPrixAchat(commande.getPrixAchat());
                cde.setPrixVente(commande.getPrixVente());
                cde.setProduit(commande.getProduit());
                cde.setEntreprise(entreprise);
                //cde.setUsercreate(commande.getUsercreate());
                cde.setSortiProduit(BigDecimal.ZERO);
                cde.setEntreeProduit(commande.getQuantite());
                cde.setStockFinalTheorie(commande.getQuantite());
                cde = commandeRepositories.save(cde);

            }

            //entrement du mouvement du stock
            detailCommandeEntree = new DetailCommandeEntree();
            detailCommandeEntree.setCommande(cde);
            detailCommandeEntree.setDateenregistrement(new Date());
            detailCommandeEntree.setFournisseur(commande.getFournisseurid());
            detailCommandeEntree.setMois(mois);
            detailCommandeEntree.setPrixachat(commande.getPrixAchat());
            detailCommandeEntree.setPrixvente(commande.getPrixVente());
           // detailCommandeEntree.setp
            detailCommandeEntree.setQuantiteentree(commande.getQuantite());
            detailCommandeEntree.setUsercreate(commande.getUsercreate());
            detailCommandeEntree.setNumerocommande(commande.getNumeroRepartition());

            detailCommandeEntreeRepositories.save(detailCommandeEntree);

            //enregirement des prix de vente et achat pour controler les marche
            //verifions si le dernier prix de vente du produit est le meme
            Optional<PrixVente> ancienPrix = prixVenteRepositories.findLastPrixventeByProduit(commande.getProduit());

            if (ancienPrix.isPresent()) {
                System.out.println("ancien valeutr privente:" + ancienPrix.get().getPrix().intValue());
                System.out.println("nouveau valeutr privente:" + commande.getPrixVente().intValue());

                boolean resultat = ancienPrix.get().getPrix().intValue() == (commande.getPrixVente().intValue());
                if (resultat == Boolean.FALSE) {
                    prixVente = new PrixVente();
                    prixVente.setProduit(cde.getProduit());
                    prixVente.setDatedebut(new Date());
                    prixVente.setPrix(cde.getPrixVente());
                    //prixVente.s
                    prixVente.setUsercreat(commande.getUsercreate());
                    // if (ancienPrix.isPresent()) {
                    PrixVente arretAncienPrix = ancienPrix.get();
                    arretAncienPrix.setDatefin(new Date());
                    prixVenteRepositories.save(arretAncienPrix);
                    //}
                    prixVenteRepositories.save(prixVente);
                }

            } else if (ancienPrix.isEmpty()) {

                prixVente = new PrixVente();
                prixVente.setProduit(cde.getProduit());
                prixVente.setDatedebut(new Date());
                prixVente.setPrix(cde.getPrixVente());
                prixVente.setUsercreat(commande.getUsercreate());

                prixVenteRepositories.save(prixVente);

            }
//            boolean doitCreerNouveauPrix = ancienPrix.isEmpty()
//                    || !ancienPrix.get().getPrix().equals(cde.getPrixVente());
//
//            if (doitCreerNouveauPrix) {
//                // Si un ancien prix existe, on le clôture
//                ancienPrix.ifPresent(ancien -> {
//                    ancien.setDatefin(new Date());
//                    prixVenteRepositories.save(ancien);
//                });
//
//                // Création du nouveau prix
//                PrixVente nouveauPrix = new PrixVente();
//                nouveauPrix.setProduit(cde.getProduit());
//                nouveauPrix.setDatedebut(new Date());
//                nouveauPrix.setPrix(cde.getPrixVente());
//                nouveauPrix.setUsercreat(commande.getUsercreate());
//
//                prixVenteRepositories.save(nouveauPrix);
//            }
            //verifions le dernier prix d achat du produit 
            Optional<PrixAchat> ancienPrixAchat = prixAchatRepositories.findLastPrixAchatByProduit(commande.getProduit());

//            doitCreerNouveauPrix = ancienPrixAchat.isEmpty()
//                    || !ancienPrixAchat.get().getPrix().equals(cde.getPrixAchat());
//            if (doitCreerNouveauPrix) {
//                // Si un ancien prix existe, on le clôture
//                ancienPrixAchat.ifPresent(ancien -> {
//                    ancien.setDatefin(new Date());
//                    prixAchatRepositories.save(ancien);
//                });
//
//                // Création du nouveau prix
//                prixAchat = new PrixAchat();
//                prixAchat.setProduit(cde.getProduit());
//                prixAchat.setDatedebut(new Date());
//                prixAchat.setPrix(commande.getPrixAchat());
//                prixAchat.setUsercreat(commande.getUsercreate());;
//
//                prixAchatRepositories.save(prixAchat);
//            }
            if (ancienPrixAchat.isPresent()) {

                System.out.println("ancien valeutr prixachat:" + ancienPrixAchat.get().getPrix());
                System.out.println("nouveau valeutr prixachat:" + commande.getPrixAchat());

                Boolean resul = ancienPrixAchat.get().getPrix().intValue() == (commande.getPrixAchat().intValue());
                if (resul == Boolean.FALSE) {
                    prixAchat = new PrixAchat();
                    prixAchat.setProduit(cde.getProduit());
                    prixAchat.setDatedebut(new Date());
                    prixAchat.setPrix(commande.getPrixAchat());
                    prixAchat.setUsercreat(commande.getUsercreate());
                    //if (ancienPrixAchat.isPresent()) {
                    PrixAchat arretAncienPrixAchat = ancienPrixAchat.get();
                    arretAncienPrixAchat.setDatefin(new Date());
                    prixAchatRepositories.save(arretAncienPrixAchat);
                    //}
                    prixAchatRepositories.save(prixAchat);
                }

            } else if (ancienPrixAchat.isEmpty()) {
                prixAchat = new PrixAchat();
                prixAchat.setProduit(cde.getProduit());
                prixAchat.setDatedebut(new Date());
                prixAchat.setPrix(commande.getPrixAchat());
                prixAchat.setUsercreat(commande.getUsercreate());

                prixAchatRepositories.save(prixAchat);
            }
            log.info("Début approvisionnement - Produit: {}, Quantité: {}");

            log.info("Approvisionnement réussi - ID: {}", cde.getId());
            cde.setQuantite(commande.getQuantite());
            cde.setUsercreate(commande.getUsercreate());

            return cde;

        } catch (Exception e) {
            log.error("Erreur lors de l'approvisionnement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'approvisionnement: " + e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    // Un article fraichement approvisionne doit apparaitre immediatement en
    // caisse - sans ceci, le cache Caffeine "topArticles" (10 min, voir
    // BarcodeService.getTopArticles) continue de servir l'ancienne liste et
    // le nouvel article reste invisible en Vente Articles jusqu'a expiration.
    @CacheEvict(value = {"topArticles", "produits", "articleAutocomplete"}, allEntries = true)
    public void addProduitByPointVente(Commande commande, Boutique boutique, Entreprise entreprise, Mois mois, Magasin depot, Barcodeproduit barcodeProduit) {
        try {
// Gestion du code-barres
//            if (barcodeProduit != null && barcodeProduit.getCodeBard() != null) {
//                handleBarcodeProduit(barcodeProduit, commande.getProduit());
//            
            DetailCommandeSortie detailCommandeSortie = null;
            Optional<PointVente> pointVente = pointVenteRepositories.findByProduitAndBoutiqueAndEntreprise(commande.getProduit(), boutique, entreprise);
            //entrement du mouvement du stock
            detailCommandeSortie = new DetailCommandeSortie();
            detailCommandeSortie.setCommande(commande);
            detailCommandeSortie.setDatesortie(new Date());

            detailCommandeSortie.setMois(mois);
            detailCommandeSortie.setPrixachat(commande.getPrixAchat());
            detailCommandeSortie.setPrixvente(commande.getPrixVente());
            detailCommandeSortie.setQuantitsortie(commande.getQuantite());
            detailCommandeSortie.setUsercreate(commande.getUsercreate());
            detailCommandeSortieRepositories.save(detailCommandeSortie);
            if (pointVente.isPresent()) {
                PointVente lastpoinVente = pointVente.get();
                BigDecimal stockFinal = lastpoinVente.getStockFinalTheorie().add(commande.getStockFinalTheorie());
                BigDecimal stockEntree = lastpoinVente.getEntreeProduit().add(commande.getStockFinalTheorie());
                BigDecimal stockSortie = commande.getSortiProduit().add(commande.getStockFinalTheorie());
                //BigDecimal stockFinalCommande = commande.getStockFinalTheorie().subtract(commande.getQuantite());

                // enregistrer les details sortie 
//                //entrement du mouvement du stock
//                detailCommandeSortie = new DetailCommandeSortie();
//                detailCommandeSortie.setCommande(commande);
//                detailCommandeSortie.setDatesortie(new Date());
//
//                detailCommandeSortie.setMois(mois);
//                detailCommandeSortie.setPrixachat(commande.getPrixAchat());
//                detailCommandeSortie.setPrixvente(commande.getPrixVente());
//                detailCommandeSortie.setQuantitsortie(commande.getQuantite());
//                detailCommandeSortie.setUsercreate(commande.getUsercreate());
                // mise a jour commande 
                commande.setSortiProduit(stockSortie);
                commande.setStockFinalTheorie(BigDecimal.ZERO);
                commandeRepositories.save(commande);
                // mise a jour au point de vente 

                lastpoinVente.setEntreeProduit(stockEntree);
                lastpoinVente.setStockFinalTheorie(stockFinal);
                pointVenteRepositories.save(lastpoinVente);

//                detailCommandeSortieRepositories.save(detailCommandeSortie);
                //gestion de s prix articles 
                // le prixArticles existemps
                Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(entreprise, lastpoinVente.getProduit(),lastpoinVente.getBoutique().getId());
                if (pa.isPresent()) {
                    PrixArticles last = pa.get();
                    last.setActif(true);
                    last.setPrixVenteNet(commande.getPrixVente());
                    if (last.getTva()==null || last.getTva()==BigDecimal.ZERO) {
                        last.setPrixVenteTTC(commande.getPrixVente());
                    }else{
                        BigDecimal prcent=BigDecimal.valueOf(100.0);
                        BigDecimal prix_tva=last.getPrixVenteNet().add((last.getPrixVenteNet().multiply(last.getTva()).divide(prcent)));
                        last.setPrixVenteTTC(prix_tva);
                    }
                   
                    prixArticlesRepositories.save(last);

                    return;
                }

            }

//ici ont creer le point de vente pour la premier fois
            PointVente newpointVente = new PointVente();
            newpointVente.setBoutique(boutique);
            newpointVente.setDateReception(new Date());
            newpointVente.setDepotId(commande.getMagasinid());
            newpointVente.setEntreeProduit(commande.getQuantite());
            // Premiere reception pour ce produit dans cette boutique : le stock
            // avant reception est bien zero (jamais mis a zero explicitement
            // avant, ce qui laissait le champ null -> NaN a l'affichage cote
            // Gestion Approvisionnement, entreeProduit + stockInitial).
            newpointVente.setStockInitial(BigDecimal.ZERO);
            newpointVente.setStockFinalTheorie(commande.getQuantite());
            newpointVente.setSortiProduit(BigDecimal.ZERO);
            newpointVente.setEntreprise(entreprise);
            newpointVente.setProduit(commande.getProduit());
            // mise a jour commande 
            commande.setSortiProduit(commande.getQuantite());
            commande.setStockFinalTheorie(BigDecimal.ZERO);
            commandeRepositories.save(commande);
            PointVente savePointVente = pointVenteRepositories.save(newpointVente);

// creation du prix articles
            PrixArticles newPrixArticles = new PrixArticles();
            newPrixArticles.setActif(true);
            newPrixArticles.setDateCreation(new Date());
            newPrixArticles.setPointVente(newpointVente);
            newPrixArticles.setEntreprise(entreprise);
            newPrixArticles.setPrixVenteNet(commande.getPrixVente());
            PrixArticles prixArticleSave = prixArticlesRepositories.save(newPrixArticles);

            // Gestion du code-barres
            if (barcodeProduit != null && barcodeProduit.getCodeBard() != null) {
                handleBarcodeProduit(barcodeProduit, prixArticleSave);
            }

            log.info("Ajout des produits au point de vente terminé - Boutique: {}", boutique.getCode());
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout au point de vente: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'ajout au point de vente", e);
        }
    }

    @Override
    public Commande getDernierCommandeByProduitByDepot(Produit produit, Magasin depot) {
        return commandeRepositories.findLastCommandeByProduitAndDepot(produit, depot)
                .orElse(null);

    }

    @Override
    public BigDecimal getDernierPrixAchat(Produit produit) {
        Optional<PrixAchat> pa = prixAchatRepositories.findLastPrixAchatByProduit(produit);
        return pa.isEmpty() == true ? BigDecimal.ZERO : pa.get().getPrix();

    }

    @Override
    public BigDecimal getDernierPrixVente(Produit produit) {
        Optional<PrixVente> pv = prixVenteRepositories.findLastPrixventeByProduit(produit);
        return pv.isEmpty() == true ? BigDecimal.ZERO : pv.get().getPrix();
    }

    @Override
    public Long count() {
        return commandeRepositories.count();
    }

    @Override
    public boolean existsByCode(String code) {
        return commandeRepositories.existsByNumeroRepartition(code);
    }

    private void validateCommande(Commande commande) {
        if (commande.getProduit() == null) {
            throw new IllegalArgumentException("Produit obligatoire");
        }
        if (commande.getQuantite() == null || commande.getQuantite().intValue() <= 0) {
            throw new IllegalArgumentException("Quantité doit être positive");
        }
        if (commande.getPrixAchat() == null || commande.getPrixAchat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prix d'achat doit être positif");
        }
        if (commande.getPrixVente() == null || commande.getPrixVente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prix de vente doit être positif");
        }
    }

    private BigDecimal calculateStockFinal(Commande commande) {
        BigDecimal stockActuel = getStockActuel(commande.getProduit(), commande.getMagasinid());
        return stockActuel.add((commande.getQuantite()));
    }

    private BigDecimal getStockActuel(Produit produit, Magasin depot) {
        Commande derniere = getDernierCommandeByProduitByDepot(produit, depot);
        return derniere != null && derniere.getStockFinalTheorie() != null
                ? derniere.getStockFinalTheorie() : BigDecimal.ZERO;
    }

    private void calculatePrixTTC(Commande commande, Entreprise entreprise) {
        // Calcul du prix TTC basé sur les taxes
        BigDecimal prixHT = commande.getPrixVente();
        BigDecimal tauxTVA = getTauxTVA(commande.getProduit(), entreprise);

        BigDecimal tva = prixHT.multiply(tauxTVA).divide(BigDecimal.valueOf(100));
        BigDecimal prixTTC = prixHT.add(tva);

    }

    private BigDecimal getTauxTVA(Produit produit, Entreprise entreprise) {
        // Logique pour récupérer le taux de TVA
        // À adapter selon votre modèle de données
        return BigDecimal.valueOf(19.25); // Exemple : 19.25% au Cameroun
    }

    private void updateStockPointVente(PointVente pointVente, Commande commande) {
        if (pointVente.getStockFinalTheorie() == null) {
            pointVente.setStockFinalTheorie(commande.getQuantite());
        } else {
            pointVente.setStockFinalTheorie(pointVente.getStockFinalTheorie().add(commande.getQuantite()));
        }

        if (pointVente.getEntreeProduit() == null) {
            pointVente.setEntreeProduit(commande.getQuantite());
        } else {
            pointVente.setEntreeProduit(pointVente.getEntreeProduit().add(commande.getQuantite()));
        }

        pointVenteRepositories.save(pointVente);
    }

    private void createPrixArticle(PointVente pointVente, Commande commande, Entreprise entreprise) {
        PrixArticles prixArticle = new PrixArticles();
        prixArticle.setPointVente(pointVente);
        prixArticle.setPrixVenteNet(commande.getPrixVente());
        //prixArticle.setPrixVenteTTC(commande.getPrixVenteTTC());
        prixArticle.setQuantite(commande.getQuantite().toBigInteger());
        prixArticle.setActif(true);
        prixArticle.setEntreprise(entreprise);

        prixArticlesRepositories.save(prixArticle);
    }

    private void handleBarcodeProduit(Barcodeproduit barcodeProduit, PrixArticles prixArticles) {
        // Associer le code-barres au produit
        barcodeProduit.setPrixArticles(prixArticles);
        // Logique de sauvegarde du code-barres
        barcodeproduitRepositories.save(barcodeProduit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String approvisionnement(Commande c, Mois m, Entreprise e) {
        try {
            Commande currentCommande = null;
            BigDecimal quantite = BigDecimal.ZERO;

            Commande existingCommande = commandeRepositories
                    .findLastCommandeByProduitAndDepotAndMois(c.getProduit(), c.getMagasinid())
                    .orElse(null);

            if (existingCommande == null) {
                if (m.getNumero() == 1) {
                    // Gestion début d'année - recherche commande décembre année précédente
                    Optional<Commande> commandeDecembre = getCommandeDecembrePrecedent(c, e, m);

                    if (commandeDecembre == null) {
                        currentCommande = createNewCommande(c, m, BigDecimal.ZERO);
                    } else {
                        quantite = commandeDecembre.get().getStockFinalTheorie();
                        currentCommande = createNewCommande(c, m, quantite);
                    }
                } else {
                    // Gestion mois suivants
                    Mois ancienMois = moisRepositories.findByEntrepriseAndNumero(e.getAnnee().getId(), m.getNumero() - 1)
                            .stream()
                            .findFirst()
                            .orElse(null);
                    Commande ancienCommande = null;

                    if (ancienMois != null) {
                        ancienCommande = commandeRepositories
                                .findLastCommandeByProduitAndDepotAndMois(c.getProduit(), c.getMagasinid())
                                .orElse(null);
                    }

                    if (ancienCommande == null) {
                        BigDecimal stockInitial = (c.getStockInitial() != null && c.getStockInitial().intValue() != 0)
                                ? c.getStockInitial() : BigDecimal.ZERO;
                        currentCommande = createNewCommande(c, m, stockInitial);
                    } else {
                        quantite = ancienCommande.getStockFinalTheorie();
                        currentCommande = createNewCommande(c, m, quantite);
                    }
                }
                commandeRepositories.save(currentCommande);
            } else {
                // Commande existe déjà pour ce mois
                Commande commandeMois = createAdditionalCommande(c, m, existingCommande.getStockFinalTheorie());
                commandeRepositories.save(commandeMois);
            }

            log.info("Approvisionnement réussi pour le produit: {}", c.getProduit().getReference());
            return Success.OPERATION_SUCCESS.toString();

        } catch (Exception ex) {
            log.error("Erreur lors de l'approvisionnement: {}", ex.getMessage(), ex);
            throw new RuntimeException(com.mproduits.exceptions.Error.OPERATION_FAILED.toString(), ex);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateApprovisionnement(Commande c) throws GlobalException {
        try {
            commandeRepositories.save(c);
            log.info("Mise à jour de l'approvisionnement réussie pour la commande: {}", c.getId());
        } catch (Exception ex) {
            log.error("Erreur lors de la mise à jour de l'approvisionnement: {}", ex.getMessage(), ex);
            throw new GlobalException("Erreur lors de la mise à jour....." + ex);
        }

    }

//    @Override
//   
//      public String livraisonCommande(Livraison_old l, Commande c, PrixClient p, Entreprise e) throws GlobalException {
//        try {
    ////            BigInteger quantiteLivrer = l.getQuantite();
////            
////            if (quantiteLivrer.intValue() > c.getStockFinal().intValue()) {
////                throw new GlobalException(Error.QUANTITE_INSUFISSANT.toString());
////            }
////            
////            // Mise à jour des sorties de la commande
////            quantiteLivrer = l.getQuantite().add(c.getSortir());
////            c.setSortir(quantiteLivrer);
////            
////            // Création de la livraison
////            Livraison nouvelleLivraison = Livraison.builder()
////                .bc(l.getBc())
////                .dateLivraison(l.getDateLivraison())
////                .libelle(l.getLibelle())
////                .mesure(l.getMesure())
////                .quantite(l.getQuantite())
////                .prixClient(p)
////                .commande(c)
////                .build();
////            
////            commandeRepository.save(c);
////            livraisonRepository.save(nouvelleLivraison);
////            
////            // Création du compte associé
////            Compte compte = Compte.builder()
////                .libelle(l.getLibelle())
////                .entreprise(e)
////                .livraison(nouvelleLivraison)
////                .build();
////            
////            compteRepositories.save(compte);
//            
//            log.info("Livraison_old créée avec succès: {}", nouvelleLivraison.getId());
//            return Success.OPERATION_SUCCESS.toString();
//            
//        } catch (Exception ex) {
//            log.error("Erreur lors de la livraison: {}", ex.getMessage(), ex);
//            throw new GlobalException("Erreur lors de la livraison..." +ex);
//        }
//    
//    }

   
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String addProduitByPointVente(List<Commande> colCommande, Boutique b, Entreprise e, Mois m, Magasin d) {
        try {
            BigDecimal prCent = BigDecimal.valueOf(100.0);
            String erreur = com.mproduits.exceptions.Error.QUANTITE_INSUFISSANT.toString() + "REFERENCE : ";

            for (Commande cde : colCommande) {
                if (cde.getQuantite().intValue() == 0 || cde.getPrixVente().intValue() == 0) {
                    continue;
                }

                // Vérification du stock disponible
                Commande commandeStock = commandeRepositories
                        .findLastCommandeByProduitAndDepotAndMois(cde.getProduit(), d)
                        .orElse(null);

                if (commandeStock == null
                        || commandeStock.getStockFinalTheorie().intValue() == 0
                        || commandeStock.getStockFinalTheorie().intValue() < cde.getQuantite().intValue()) {

                    erreur += cde.getProduit().getReference();
                    throw new GlobalException(erreur);
                }

                // Gestion du point de vente
                processPointVente(cde, b, e, m, d, commandeStock, prCent);

                // Mise à jour du stock de la commande
                updateCommandeStock(commandeStock, cde);
            }

            log.info("Produits ajoutés au point de vente avec succès");
            return Success.OPERATION_SUCCESS.toString();

        } catch (Exception ex) {
            log.error("Erreur lors de l'ajout des produits au point de vente: {}", ex.getMessage(), ex);
            throw ex;
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteManyProduit(List<Produit> produits) {
        try {
            produits.forEach(produit -> {
                produit.setDeletes(Boolean.TRUE);
                produitRepositories.save(produit);
            });

            log.info("Produits marqués comme supprimés: {}", produits.size());

            return Success.OPERATION_SUCCESS.toString();

        } catch (Exception e) {
            log.error("Erreur lors de la suppression du produit: {}", e.getMessage(), e);
            return com.mproduits.exceptions.Error.DELETE_ERROR.toString();
        }

    }

    @Override
    public String deleteOneProduit(Produit p) {
        try {
            p.setDeletes(Boolean.TRUE);
            p.setDateDelete(new Date());
            produitRepositories.save(p);
            log.info("Produit marqués comme supprimés: {}", p);
            return Success.OPERATION_SUCCESS.toString();

        } catch (Exception e) {
            log.error("Erreur lors de la suppression des produits: {}", e.getMessage(), e);
            return com.mproduits.exceptions.Error.DELETE_ERROR.toString();
        }
    }

    private Optional<Commande> getCommandeDecembrePrecedent(Commande c, Entreprise e, Mois m) {
        EntreprisePK ancienneAnneePK = new EntreprisePK(
                e.getAnnee().getId().intValue() - 1,
                e.getEntreprisePK().getCompagnieId()
        );

        return entrepriseRepositories.findById(ancienneAnneePK)
                .flatMap(ancienneAnnee
                        -> moisRepositories.findByEntrepriseAndNumero(ancienneAnnee.getAnnee().getId(), 12).stream()
                        .flatMap(moisDecembre
                                -> commandeRepositories.findLastCommandeByProduitAndDepotAndMois(
                                c.getProduit(), c.getMagasinid()
                        ).stream()
                        )
                        .findFirst()
                );

    }

    private Commande createNewCommande(Commande source, Mois m, BigDecimal stockInitial) {
        Commande destination = new Commande();

        destination.setStockInitial(stockInitial);
        destination.setEntreeProduit(BigDecimal.ZERO);
        destination.setSortiProduit(BigDecimal.ZERO);
        destination.setProduit(source.getProduit());
        destination.setQuantite(source.getQuantite());
        destination.setMagasinid(source.getMagasinid());
        destination.setFournisseurid(source.getFournisseurid());
        destination.setPrixAchat(source.getPrixAchat());
        destination.setPrixVente(source.getPrixVente());

        destination.setNumeroRepartition(source.getNumeroRepartition());
        destination.setDateReception(source.getDateReception());
        return destination;
//        return Commande.builder()
//                .commande(true)
//                //.debutMois(true)
//                .livraison(false)
//                .stockInitial(stockInitial)
//                .entreeProduit(BigDecimal.ZERO)
//                .soortiProduit(BigDecimal.ZERO)
//                .produitId(source.getProduitId())
//                .quantite(source.getQuantite())
//                .magasinid(source.getMagasinid())
//                .prixAchat(source.getPrixAchat())
//                .prixVente(source.getPrixVente())
//                .moisid(m)
//                .numeroRepartition(source.getNumeroRepartition())
//                .dateReception(source.getDateReception())
//                .build();
    }

    private Commande createAdditionalCommande(Commande source, Mois m, BigDecimal stockInitial) {

        Commande destination = new Commande();

        destination.setStockInitial(stockInitial);
        destination.setEntreeProduit(BigDecimal.ZERO);
        destination.setSortiProduit(BigDecimal.ZERO);
        destination.setProduit(source.getProduit());
        destination.setQuantite(source.getQuantite());
        destination.setMagasinid(source.getMagasinid());
        destination.setFournisseurid(source.getFournisseurid());
        destination.setPrixAchat(source.getPrixAchat());
        destination.setPrixVente(source.getPrixVente());

        destination.setNumeroRepartition(source.getNumeroRepartition());
        destination.setDateReception(source.getDateReception());
        return destination;
//        return Commande.builder()
//                .commande(true)
//                .livraison(false)
//                // .debutMois(false)
//                .stockInitial(stockInitial)
//                .soortiProduit(BigDecimal.ZERO)
//                .entreeProduit(BigDecimal.ZERO)
//                .moisid(m)
//                .produitId(source.getProduitId())
//                .quantite(source.getQuantite())
//                .magasinid(source.getMagasinid())
//                .prixAchat(source.getPrixAchat())
//                .prixVente(source.getPrixVente())
//                .numeroRepartition(source.getNumeroRepartition())
//                .dateReception(source.getDateReception())
//                .build();
    }

    private void processPointVente(Commande cde, Boutique b, Entreprise e, Mois m, Magasin d,
            Commande commandeStock, BigDecimal prCent) {
        PointVente pointVenteExistant = pointVenteRepositories
                .findLastByProduitAndBoutiqueAndEntrepriseAndMois(cde.getProduit(), b, e.getAnnee())
                .orElse(null);

        if (pointVenteExistant == null) {
            createNewPointVente(cde, b, e, m, d, prCent);
        } else {
            updateExistingPointVente(cde, pointVenteExistant, e, prCent);
        }
    }

    private void createNewPointVente(Commande cde, Boutique b, Entreprise e, Mois m, Magasin d, BigDecimal prCent) {
        // Gestion du stock initial selon le mois et l'historique
        BigDecimal stockInitial = getStockInitialPointVente(cde, b, e, m);
        BigDecimal stockFinal = stockInitial.add(cde.getQuantite());
        PointVente nouveauPointVente = new PointVente();
        nouveauPointVente.setBoutique(b);
        nouveauPointVente.setDateReception(new Date());
        nouveauPointVente.setEntreeProduit(cde.getQuantite());
        nouveauPointVente.setSortiProduit(BigDecimal.ZERO);
        nouveauPointVente.setStockInitial(stockInitial);
        nouveauPointVente.setStockFinalTheorie(stockFinal);
        nouveauPointVente.setProduit(cde.getProduit());
        nouveauPointVente.setDepotId(d);

//        PointVente nouveauPointVente = PointVente.builder()
//                .boutiqueid(b)
//                .dateReception(new Date())
//                .entreeProduit(cde.getQuantite())
//                .soortiProduit(BigDecimal.ZERO)
//                .stockInitial(stockInitial)
//                .stockFinalTheorie(stockFinal)
//                .fondCuveEntre(BigDecimal.ZERO)
//                .fondCuveSorti(BigDecimal.ZERO)
//                .fondCuveEntre(BigDecimal.ZERO)
//                .produitId(cde.getProduitId())
//                .depotId(d)
//                .moisid(m)
//                .build();
        pointVenteRepositories.save(nouveauPointVente);

        // Création du prix associé
        createPrixArticles(cde, e, nouveauPointVente, prCent);
    }

    private BigDecimal getStockInitialPointVente(Commande cde, Boutique b, Entreprise e, Mois m) {
        if (m.getNumero() == 1) {
            // Recherche en décembre de l'année précédente
            return getStockFromPreviousYear(cde, b, e)
                    .map(PointVente::getStockFinalTheorie)
                    .orElse(BigDecimal.ZERO);
        } else {
            // Recherche au mois précédent
            return moisRepositories.findByEntrepriseAndNumero(e.getAnnee().getId(), m.getNumero() - 1).stream()
                    .flatMap(ancienMois -> pointVenteRepositories.findLastByProduitAndBoutiqueAndEntrepriseAndMois(
                    cde.getProduit(), b, e.getAnnee()).stream()
                    )
                    .map(PointVente::getStockFinalTheorie)
                    .findFirst() // ✅ transforme en Optional<BigDecimal>
                    .orElse(BigDecimal.ZERO); // ✅ fallback si rien trouvé

        }
    }

    private Optional<PointVente> getStockFromPreviousYear(Commande cde, Boutique b, Entreprise e) {
        EntreprisePK ancienneAnneePK = new EntreprisePK(
                e.getAnnee().getId().intValue() - 1,
                e.getEntreprisePK().getCompagnieId()
        );

        return entrepriseRepositories.findById(ancienneAnneePK)
                .flatMap(ancienneAnnee
                        -> moisRepositories.findByEntrepriseAndNumero(ancienneAnnee.getAnnee().getId(), 12).stream()
                        .flatMap(moisDecembre
                                -> pointVenteRepositories.findLastByProduitAndBoutiqueAndEntrepriseAndMois(
                                cde.getProduit(), b, ancienneAnnee.getAnnee()
                        ).stream()
                        )
                        .findFirst() // ou collect, selon votre besoin
                );

    }

    private void updateExistingPointVente(Commande cde, PointVente pointVente, Entreprise e, BigDecimal prCent) {
        BigDecimal nouvelleEntree = pointVente.getEntreeProduit().add(cde.getQuantite());
        BigDecimal nouveauStockFinal = pointVente.getStockInitial()
                .add(nouvelleEntree)
                .subtract(pointVente.getSortiProduit());

        pointVente.setEntreeProduit(nouvelleEntree);
        pointVente.setStockFinalTheorie(nouveauStockFinal);
        pointVenteRepositories.save(pointVente);

        // Mise à jour du prix
        updatePrixArticles(cde, e, pointVente, prCent);
    }

    private void createPrixArticles(Commande cde, Entreprise e, PointVente pv, BigDecimal prCent) {

        PrixArticles prixArticles = new PrixArticles();
        prixArticles.setActif(true);
        prixArticles.setDateCreation(new Date());
        prixArticles.setEntreprise(e);
        prixArticles.setPointVente(pv);
        prixArticles.setPrixVenteNet(cde.getPrixVente());

//        PrixArticles prixArticles = PrixArticles.builder()
//                .actif(true)
//                .dateCreation(new Date())
//                .entreprise(e)
//                .pointVenteId(pv)
//                .prixVenteNet(cde.getPrixVente())
//                .remise(cde.getRemise())
//                .tva(cde.getTva())
//                .prixVenteTTC(prixTTC)
//                .build();
        prixArticlesRepositories.save(prixArticles);
    }

    private void updatePrixArticles(Commande cde, Entreprise e, PointVente pv, BigDecimal prCent) {
        prixArticlesRepositories.findActiveByEntrepriseAndPointVente(e, pv)
                .ifPresent(prixArticles -> {

                    prixArticles.setPrixVenteNet(cde.getPrixVente());

                    prixArticlesRepositories.save(prixArticles);
                });
    }

    private void updateCommandeStock(Commande commandeStock, Commande nouvelleCommande) {
        BigDecimal nouvelleSortie = (nouvelleCommande.getQuantite().add(commandeStock.getSortiProduit()));
        BigDecimal nouveauStockFinal = commandeStock.getStockInitial()
                .add(commandeStock.getEntreeProduit())
                .subtract(nouvelleSortie);

        commandeStock.setSortiProduit(nouvelleSortie);
        commandeStock.setStockFinalTheorie(nouveauStockFinal);
        commandeRepositories.save(commandeStock);
    }

    @Override
    public Page<PrixArticles> getPrixArticles(int page, int size, String search, Long boutiqueid) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        if (search != null && !search.isEmpty()) {
            return prixArticlesRepositories.findProduitLibelleByEntrepriseWithFilter(e, search, pageable, boutiqueid);
        } else {
            return prixArticlesRepositories.findAllByEntrepriseProduitActif(e, Boolean.TRUE, pageable, boutiqueid);
        }
    }

    @Override
    public Page<PrixArticles> getPrixArticlesByMagasin(int page, int size, String search, Long magasinid) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        if (search != null && !search.isEmpty()) {
            return prixArticlesRepositories.findProduitLibelleByEntrepriseWithFilterMagasin(e, search, pageable, magasinid);
        } else {
            return prixArticlesRepositories.findAllByEntrepriseProduitActifMagasin(e, Boolean.TRUE, pageable, magasinid);
        }
    }

    @Override
    public List<PrixArticles> getPrixArticlesFilster(String search, Long boutiqueid) {
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        if (search != null && !search.trim().isEmpty()) {
            return prixArticlesRepositories.findProduitLibelleByEntrepriseWithFilter(e, search, boutiqueid);
        }
        return prixArticlesRepositories.findAllByEntrepriseProduitActif(e, Boolean.TRUE, boutiqueid);
    }

    @Override
    public BigDecimal stockProduit(Long produitId, Long boutiqueid) {
        Optional<Produit> produit = produitRepositories.findByIdAndCompagnie_Id(produitId, tenantContext.currentCompagnieId());
        if (produit.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        if (boutique.isEmpty()) {

            return stockDepot(produitId, boutiqueid);
        }
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        Optional<BigDecimal> stock = pointVenteRepositories.stockFinalProduit(produit.get(), boutique.get(), entreprise);

        if (stock.isPresent()) {
            return stock.get();
        }

        return BigDecimal.ZERO;

    }

    @Override
    public BigDecimal stockDepot(Long produitId, Long boutiqueid) {

        Optional<Produit> produit = produitRepositories.findByIdAndCompagnie_Id(produitId, tenantContext.currentCompagnieId());
        if (produit.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Optional<Magasin> boutique = magasinRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        if (boutique.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        Optional<BigDecimal> stock = commandeRepositories.stockFinalDepot(boutique.get(), produit.get(), entreprise);

        if (stock.isPresent()) {
            return stock.get();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<PrixArticles> alertstock(BigDecimal seuilStock) {
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        return prixArticlesRepositories.findAllByEntrepriseProduitActifWithSeuilStock(e, Boolean.TRUE, seuilStock);
    }

}
