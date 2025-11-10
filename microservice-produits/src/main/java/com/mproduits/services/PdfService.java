/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

/**
 *
 * @author USER01
 */
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.mproduits.model.*;
import com.mproduits.repositories.VersementClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service de Génération PDF - iText 7
 * Génère les rapports: Devis, Facture, Bon de Livraison, Reçu Versement
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfService {
    
    private static final String COMPANY_NAME = "MPRODUITS SARL";
    private static final String COMPANY_ADDRESS = "Rue XYZ, Yaoundé";
    private static final String COMPANY_TEL = "+237 6XX XXX XXX";
    @Autowired
    private    VersementClientRepository versementRepositories;
    
    /**
     * Génère un PDF de facture complète
     */
    public byte[] genererFacturePdf(Facture facture) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // En-tête
            document.add(creerEntete());
            
            // Informations facture
            document.add(creerInfosFacture(facture));
            
            // Adresses
            document.add(creerAdressesTable(facture));
            
            // Tableau items
            document.add(creerTableauFactureItems(facture));
            
            // Totaux
            document.add(creerTotaux(facture));
            
            // Versements
            if (facture.getVersements() != null && !facture.getVersements().isEmpty()) {
                document.add(creerTableauVersements(facture));
            }
            
            // Pied de page
            document.add(creerPiedPage());
            
            document.close();
            
            log.info("PDF Facture généré: {}", facture.getNumeroFacture());
            
        } catch (Exception e) {
            log.error("Erreur génération PDF facture", e);
            throw new IOException("Erreur génération PDF", e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Génère un PDF de devis
     */
    public byte[] genererDevisPdf(Devis devis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // En-tête
            document.add(creerEntete());
            
            // Titre
            Paragraph titre = new Paragraph("DEVIS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(titre);
            
            // Infos devis
            Table infoTable = new Table(2);
            infoTable.addCell("Date: " + formatDate(devis.getDateDevis()));
           // infoTable.addCell("Validité: " + devis.);
            infoTable.addCell("Client: " + devis.getClient().getNom());
            infoTable.addCell("Statut: " + devis.getStatut());
            document.add(infoTable);
            
            // Items
            document.add(creerTableauDevisItems(devis));
            
            // Total
            Paragraph total = new Paragraph(
                "Total TTC: " + formatCurrency(devis.getTotal()))
                .setFontSize(14)
                .setBold();
            document.add(total);
            
            document.close();
            
        } catch (Exception e) {
            log.error("Erreur génération PDF devis", e);
            throw new IOException("Erreur génération PDF", e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Génère un PDF bon de livraison
     */
    public byte[] genererBonLivraisonPdf(Facture facture) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            document.add(creerEntete());
            
            Paragraph titre = new Paragraph("BON DE LIVRAISON")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(titre);
            
            document.add(creerInfosFacture(facture));
            document.add(creerTableauFactureItems(facture));
            document.add(creerSignatureZone());
            
            document.close();
            
        } catch (Exception e) {
            throw new IOException("Erreur génération bon livraison", e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Génère un reçu de versement
     */
    public byte[] genererRecuVersementPdf(Facture facture, VersementClient versement) 
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            document.add(creerEntete());
            
            Paragraph titre = new Paragraph("REÇU DE VERSEMENT")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(titre);
            
            Table reçuTable = new Table(2)
                .setWidth(300);
            
            reçuTable.addCell("Facture N°:").addCell(facture.getNumeroFacture());
            reçuTable.addCell("Client:").addCell(facture.getClient().getNom());
            reçuTable.addCell("Montant facture:").addCell(
                formatCurrency(facture.getTotalTtc()));
            reçuTable.addCell("Montant versé:").addCell(
                formatCurrency(versement.getMontant()));
            reçuTable.addCell("Mode:").addCell(versement.getModePaiement().name());
            reçuTable.addCell("Date:").addCell(formatDate(versement.getDateVersement()));
            reçuTable.addCell("Référence:").addCell(
                versement.getReferencePaiement() != null ? 
                versement.getReferencePaiement() : "N/A");
            
            document.add(reçuTable);
            
            document.close();
            
        } catch (Exception e) {
            throw new IOException("Erreur génération reçu", e);
        }
        
        return baos.toByteArray();
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private Paragraph creerEntete() {
        Paragraph entete = new Paragraph();
        entete.add(new Text(COMPANY_NAME + "\n")
            .setFontSize(16)
            .setBold());
        entete.add(new Text(COMPANY_ADDRESS + " | " + COMPANY_TEL + "\n")
            .setFontSize(10));
        entete.setTextAlignment(TextAlignment.CENTER);
        entete.setMarginBottom(10);
        
        return entete;
    }
    
    private Paragraph creerInfosFacture(Facture facture) {
        Paragraph infos = new Paragraph();
        infos.add("Facture N° : " + facture.getNumeroFacture() + "\n");
        infos.add("Date : " + formatDate(facture.getDateFacture()) + "\n");
        infos.add("Statut : " + facture.getStatut() + "\n");
        infos.setMarginBottom(10);
        
        return infos;
    }
    
    private Table creerAdressesTable(Facture facture) {
        Table table = new Table(2);
        
        table.addCell(new Cell()
            .add(new Paragraph("FACTURER À:").setBold())
            .add(new Paragraph(facture.getClient().getNom()))
            .add(new Paragraph(facture.getClient().getAdresse())));
        
        table.addCell(new Cell()
            .add(new Paragraph("LIVRER À:").setBold())
            .add(new Paragraph(facture.getClient().getNom()))
            .add(new Paragraph(facture.getClient().getVille())));
        
        return table;
    }
    
    private Table creerTableauFactureItems(Facture facture) {
        Table table = new Table(6);
        
        // En-tête
        table.addHeaderCell("Code");
        table.addHeaderCell("Produit");
        table.addHeaderCell("Quantité");
        table.addHeaderCell("P.U. HT");
        table.addHeaderCell("Montant HT");
        table.addHeaderCell("Montant TTC");
        
        // Items
        for (FactureItem item : facture.getItems()) {
            //FactureItemSnapshot snap = item.creerSnapshot();
            table.addCell(item.getProduitCodeSnapshot());
            table.addCell(item.getProduitLibelleSnapshot());
            table.addCell(String.valueOf(item.getQuantite()));
            table.addCell(formatCurrency(item.getPrixUnitaireHT()));
            table.addCell(formatCurrency(item.getMontantHT()));
            table.addCell(formatCurrency(item.getMontantTTC()));
        }
        
        return table;
    }
    
    private Table creerTableauDevisItems(Devis devis) {
        Table table = new Table(5);
        
        table.addHeaderCell("Produit");
        table.addHeaderCell("Quantité");
        table.addHeaderCell("P.U.");
        table.addHeaderCell("Remise");
        table.addHeaderCell("Total");
        
        // À implémenter selon votre logique
        
        return table;
    }
    
    private Table creerTotaux(Facture facture) {
        Table table = new Table(2);
        
        table.addCell("Total HT:").addCell(formatCurrency(facture.getTotalHt()));
        table.addCell("Total TVA:").addCell(formatCurrency(facture.getTotalTVA()));
        table.addCell("Total TTC:").addCell(
            new Cell().add(new Paragraph(
                formatCurrency(facture.getTotalTtc())).setBold()));
        
        if (versementRepositories.sumVersementsByFacture(facture.getId()) != null && 
            versementRepositories.sumVersementsByFacture(facture.getId()).compareTo(BigDecimal.ZERO) > 0) {
            table.addCell("Déjà payé:").addCell(
                formatCurrency((versementRepositories.sumVersementsByFacture(facture.getId()))));
            table.addCell("Solde restant:").addCell(
                formatCurrency(facture.getTotalTtc()
                    .subtract(versementRepositories.sumVersementsByFacture(facture.getId()))));
        }
        
        return table;
    }
    
    private Table creerTableauVersements(Facture facture) {
        Table table = new Table(4);
        
        table.addHeaderCell("Date");
        table.addHeaderCell("Montant");
        table.addHeaderCell("Mode");
        table.addHeaderCell("Référence");
        
        for (VersementClient versement : facture.getVersements()) {
            table.addCell(formatDate(versement.getDateVersement()));
            table.addCell(formatCurrency(versement.getMontant()));
            table.addCell(versement.getModePaiement().name());
            table.addCell(versement.getReferencePaiement() != null ? 
                versement.getReferencePaiement() : "-");
        }
        
        return table;
    }
    
    private Paragraph creerPiedPage() {
        Paragraph p = new Paragraph(
            "Merci pour votre confiance - Facture générée le " + formatDate(new Date()));
        p.setTextAlignment(TextAlignment.CENTER);
        p.setMarginTop(30);
        
        return p;
    }
    
    private Paragraph creerSignatureZone() {
        Paragraph p = new Paragraph("\n\nSignature responsable: ________________  "
            + "Date: ________________");
        p.setMarginTop(50);
        
        return p;
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%.2f XAF", amount);
    }
    
    private String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
    
}
