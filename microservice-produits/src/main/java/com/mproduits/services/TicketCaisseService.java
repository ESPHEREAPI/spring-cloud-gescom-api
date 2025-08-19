/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.TicketRequest;
import com.mproduits.dto.TicketResponse;
import com.mproduits.enums.TypePaiement;
import com.mproduits.exceptions.TicketGenerationException;
import com.mproduits.model.Entreprise;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Paiement;
import com.mproduits.model.Personne;
import com.mproduits.model.Vente;
import com.mproduits.repositories.PaiementRepositories;
import com.mproduits.repositories.VenteRepositories;
import com.mproduits.utiles.GlobalFonctions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
    private static final String DEFAULT_TICKET_PATH = "C:/Ticket";
    private static final String TICKET_EXTENSION = ".txt";
    private static final int TICKET_WIDTH = 50;
    @Autowired
    VenteRepositories venteRepositories;
    @Autowired
    PaiementRepositories paiementRepositories;

    /**
     * Génère un ticket de caisse optimisé
     */
    public TicketResponse generateTicket(TicketRequest request) throws IOException {
        try {
            // Validation des données d'entrée
            validateTicketRequest(request);

            // Construction du chemin du fichier
            String filePath = buildTicketPath(request);

            // Création du contenu du ticket
            File ticketFile  = buildTicketContent(request, filePath,request.getVente());

            // Sauvegarde du fichier
           // File ticketFile = saveTicketToFile(filePath, ticketContent, request.getVente());

            // Retour de la réponse
            return TicketResponse.builder()
                    .filePath(filePath)
                    .fileName(ticketFile.getName())
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
     * Construction du chemin du fichier de manière optimisée
     */
    private String buildTicketPath(TicketRequest request) {
        String basePath = Optional.ofNullable(request.getProperty().getCheminTicket())
                .filter(path -> !path.trim().isEmpty())
                .orElse(DEFAULT_TICKET_PATH);

        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(request.getVente().getEntreprise().getAnnee().getId());
        String month = request.getMois();
        String dateFormatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String vendeurName = buildVendeurName(request.getVente().getVendeur());
        String ticketNumber = request.getVente().getNumeroTicket();

        // Construction du chemin complet
        Path fullPath = Paths.get(basePath, year, month, dateFormatted + "_" + vendeurName);

        // Création des dossiers si nécessaire
        try {
            Files.createDirectories(fullPath);
        } catch (IOException e) {
            logger.error("Erreur lors de la création des dossiers: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer les dossiers", e);
        }

        return fullPath.resolve(ticketNumber + TICKET_EXTENSION).toString();
    }

    /**
     * Construction du nom du vendeur
     */
    private String buildVendeurName(Personne vendeur) {
        String nom = Optional.ofNullable(vendeur.getNom()).orElse("");
        String prenom = Optional.ofNullable(vendeur.getPrenom()).orElse("");

        return (nom + "_" + prenom).replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Construction du contenu du ticket
     */
    private File buildTicketContent(TicketRequest request, String filepathe,Vente v) throws IOException {
        StringBuilder content = new StringBuilder();

//        // En-tête
//        buildTicketHeader(content, request);
//
//        // Lignes de vente
//        buildTicketLines(content, request.getLignesVente());
//
//        // Pied de page avec totaux
//        buildTicketFooter(content, request);
//
//        return content.toString();
        Vente vente = request.getVente();
        int nbreArticle = 0;

//        String cheminFile = null;
//        if (cheminFile == null || cheminFile.isEmpty()) {
//            cheminFile = "C:/Ticket";
//        }
        String caissier = vente.getVendeur().getNom();
        if (vente.getVendeur().getPrenom() != null) {
            caissier += "_" + vente.getVendeur().getPrenom();
        }
        LocalDateTime now = LocalDateTime.now();
//        String year = String.valueOf(request.getVente().getEntreprise().getAnnee().getId());
//        String month = request.getMois();
//        String dateFormatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        String dateStr = GlobalFonctions.formatDateForNameTicket(new Date());
//        cheminFile += "/" + vente.getEntreprise().getAnnee().getId() + "/" + month;
//        Files.createDirectories(Paths.get(cheminFile));

        // String fileName = dateStr + "_" + caissier + "_" + vente.getNumeroTicket() + ".txt";
        //String fullPath = cheminFile + "/" + fileName;
        File ticketFile = new File(filepathe);
        if (ticketFile.exists() == Boolean.FALSE) {
            ticketFile.createNewFile();
        }

        try (BufferedWriter bw = Files.newBufferedWriter(ticketFile.toPath(), StandardCharsets.UTF_8)) {
            int width = 32;

            // --- EN-TÊTE ---
            bw.write(center("╔════════════════════════════╗", width));
            bw.newLine();
            bw.write(center("║     TICKET DE CAISSE      ║", width));
            bw.newLine();
            bw.write(center("╚════════════════════════════╝", width));
            bw.newLine();

            // String societe = (String) parameters.getOrDefault("societe", "");
            bw.write(center("" + " " + vente.getEntreprise().getEmployeur().getSociete(), width));
            bw.newLine();
            bw.write(center(vente.getEntreprise().getEmployeur().getPersonne().getQuartier() + " " + vente.getEntreprise().getEmployeur().getPersonne().getVille(), width));
            bw.newLine();
            bw.write(center("BP: " + vente.getEntreprise().getEmployeur().getPersonne().getBp(), width));
            bw.newLine();
            bw.write(center("Tel: " + vente.getEntreprise().getEmployeur().getPersonne().getTel(), width));
            bw.newLine();
             bw.write(center("NUM: " + vente.getEntreprise().getEmployeur().getNumeroContribuable(), width));
            bw.newLine();
            bw.write("--------------------------------");
            bw.newLine();

            String client = vente.getClient()== null ? null : vente.getClient().getNom();
            if (client != null && !client.isEmpty()) {
                bw.write("Client    : " + client);
                bw.newLine();
            }

            bw.write("Caissier  : " + vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom());
            bw.newLine();
            bw.write("Date      : " + GlobalFonctions.formatDate(new Date()));
            bw.newLine();
            bw.write("Ticket N° : " + vente.getNumeroTicket());
            bw.newLine();
            bw.write("--------------------------------");
            bw.newLine();

            // --- ARTICLES ---
            for (LigneVente l : vente.getLignes()) {
                nbreArticle += l.getQuantite().intValue();
                String libelle = truncate(l.getProduit().getLibelle(), 28);
                String detail = l.getQuantite() + " x " + formatNumberGeneral(l.getPrixUnitaire().longValue()) + " = " + formatNumberGeneral(l.getTotalLigne().longValue());
                bw.write(libelle);
                bw.newLine();
                bw.write(center(detail, width));
                bw.newLine();
            }

            bw.write("--------------------------------");
            bw.newLine();

            // --- TOTALS ---
            // String total = (String) parameters.getOrDefault("tt", "0");
            bw.write(alignRight("Total : " + vente.getTotalBrut() + " FCFA", width));
            bw.newLine();
            bw.write(alignRight("Articles : " + nbreArticle, width));
            bw.newLine();
            Optional<Paiement> p = paiementRepositories.findByVente(vente);
            bw.write(alignRight("Paiement : " + p.get().getTypePaiement().toString(), width));
            bw.newLine();

            if (vente.getTotalRemise().intValue() > 0) {
                bw.write(alignRight("Remise : " + formatNumberGeneral(vente.getTotalRemise().longValue()) + " FCFA", width));
                bw.newLine();
            }

            bw.write(alignRight("Reçu : " + formatNumberGeneral(vente.getTotalrecu().longValue()) + " FCFA", width));
            bw.newLine();
            BigDecimal rendu = vente.getTotalrecu().subtract(vente.getTotalNet());
            // String renduText = remboursementAvecBonAchat ? "Bon d'Achat" : formatNumberGeneral(rendu.longValue()) + " FCFA";
            bw.write(alignRight("Rendu : " + formatNumberGeneral(rendu.longValue()) + " FCFA", width));
            bw.newLine();

            bw.write("--------------------------------");
            bw.newLine();

            // --- FOOTER ---
            bw.write(center("Les marchandises vendues", width));
            bw.newLine();
            bw.write(center("ne sont ni reprises ni échangées", width));
            bw.newLine();
            bw.write(center("Merci et à bientôt !", width));
            bw.newLine();
            bw.write("__");
            bw.close();

        }
        File file = new File(filepathe);
        v.setCheminPatheTicket(filepathe);
        venteRepositories.save(v);
//        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
//            writer.write(content);
//
//            writer.flush();
//        }

       
        return file;

        
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

    /**
     * Construction de l'en-tête du ticket avec design
     */
    private void buildTicketHeader(StringBuilder content, TicketRequest request) {
        Vente vente = request.getVente();
        Entreprise entreprise = vente.getEntreprise();

//        // Bordure supérieure décorative
//        content.append("╔════════════════════════════════════════╗\n");
//        content.append("║                                        ║\n");
        // Nom de la société centré
        String societeComplet = entreprise.getEmployeur().getSociete();
        content.append("").append(centerText(societeComplet.toUpperCase(), 40)).append("\n");

        // Adresse centrée
        String adresse = entreprise.getEmployeur().getPersonne().getQuartier()
                + " " + entreprise.getEmployeur().getPersonne().getVille();
        content.append("").append(centerText(adresse, 40)).append("\n");

        // BP et téléphone
        String bp = "BP: " + entreprise.getEmployeur().getPersonne().getBp();
        content.append("").append(centerText(bp, 40)).append("\n");

        String tel = "Tél: " + entreprise.getEmployeur().getPersonne().getTel();
        content.append("").append(centerText(tel, 40)).append("\n");

        // Client si présent
        String client = vente.getClient() == null ? "" : vente.getClient().getNom();
        if (client != null && !client.trim().isEmpty()) {
            content.append(" Client: ").append(padRight(client, 27)).append(" \n");
        }

        // Vendeur
        String vendeur = buildVendeurDisplayName(vente.getVendeur());
        content.append("Vendeur: ").append(padRight(vendeur, 26)).append(" \n");

        // Date et heure
        String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss"));
        content.append(" Date: ").append(padRight(dateHeure, 29)).append(" \n");

        // Numéro de ticket
        String numTicket = "N° " + vente.getNumeroTicket();
        content.append(" Ticket: ").append(padRight(numTicket, 27)).append(" \n");

    }

    /**
     * Construction des lignes de vente avec design
     */
    private void buildTicketLines(StringBuilder content, List<LigneVente> lignesVente) {
        int totalArticles = 0;

        for (LigneVente ligne : lignesVente) {
            totalArticles += ligne.getQuantite().intValue();

            // Nom du produit
            String nomProduit = ligne.getProduit().getLibelle();
            if (nomProduit.length() > 36) {
                nomProduit = nomProduit.substring(0, 33) + "...";
            }
            content.append("").append(padRight(nomProduit, 37)).append("\n");

            // Ligne de calcul avec alignement
            String quantite = ligne.getQuantite().toString();
            String prix = formatNumber(ligne.getPrixUnitaire());
            String total = formatNumber(ligne.getTotalLigne());

            String ligneCalcul = quantite + " x " + prix + " = " + total + " FCFA";
            content.append("").append(padRight(ligneCalcul, 35)).append("\n");

            // Ligne de séparation entre articles
            if (lignesVente.indexOf(ligne) < lignesVente.size() - 1) {
                content.append("\n");
            }
        }

        content.append(" Nombre total d'articles: ").append(padLeft(String.valueOf(totalArticles), 13)).append(" \n");

    }

    /**
     * Construction du pied de page avec design
     */
    private void buildTicketFooter(StringBuilder content, TicketRequest request) {
        Vente vente = request.getVente();
//        String total = formatNumber(vente.getTotalBrut());

        // Total
//        content.append("║ TOTAL: ").append(padLeft(total + " FCFA", 30)).append(" ║\n");
        // Type de paiement
        String typePaiement = getTypePaiementLabel(request.getTypePaiement());
        content.append(" Paiement: ").append(padLeft(typePaiement, 27)).append(" \n");

        // Gestion des remises et rendu
        if (vente.getTotalRemise().compareTo(BigDecimal.ZERO) == 0) {
            if (Boolean.TRUE.equals(request.getRemboursementAvecBonAchat())) {
                content.append(" Rendu: ").append(padLeft("Bon d'Achat", 28)).append("\n");
            } else {
                String totalBrut = formatNumber(vente.getTotalBrut()) + " FCFA";
                content.append(" Total: ").append(padLeft(totalBrut, 28)).append(" \n");

                String montantRecu = formatNumber(vente.getTotalrecu()) + " FCFA";
                content.append(" Reçu:  ").append(padLeft(montantRecu, 28)).append(" \n");

                BigDecimal rendu = vente.getTotalBrut().subtract(vente.getTotalrecu());
                String renduStr = formatNumber(rendu) + " FCFA";
                content.append(" Rendu: ").append(padLeft(renduStr, 28)).append(" \n");
            }
        } else {
            String remise = formatNumber(vente.getTotalRemise()) + " FCFA";
            content.append(" Remise: ").append(padLeft(remise, 27)).append(" \n");

            if (Boolean.TRUE.equals(request.getRemboursementAvecBonAchat())) {
                content.append(" Rendu: ").append(padLeft("Bon d'Achat", 28)).append(" \n");
            } else {
                BigDecimal rendu = vente.getTotalNet().subtract(vente.getTotalrecu());
                String renduStr = formatNumber(rendu) + " FCFA";
                content.append(" Rendu: ").append(padLeft(renduStr, 28)).append(" \n");
            }

            String netPayer = formatNumber(vente.getTotalNet()) + " FCFA";
            content.append(" Net à payer: ").append(padLeft(netPayer, 24)).append(" \n");
        }

        content.append("Les marchandises vendues ne sont   \n");
        content.append("ni reprises ni échangées               \n");

        content.append("      ★ Merci de votre visite ! ★       \n");

        content.append("           À bientôt chez nous          \n");

    }

    /**
     * Sauvegarde du ticket dans un fichier
     */
    private File saveTicketToFile(String filePath, String content, Vente v) throws IOException {
        File file = new File(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(content);

            v.setCheminPatheTicket(filePath);
            venteRepositories.save(v);
            writer.flush();
        }

        logger.info("Ticket sauvegardé avec succès: {}", filePath);
        return file;
    }

    /**
     * Méthodes utilitaires pour le formatage et design
     */
    private String buildVendeurDisplayName(Personne vendeur) {
        String nom = Optional.ofNullable(vendeur.getNom()).orElse("");
        String prenom = Optional.ofNullable(vendeur.getPrenom()).orElse("");
        return (nom + " " + prenom).trim();
    }

    private String getTypePaiementLabel(TypePaiement tp) {
        return tp != null ? tp.name() : TypePaiement.ESPECES.name();
    }

    private String formatNumber(BigDecimal number) {
        if (number == null) {
            return "0";
        }
        return NumberFormat.getNumberInstance(Locale.FRANCE).format(number);
    }

    /**
     * Centre un texte dans une largeur donnée
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }

        int padding = width - text.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;

        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    /**
     * Aligne un texte à droite dans une largeur donnée
     */
    private String padLeft(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return " ".repeat(width - text.length()) + text;
    }

    /**
     * Aligne un texte à gauche dans une largeur donnée
     */
    private String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }

}
