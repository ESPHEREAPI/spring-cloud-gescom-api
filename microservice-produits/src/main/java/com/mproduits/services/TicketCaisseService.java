/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.TicketRequest;
import com.mproduits.dto.TicketResponse;
import com.mproduits.exceptions.TicketGenerationException;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Paiement;
import com.mproduits.model.Vente;
import com.mproduits.repositories.PaiementRepositories;
import com.mproduits.utiles.GlobalFonctions;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class TicketCaisseService {

    private static final Logger logger = LoggerFactory.getLogger(TicketCaisseService.class);
    @Autowired
    PaiementRepositories paiementRepositories;

    /**
     * Génère le contenu texte d'un ticket de caisse.
     *
     * Genere entierement en memoire (aucune ecriture disque) : l'ancienne
     * version ecrivait le ticket dans un fichier sous un chemin par defaut
     * code en dur "C:/Ticket", valide uniquement sur un poste Windows - sur
     * le backend Linux/Docker de production, ce chemin echouait (ou ecrivait
     * dans un dossier ephemere non monte en volume), faisant echouer toute
     * impression de ticket avec une simple erreur 500 sans message. Le
     * contenu etant entierement derivable de la Vente en base, le
     * regenerer a chaque demande evite ce point de defaillance.
     */
    public TicketResponse generateTicket(TicketRequest request) {
        try {
            // Validation des données d'entrée
            validateTicketRequest(request);

            String contenu = construireContenuTicket(request);

            return TicketResponse.builder()
                    .content(contenu)
                    .success(true)
                    .message("Ticket créé avec succès")
                    .ticketNumber(request.getVente().getNumeroTicket())
                    .build();

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du ticket: {}", e.getMessage(), e);
            throw new TicketGenerationException("Erreur lors de la génération du ticket", e);
        }
    }

    /**
     * Validation des données d'entrée
     */
    private void validateTicketRequest(TicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Les données du ticket ne peuvent pas être nulles");
        }

        if (request.getLignesVente() == null || request.getLignesVente().isEmpty()) {
            throw new IllegalArgumentException("Aucune ligne de vente trouvée");
        }

        if (request.getVente() == null) {
            throw new IllegalArgumentException("Les informations de vente sont manquantes");
        }
    }

    /**
     * Construction du contenu du ticket (texte pur, en memoire)
     */
    private String construireContenuTicket(TicketRequest request) {
        Vente vente = request.getVente();
        int nbreArticle = 0;
        int width = 32;
        StringBuilder bw = new StringBuilder();

        // --- EN-TÊTE ---
        bw.append(center("╔════════════════════════════╗", width)).append('\n');
        bw.append(center("║     TICKET DE CAISSE      ║", width)).append('\n');
        bw.append(center("╚════════════════════════════╝", width)).append('\n');

        bw.append(center(" " + vente.getEntreprise().getCompagnie().getNom(), width)).append('\n');
        bw.append(center(vente.getEntreprise().getCompagnie().getQuartier() + " " + vente.getEntreprise().getCompagnie().getVille(), width)).append('\n');
        bw.append(center("BP: " + vente.getEntreprise().getCompagnie().getBp(), width)).append('\n');
        bw.append(center("Tel: " + vente.getEntreprise().getCompagnie().getTel(), width)).append('\n');
        bw.append(center("NUM: " + vente.getEntreprise().getCompagnie().getNumeroContribuable(), width)).append('\n');
        bw.append("--------------------------------").append('\n');

        String client = vente.getClient() == null ? null : vente.getClient().getNom();
        if (client != null && !client.isEmpty()) {
            bw.append("Client    : ").append(client).append('\n');
        }

        bw.append("Caissier  : ").append(vente.getVendeur().getNom()).append(" ").append(vente.getVendeur().getPrenom()).append('\n');
        bw.append("Date      : ").append(GlobalFonctions.formatDate(new Date())).append('\n');
        bw.append("Ticket N° : ").append(vente.getNumeroTicket()).append('\n');
        bw.append("--------------------------------").append('\n');

        // --- ARTICLES ---
        for (LigneVente l : vente.getLignes()) {
            nbreArticle += l.getQuantite().intValue();
            String libelle = truncate(l.getProduit().getLibelle(), 28);
            String detail = l.getQuantite() + " x " + formatNumberGeneral(l.getPrixUnitaire().longValue()) + " = " + formatNumberGeneral(l.getTotalLigne().longValue());
            bw.append(libelle).append('\n');
            bw.append(center(detail, width)).append('\n');
        }

        bw.append("--------------------------------").append('\n');

        // --- TOTALS ---
        bw.append(alignRight("Total : " + vente.getTotalBrut() + " FCFA", width)).append('\n');
        bw.append(alignRight("Articles : " + nbreArticle, width)).append('\n');
        List<Paiement> paiementsVente = paiementRepositories.findAllByVente(vente);
        String modePaiement = paiementsVente.stream()
                .map(pai -> pai.getTypePaiement().toString())
                .distinct()
                .collect(java.util.stream.Collectors.joining(" + "));
        bw.append(alignRight("Paiement : " + modePaiement, width)).append('\n');

        if (vente.getTotalRemise().intValue() > 0) {
            bw.append(alignRight("Remise : " + formatNumberGeneral(vente.getTotalRemise().longValue()) + " FCFA", width)).append('\n');
        }

        bw.append(alignRight("Reçu : " + formatNumberGeneral(vente.getTotalrecu().longValue()) + " FCFA", width)).append('\n');
        BigDecimal rendu = vente.getTotalrecu().subtract(vente.getTotalNet());
        bw.append(alignRight("Rendu : " + formatNumberGeneral(rendu.longValue()) + " FCFA", width)).append('\n');

        bw.append("--------------------------------").append('\n');

        // --- FOOTER ---
        bw.append(center("Les marchandises vendues", width)).append('\n');
        bw.append(center("ne sont ni reprises ni échangées", width)).append('\n');
        bw.append(center("Merci et à bientôt !", width)).append('\n');
        bw.append("__");

        return bw.toString();
    }

    private static String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private static String alignRight(String text, int width) {
        //return String.format("%" + width + "s", text);

        if (text == null) {
            return "";
        }
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    private static String truncate(String str, int maxLength) {
        return str.length() > maxLength ? str.substring(0, maxLength - 1) : str;
    }

    private static String formatNumberGeneral(long value) {
        return String.format("%,d", value).replace(',', ' ');
    }

}
