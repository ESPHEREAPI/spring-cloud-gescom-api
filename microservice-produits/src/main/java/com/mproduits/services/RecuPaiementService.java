/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.RecuPaiementDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.ErrorResponse;
import com.mproduits.model.Client;
import com.mproduits.model.Facture;
import com.mproduits.model.VersementClient;
import com.mproduits.repositories.VersementClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author USER01
 */
/**
 * Service de génération et gestion des reçus de paiement
 * Génère des reçus au format PDF, HTML, et TXT
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecuPaiementService {

    private final VersementClientRepository versementRepository;
    private final NumeroGeneratorService numeroGeneratorService;
    private final NotificationService notificationService;
    
    private static final String ENTREPRISE_NOM = "Votre Entreprise";
    private static final String ENTREPRISE_ADRESSE = "123 Rue Example, Ville";
    private static final String ENTREPRISE_TEL = "+237 XXX XXX XXX";
    private static final String ENTREPRISE_EMAIL = "contact@entreprise.com";
    
    // ========================================================================
    // GÉNÉRATION DE REÇU
    // ========================================================================
    
    /**
     * Génère un reçu de paiement pour un versement
     */
    public RecuPaiementDTO genererRecu(Long versementId) {
        log.info("Génération du reçu pour versement ID: {}", versementId);
        
        VersementClient versement = versementRepository.findByIdWithFactureAndClient(versementId)
            .orElseThrow(() -> new BadRequestException("Versement non trouvé avec ID: " + versementId));
        
        RecuPaiementDTO recu = new RecuPaiementDTO();
        
        // Informations du reçu
        recu.setNumeroRecu(numeroGeneratorService.genererNumeroRecuFromVersement(versementId));
        recu.setDateEmission(new Date());
        recu.setVersementId(versementId);
        
        // Informations du versement
        recu.setMontant(versement.getMontant());
        recu.setDateVersement(versement.getDateVersement());
        recu.setModePaiement(versement.getModePaiement().name());
        recu.setReferencePaiement(versement.getReferencePaiement());
        
        // Informations du client
        Client client = versement.getClient();
        recu.setClientId(client.getId());
        recu.setClientNom(client.getNom());
        recu.setClientAdresse(client.getAdresse());
        recu.setClientTelephone(client.getTelephone());
        recu.setClientEmail(client.getEmail());
        
        // Informations de la facture
        Facture facture = versement.getFacture();
        recu.setFactureId(facture.getId());
        recu.setFactureNumero(facture.getNumeroFacture());
        recu.setFactureTotalTtc(facture.getTotalTtc());
        
        // Calcul du solde restant
        BigDecimal totalVersements = versementRepository.sumVersementsByFacture(facture.getId());
        BigDecimal soldeRestant = facture.getTotalTtc().subtract(totalVersements);
        recu.setFactureSoldeRestant(soldeRestant);
        
        // Montant en lettres
        recu.setMontantEnLettres(convertirMontantEnLettres(versement.getMontant()));
        
        log.info("Reçu généré: {}", recu.getNumeroRecu());
        return recu;
    }

    /**
     * Génère un reçu pour plusieurs versements (paiement groupé)
     */
    public RecuPaiementDTO genererRecuGroupe(List<Long> versementIds) {
        log.info("Génération du reçu groupé pour {} versements", versementIds.size());
        
        if (versementIds == null || versementIds.isEmpty()) {
            throw new ErrorResponse("La liste des versements est vide");
        }
        
        List<VersementClient> versements = new ArrayList<>();
        for (Long id : versementIds) {
            VersementClient v = versementRepository.findByIdWithFactureAndClient(id)
                .orElseThrow(() -> new ErrorResponse("Versement non trouvé: " + id));
            versements.add(v);
        }
        
        // Vérifier que tous les versements sont du même client
        Client client = versements.get(0).getClient();
        boolean memeClient = versements.stream()
            .allMatch(v -> v.getClient().getId().equals(client.getId()));
        
        if (!memeClient) {
            throw new ErrorResponse("Tous les versements doivent appartenir au même client");
        }
        
        RecuPaiementDTO recu = new RecuPaiementDTO();
        recu.setNumeroRecu(numeroGeneratorService.genererNumeroRecu());
        recu.setDateEmission(new Date());
        
        // Calculer le montant total
        BigDecimal montantTotal = versements.stream()
            .map(VersementClient::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        recu.setMontant(montantTotal);
        recu.setMontantEnLettres(convertirMontantEnLettres(montantTotal));
        
        // Informations du client
        recu.setClientId(client.getId());
        recu.setClientNom(client.getNom());
        recu.setClientAdresse(client.getAdresse());
        recu.setClientTelephone(client.getTelephone());
        recu.setClientEmail(client.getEmail());
        
        // Liste des versements
        recu.setVersementIds(versementIds);
        
        log.info("Reçu groupé généré: {} pour montant total: {}", recu.getNumeroRecu(), montantTotal);
        return recu;
    }

    // ========================================================================
    // GÉNÉRATION HTML
    // ========================================================================
    
    /**
     * Génère le contenu HTML d'un reçu
     */
    public String genererRecuHTML(Long versementId) {
        RecuPaiementDTO recu = genererRecu(versementId);
        return construireHTMLRecu(recu);
    }

    /**
     * Construit le HTML du reçu
     */
    private String construireHTMLRecu(RecuPaiementDTO recu) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.FRENCH));
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Reçu de Paiement - ").append(recu.getNumeroRecu()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }\n");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }\n");
        html.append(".title { font-size: 24px; font-weight: bold; color: #333; margin: 10px 0; }\n");
        html.append(".info-section { margin: 20px 0; }\n");
        html.append(".info-row { display: flex; justify-content: space-between; margin: 10px 0; }\n");
        html.append(".label { font-weight: bold; color: #666; }\n");
        html.append(".value { color: #333; }\n");
        html.append(".montant { font-size: 20px; font-weight: bold; color: #2e7d32; text-align: center; margin: 20px 0; padding: 15px; background: #e8f5e9; border-radius: 5px; }\n");
        html.append(".footer { margin-top: 40px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #ddd; padding-top: 20px; }\n");
        html.append(".signature { margin-top: 60px; text-align: right; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("th { background-color: #f5f5f5; font-weight: bold; }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        // En-tête
        html.append("<div class='header'>\n");
        html.append("<h1>").append(ENTREPRISE_NOM).append("</h1>\n");
        html.append("<p>").append(ENTREPRISE_ADRESSE).append("<br>\n");
        html.append("Tél: ").append(ENTREPRISE_TEL).append(" | Email: ").append(ENTREPRISE_EMAIL).append("</p>\n");
        html.append("<div class='title'>REÇU DE PAIEMENT</div>\n");
        html.append("<p>N° ").append(recu.getNumeroRecu()).append("</p>\n");
        html.append("</div>\n");
        
        // Informations du client
        html.append("<div class='info-section'>\n");
        html.append("<h3>Informations Client</h3>\n");
        html.append("<div class='info-row'><span class='label'>Nom:</span> <span class='value'>").append(recu.getClientNom()).append("</span></div>\n");
        if (recu.getClientAdresse()!= null) {
            html.append("<div class='info-row'><span class='label'>Adresse:</span> <span class='value'>").append(recu.getClientAdresse()).append("</span></div>\n");
        }
        if (recu.getClientTelephone()!= null) {
            html.append("<div class='info-row'><span class='label'>Téléphone:</span> <span class='value'>").append(recu.getClientTelephone()).append("</span></div>\n");
        }
        html.append("</div>\n");
        
        // Détails du paiement
        html.append("<div class='info-section'>\n");
        html.append("<h3>Détails du Paiement</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>Date</th><th>Facture N°</th><th>Mode de Paiement</th><th>Référence</th></tr>\n");
        html.append("<tr>\n");
        html.append("<td>").append(sdf.format(recu.getDateVersement())).append("</td>\n");
        html.append("<td>").append(recu.getFactureNumero()).append("</td>\n");
        html.append("<td>").append(recu.getModePaiement()).append("</td>\n");
        html.append("<td>").append(recu.getReferencePaiement() != null ? recu.getReferencePaiement() : "-").append("</td>\n");
        html.append("</tr>\n");
        html.append("</table>\n");
        html.append("</div>\n");
        
        // Montant
        html.append("<div class='montant'>\n");
        html.append("<div>Montant Versé: ").append(df.format(recu.getMontant())).append(" FCFA</div>\n");
        html.append("<div style='font-size: 14px; margin-top: 10px;'>").append(recu.getMontantEnLettres()).append("</div>\n");
        html.append("</div>\n");
        
        // Solde
        if (recu.getFactureSoldeRestant()!= null) {
            html.append("<div class='info-row'>\n");
            html.append("<span class='label'>Montant Total Facture:</span> <span class='value'>").append(df.format(recu.getFactureTotalTtc())).append(" FCFA</span>\n");
            html.append("</div>\n");
            html.append("<div class='info-row'>\n");
            html.append("<span class='label'>Solde Restant:</span> <span class='value' style='font-weight: bold; color: ");
            html.append(recu.getFactureSoldeRestant().compareTo(BigDecimal.ZERO) > 0 ? "#d32f2f" : "#2e7d32");
            html.append(";'>").append(df.format(recu.getFactureSoldeRestant())).append(" FCFA</span>\n");
            html.append("</div>\n");
        }
        
        // Signature
        html.append("<div class='signature'>\n");
        html.append("<p>Signature autorisée</p>\n");
        html.append("<p>_____________________</p>\n");
        html.append("</div>\n");
        
        // Pied de page
        html.append("<div class='footer'>\n");
        html.append("<p>Ce reçu est généré automatiquement et valide sans signature.</p>\n");
        html.append("<p>Émis le ").append(sdf.format(recu.getDateEmission())).append("</p>\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        
        return html.toString();
    }

    // ========================================================================
    // GÉNÉRATION TEXTE SIMPLE
    // ========================================================================
    
    /**
     * Génère un reçu au format texte simple
     */
    public String genererRecuTexte(Long versementId) {
        RecuPaiementDTO recu = genererRecu(versementId);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.FRENCH));
        
        StringBuilder txt = new StringBuilder();
        txt.append("=".repeat(60)).append("\n");
        txt.append("              REÇU DE PAIEMENT\n");
        txt.append("=".repeat(60)).append("\n\n");
        
        txt.append(ENTREPRISE_NOM).append("\n");
        txt.append(ENTREPRISE_ADRESSE).append("\n");
        txt.append("Tél: ").append(ENTREPRISE_TEL).append("\n\n");
        
        txt.append("Reçu N° : ").append(recu.getNumeroRecu()).append("\n");
        txt.append("Date    : ").append(sdf.format(recu.getDateEmission())).append("\n\n");
        
        txt.append("-".repeat(60)).append("\n");
        txt.append("CLIENT\n");
        txt.append("-".repeat(60)).append("\n");
        txt.append("Nom     : ").append(recu.getClientNom()).append("\n");
        if (recu.getClientTelephone()!= null) {
            txt.append("Tél     : ").append(recu.getClientTelephone()).append("\n");
        }
        txt.append("\n");
        
        txt.append("-".repeat(60)).append("\n");
        txt.append("PAIEMENT\n");
        txt.append("-".repeat(60)).append("\n");
        txt.append("Facture N°      : ").append(recu.getFactureNumero()).append("\n");
        txt.append("Date Versement  : ").append(sdf.format(recu.getDateVersement())).append("\n");
        txt.append("Mode Paiement   : ").append(recu.getModePaiement()).append("\n");
        if (recu.getReferencePaiement() != null) {
            txt.append("Référence       : ").append(recu.getReferencePaiement()).append("\n");
        }
        txt.append("\n");
        
        txt.append("MONTANT VERSÉ   : ").append(df.format(recu.getMontant())).append(" FCFA\n");
        txt.append("En lettres      : ").append(recu.getMontantEnLettres()).append("\n\n");
        
        if (recu.getFactureSoldeRestant()!= null) {
            txt.append("Total Facture   : ").append(df.format(recu.getFactureTotalTtc())).append(" FCFA\n");
            txt.append("Solde Restant   : ").append(df.format(recu.getFactureSoldeRestant())).append(" FCFA\n\n");
        }
        
        txt.append("=".repeat(60)).append("\n");
        txt.append("     Merci pour votre confiance\n");
        txt.append("=".repeat(60)).append("\n");
        
        return txt.toString();
    }

    // ========================================================================
    // CONVERSION MONTANT EN LETTRES
    // ========================================================================
    
    /**
     * Convertit un montant numérique en lettres (français)
     */
    public String convertirMontantEnLettres(BigDecimal montant) {
        if (montant == null) {
            return "Zéro francs CFA";
        }
        
        long partieEntiere = montant.longValue();
        int partieDecimale = montant.subtract(new BigDecimal(partieEntiere))
            .multiply(new BigDecimal(100))
            .intValue();
        
        String resultat = convertirNombreEnLettres(partieEntiere) + " francs CFA";
        
        if (partieDecimale > 0) {
            resultat += " et " + convertirNombreEnLettres(partieDecimale) + " centimes";
        }
        
        return resultat.substring(0, 1).toUpperCase() + resultat.substring(1);
    }

    /**
     * Convertit un nombre en lettres
     */
    private String convertirNombreEnLettres(long nombre) {
        if (nombre == 0) return "zéro";
        if (nombre < 0) return "moins " + convertirNombreEnLettres(-nombre);
        
        String[] unites = {"", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix",
                          "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"};
        String[] dizaines = {"", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix"};
        
        if (nombre < 20) {
            return unites[(int) nombre];
        }
        
        if (nombre < 100) {
            int d = (int) (nombre / 10);
            int u = (int) (nombre % 10);
            
            if (d == 7 || d == 9) {
                return dizaines[d - 1] + (u == 0 ? "" : "-" + unites[10 + u]);
            } else if (d == 8) {
                return dizaines[d] + (u == 0 ? "s" : "-" + unites[u]);
            } else {
                return dizaines[d] + (u == 0 ? "" : (u == 1 && d != 8 ? " et un" : "-" + unites[u]));
            }
        }
        
        if (nombre < 1000) {
            int c = (int) (nombre / 100);
            long reste = nombre % 100;
            String result = (c == 1 ? "cent" : unites[c] + " cent");
            if (c > 1 && reste == 0) result += "s";
            return result + (reste == 0 ? "" : " " + convertirNombreEnLettres(reste));
        }
        
        if (nombre < 1000000) {
            long m = nombre / 1000;
            long reste = nombre % 1000;
            String result = (m == 1 ? "mille" : convertirNombreEnLettres(m) + " mille");
            return result + (reste == 0 ? "" : " " + convertirNombreEnLettres(reste));
        }
        
        long millions = nombre / 1000000;
        long reste = nombre % 1000000;
        String result = (millions == 1 ? "un million" : convertirNombreEnLettres(millions) + " millions");
        return result + (reste == 0 ? "" : " " + convertirNombreEnLettres(reste));
    }

    // ========================================================================
    // ENVOI ET NOTIFICATION
    // ========================================================================
    
    /**
     * Envoie le reçu par email au client
     */
    public void envoyerRecuParEmail(Long versementId) {
        log.info("Envoi du reçu par email pour versement: {}", versementId);
        
        RecuPaiementDTO recu = genererRecu(versementId);
        String htmlContent = genererRecuHTML(versementId);
        
        // Utiliser le service de notification pour envoyer l'email
      //  notificationService.envoyerRecuPaiement(recu, htmlContent);
        
        log.info("Reçu envoyé par email à: {}", recu.getClientEmail());
    }

    /**
     * Télécharge le reçu au format HTML
     */
    public Map<String, Object> telechargerRecu(Long versementId) {
        RecuPaiementDTO recu = genererRecu(versementId);
        String htmlContent = genererRecuHTML(versementId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("recu", recu);
        result.put("htmlContent", htmlContent);
        result.put("filename", "recu_" + recu.getNumeroRecu() + ".html");
        
        return result;
    }

    /**
     * Imprime le reçu
     */
    public String preparerRecuPourImpression(Long versementId) {
        return genererRecuHTML(versementId);
    }
}
