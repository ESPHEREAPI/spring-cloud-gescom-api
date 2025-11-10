/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.utiles;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.mproduits.model.VersementClient;


import java.io.ByteArrayOutputStream;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mproduits.dto.RapportClientData;
import com.mproduits.dto.VersementResponse;
import com.mproduits.model.Facture;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author USER01
 */
/**
 * Générateur de documents PDF pour les versements et rapports
 */
@Slf4j
@Component
public class PDFGenerator {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère un reçu de paiement
     * @param versement
     * @return 
     */
    public byte[] genererRecuPaiement(VersementClient versement) {
        log.info("Génération du reçu de paiement pour le versement {}", versement.getNumeroVersement());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            
            document.open();

            // En-tête
            addHeader(document, "REÇU DE PAIEMENT");
            document.add(Chunk.NEWLINE);

            // Informations du versement
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 2});

            addInfoRow(infoTable, "N° de Reçu:", versement.getNumeroVersement());
            addInfoRow(infoTable, "Date:", versement.getDateVersement().toString());
            addInfoRow(infoTable, "Client:", versement.getClient().getNom());
            addInfoRow(infoTable, "N° Facture:", versement.getFacture().getNumeroFacture());
            addInfoRow(infoTable, "Mode de Paiement:", versement.getModePaiement().getLibelle());
            
            if (versement.getReferencePaiement() != null) {
                addInfoRow(infoTable, "Référence:", versement.getReferencePaiement());
            }

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Montant
            Paragraph montantPara = new Paragraph();
            montantPara.setAlignment(Element.ALIGN_CENTER);
            montantPara.add(new Chunk("MONTANT PAYÉ\n", FONT_SUBTITLE));
            montantPara.add(new Chunk(formatMontant(versement.getMontant()) + " XAF", FONT_TITLE));
            document.add(montantPara);
            
            document.add(Chunk.NEWLINE);

            // Remarques
            if (versement.getRemarques() != null && !versement.getRemarques().isEmpty()) {
                Paragraph remarques = new Paragraph("Remarques: " + versement.getRemarques(), FONT_NORMAL);
                document.add(remarques);
            }

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Pied de page
            Paragraph footer = new Paragraph("Merci pour votre paiement", FONT_NORMAL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du reçu PDF", e);
            throw new RuntimeException("Erreur lors de la génération du reçu PDF", e);
        } catch (IOException ex) {
            Logger.getLogger(PDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
             throw new RuntimeException("Erreur lors de la génération du reçu PDF..IOException", ex);
        }
     
    }

    /**
     * Génère un rapport client complet
     */
    public byte[] genererRapportClient(RapportClientData data) {
        log.info("Génération du rapport client pour le client {}", data.getClientId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, baos);
            
            document.open();

            // En-tête principal
            addHeader(document, "RAPPORT DE VERSEMENTS CLIENT");
            document.add(Chunk.NEWLINE);

            // Informations client
            addClientInfo(document, data);
            document.add(Chunk.NEWLINE);

            // Période du rapport
            addPeriodeInfo(document, data);
            document.add(Chunk.NEWLINE);

            // Statistiques globales
            addStatistiquesBox(document, data);
            document.add(Chunk.NEWLINE);

            // Tableau des factures
            addFacturesTable(document, data);
            document.add(Chunk.NEWLINE);

            // Historique des versements
            addVersementsTable(document, data);

            // Pied de page
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du rapport PDF", e);
            throw new RuntimeException("Erreur lors de la génération du rapport PDF", e);
        } catch (IOException ex) {
            Logger.getLogger(PDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
               throw new RuntimeException("Erreur lors de la génération du rapport PDF... IOException", ex);
        }
      
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void addHeader(Document document, String title) throws DocumentException {
        Paragraph header = new Paragraph(title, FONT_TITLE);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.BLACK);
        document.add(new Chunk(line));
    }

    private void addClientInfo(Document document, RapportClientData data) throws DocumentException {
        Paragraph title = new Paragraph("Informations Client", FONT_SUBTITLE);
        document.add(title);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});
        
        addInfoRow(table, "Code Client:", data.getClientCode());
        addInfoRow(table, "Nom:", data.getClientNom());
        if (data.getClientEmail() != null) {
            addInfoRow(table, "Email:", data.getClientEmail());
        }
        if (data.getClientTelephone() != null) {
            addInfoRow(table, "Téléphone:", data.getClientTelephone());
        }
        
        document.add(table);
    }

    private void addPeriodeInfo(Document document, RapportClientData data) throws DocumentException {
        Paragraph title = new Paragraph("Période du Rapport", FONT_SUBTITLE);
        document.add(title);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});
        
        addInfoRow(table, "Date Début:", data.getDateDebut().format(DATE_FORMATTER));
        addInfoRow(table, "Date Fin:", data.getDateFin().format(DATE_FORMATTER));
        addInfoRow(table, "Généré le:", java.time.LocalDateTime.now().format(DATETIME_FORMATTER));
        
        document.add(table);
    }

    private void addStatistiquesBox(Document document, RapportClientData data) throws DocumentException {
        Paragraph title = new Paragraph("Statistiques", FONT_SUBTITLE);
        document.add(title);
        
        // Créer un tableau 2x2 pour les statistiques
        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingBefore(10);
        
        // Total Versements
        PdfPCell cell1 = createStatCell("Total Versements", formatMontant(data.getTotalVersements()) + " XAF", 
                new BaseColor(40, 167, 69));
        statsTable.addCell(cell1);
        
        // Versements Validés
        PdfPCell cell2 = createStatCell("Versements Validés", formatMontant(data.getTotalValides()) + " XAF",
                new BaseColor(0, 123, 255));
        statsTable.addCell(cell2);
        
        // Nombre de Versements
        PdfPCell cell3 = createStatCell("Nombre de Versements", String.valueOf(data.getNombreVersements()),
                new BaseColor(23, 162, 184));
        statsTable.addCell(cell3);
        
        // Solde Restant
        PdfPCell cell4 = createStatCell("Solde Restant", formatMontant(data.getSoldeTotal()) + " XAF",
                data.getSoldeTotal().compareTo(BigDecimal.ZERO) > 0 ? 
                        new BaseColor(255, 193, 7) : new BaseColor(40, 167, 69));
        statsTable.addCell(cell4);
        
        document.add(statsTable);
    }

    private PdfPCell createStatCell(String label, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(2);
        cell.setBorderColor(color);
        cell.setPadding(10);
        
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", FONT_NORMAL));
        p.add(new Chunk(value, FONT_SUBTITLE));
        p.setAlignment(Element.ALIGN_CENTER);
        
        cell.addElement(p);
        return cell;
    }

    private void addFacturesTable(Document document, RapportClientData data) throws DocumentException {
        Paragraph title = new Paragraph("Situation des Factures", FONT_SUBTITLE);
        document.add(title);
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1.5f, 1.5f, 1.5f});
        table.setSpacingBefore(10);
        
        // En-tête
        addTableHeader(table, new String[]{"N° Facture", "Montant TTC", "Payé", "Solde"});
        
        // Données
        BigDecimal totalTTC = BigDecimal.ZERO;
        BigDecimal totalPaye = BigDecimal.ZERO;
        BigDecimal totalSolde = BigDecimal.ZERO;
        
        for (Facture facture : data.getFactures()) {
            addTableCell(table, facture.getNumeroFacture(), FONT_NORMAL);
            addTableCell(table, formatMontant(facture.getTotalTtc()) + " XAF", FONT_NORMAL);
            addTableCell(table, formatMontant(facture.getMontantPaye()) + " XAF", FONT_NORMAL);
            addTableCell(table, formatMontant(facture.getSoldeRestant()) + " XAF", 
                    facture.getSoldeRestant().compareTo(BigDecimal.ZERO) > 0 ? 
                            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.RED) : FONT_NORMAL);
            
            totalTTC = totalTTC.add(facture.getTotalTtc());
            totalPaye = totalPaye.add(facture.getMontantPaye());
            totalSolde = totalSolde.add(facture.getSoldeRestant());
        }
        
        // Total
        addTableCell(table, "TOTAL", FONT_BOLD);
        addTableCell(table, formatMontant(totalTTC) + " XAF", FONT_BOLD);
        addTableCell(table, formatMontant(totalPaye) + " XAF", FONT_BOLD);
        addTableCell(table, formatMontant(totalSolde) + " XAF", FONT_BOLD);
        
        document.add(table);
    }

    private void addVersementsTable(Document document, RapportClientData data) throws DocumentException {
        Paragraph title = new Paragraph("Historique Détaillé des Versements", FONT_SUBTITLE);
        document.add(title);
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2, 2, 1.5f, 1.5f});
        table.setSpacingBefore(10);
        
        // En-tête
        addTableHeader(table, new String[]{"Date", "N° Versement", "Mode Paiement", "Montant", "Statut"});
        
        // Données
        for (VersementResponse versement : data.getVersements()) {
            addTableCell(table, versement.getDateVersement().toString(), FONT_NORMAL);
            addTableCell(table, versement.getNumeroVersement(), FONT_NORMAL);
            addTableCell(table, versement.getModePaiement().getLibelle(), FONT_NORMAL);
            addTableCell(table, formatMontant(versement.getMontant()) + " XAF", FONT_NORMAL);
            
            BaseColor statusColor = getStatusColor(versement.getStatut().name());
            addTableCell(table, versement.getStatut().name(), 
                    new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, statusColor));
        }
        
        // Total
        addTableCell(table, "", FONT_BOLD);
        addTableCell(table, "", FONT_BOLD);
        addTableCell(table, "TOTAL", FONT_BOLD);
        addTableCell(table, formatMontant(data.getTotalVersements()) + " XAF", FONT_BOLD);
        addTableCell(table, "", FONT_BOLD);
        
        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        Paragraph footer = new Paragraph(
                "Ce document a été généré automatiquement par le système de Gestion de Facturation.",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
            cell.setBackgroundColor(new BaseColor(0, 123, 255));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private BaseColor getStatusColor(String status) {
        switch (status) {
            case "VALIDE":
                return new BaseColor(40, 167, 69);
            case "EN_ATTENTE":
                return new BaseColor(255, 193, 7);
            case "ANNULE":
                return new BaseColor(220, 53, 69);
            default:
                return BaseColor.BLACK;
        }
    }

    private String formatMontant(BigDecimal montant) {
        return String.format("%,d", montant.longValue());
    }
    
}
