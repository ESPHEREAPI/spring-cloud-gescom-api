package com.mproduits.utiles;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mproduits.model.Facture;
import com.mproduits.model.FactureItem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Générateur de PDF pour les factures
 * 
 * Utilise iText 5.5.13.3 pour générer des PDF professionnels
 */
@Slf4j
@Component
public class PDFGeneratorFacture {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère le PDF d'une facture
     * 
     * @param facture La facture à générer
     * @return Le contenu du PDF en bytes
     */
    public byte[] genererFacturePDF(Facture facture) {
        log.info("Génération du PDF pour la facture: {}", facture.getNumeroFacture());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            
            document.open();

            // En-tête avec numéro de facture
            addHeader(document, facture);
            document.add(Chunk.NEWLINE);

            // Informations client et facture
            addClientInfo(document, facture);
            document.add(Chunk.NEWLINE);

            // Tableau des lignes
            addLignesTable(document, facture);
            document.add(Chunk.NEWLINE);

            // Totaux
            addTotaux(document, facture);
            document.add(Chunk.NEWLINE);

            // Conditions de paiement et remarques
            if (facture.getConditionsPaiement() != null && !facture.getConditionsPaiement().isEmpty()) {
                addConditionsPaiement(document, facture);
            }

            if (facture.getRemarques() != null && !facture.getRemarques().isEmpty()) {
                addRemarques(document, facture);
            }

            // Pied de page
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } catch (IOException ex) {
            Logger.getLogger(PDFGeneratorFacture.class.getName()).log(Level.SEVERE, null, ex);
             throw new RuntimeException("Erreur lors de la génération du PDF", ex);
        }
    }

    /**
     * Ajoute l'en-tête du document
     */
    private void addHeader(Document document, Facture facture) throws DocumentException {
        // Titre FACTURE
        Paragraph title = new Paragraph("FACTURE", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        // Numéro de facture
        Paragraph numero = new Paragraph(facture.getNumeroFacture(), FONT_SUBTITLE);
        numero.setAlignment(Element.ALIGN_CENTER);
        document.add(numero);
        
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.BLACK);
        document.add(new Chunk(line));
    }

    /**
     * Ajoute les informations client et facture
     */
    private void addClientInfo(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        
        // Colonne gauche - Informations client
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        
        Paragraph clientTitle = new Paragraph("CLIENT", FONT_BOLD);
        clientCell.addElement(clientTitle);
        clientCell.addElement(new Paragraph(facture.getClient().getNom(), FONT_NORMAL));
        if (facture.getClient().getAdresse() != null) {
            clientCell.addElement(new Paragraph(facture.getClient().getAdresse(), FONT_NORMAL));
        }
        if (facture.getClient().getTelephone() != null) {
            clientCell.addElement(new Paragraph("Tél: " + facture.getClient().getTelephone(), FONT_NORMAL));
        }
        if (facture.getClient().getEmail() != null) {
            clientCell.addElement(new Paragraph("Email: " + facture.getClient().getEmail(), FONT_NORMAL));
        }
        
        table.addCell(clientCell);
        
        // Colonne droite - Informations facture
        PdfPCell factureCell = new PdfPCell();
        factureCell.setBorder(Rectangle.NO_BORDER);
        
        Paragraph factureTitle = new Paragraph("DÉTAILS", FONT_BOLD);
        factureCell.addElement(factureTitle);
        factureCell.addElement(new Paragraph("Date: " + facture.getDateFacture().toString()));
        factureCell.addElement(new Paragraph("Échéance: " + facture.getDateEcheance().toString()));
        factureCell.addElement(new Paragraph("Statut: " + facture.getStatut().getLibelle(), FONT_NORMAL));
        
        table.addCell(factureCell);
        document.add(table);
    }

    /**
     * Ajoute le tableau des lignes de facture
     */
    private void addLignesTable(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1, 1, 1, 1.5f});
        table.setSpacingBefore(10);
        
        // En-tête du tableau
        addTableHeader(table, new String[]{"Désignation", "Qté", "P.U. HT", "TVA", "Total TTC"});
        
        // Lignes de facture
        for (FactureItem ligne : facture.getItems()) {
            addTableCell(table, ligne.getProduit().getLibelle(), FONT_NORMAL);
            addTableCell(table, String.valueOf(ligne.getQuantite()), FONT_NORMAL);
            addTableCell(table, formatMontant(ligne.getPrixUnitaireTTC().doubleValue()) + " XAF", FONT_NORMAL);
            addTableCell(table, ligne.getTauxTVA() + "%", FONT_NORMAL);
            addTableCell(table, formatMontant(ligne.getMontantTTC().doubleValue()) + " XAF", FONT_BOLD);
        }
        
        document.add(table);
    }

    /**
     * Ajoute les totaux
     */
    private void addTotaux(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        // Total HT
        addTotalRow(table, "Total HT:", formatMontant(facture.getTotalHt().doubleValue()) + " XAF", false);
        
        // TVA
        Double montantTVA = facture.getTotalTVA().doubleValue();
        addTotalRow(table, "TVA:", formatMontant(montantTVA) + " XAF", false);
        
        // Total TTC
        addTotalRow(table, "TOTAL TTC:", formatMontant(facture.getTotalTtc().doubleValue()) + " XAF", true);
        
        // Montant payé
        if (facture.getMontantPaye().doubleValue() > 0) {
            addTotalRow(table, "Montant payé:", formatMontant(facture.getMontantPaye().doubleValue()) + " XAF", false);
            
            // Solde restant
            addTotalRow(table, "SOLDE RESTANT:", formatMontant(facture.getSoldeRestant().doubleValue()) + " XAF", true);
        }
        
        document.add(table);
    }

    /**
     * Ajoute une ligne de total
     */
    private void addTotalRow(PdfPTable table, String label, String value, boolean bold) {
        Font font = bold ? FONT_BOLD : FONT_NORMAL;
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        if (bold) {
            valueCell.setBackgroundColor(new BaseColor(240, 240, 240));
        }
        table.addCell(valueCell);
    }

    /**
     * Ajoute les conditions de paiement
     */
    private void addConditionsPaiement(Document document, Facture facture) throws DocumentException {
        Paragraph title = new Paragraph("Conditions de paiement", FONT_BOLD);
        document.add(title);
        
        Paragraph content = new Paragraph(facture.getConditionsPaiement(), FONT_NORMAL);
        document.add(content);
        document.add(Chunk.NEWLINE);
    }

    /**
     * Ajoute les remarques
     */
    private void addRemarques(Document document, Facture facture) throws DocumentException {
        Paragraph title = new Paragraph("Remarques", FONT_BOLD);
        document.add(title);
        
        Paragraph content = new Paragraph(facture.getRemarques(), FONT_NORMAL);
        document.add(content);
        document.add(Chunk.NEWLINE);
    }

    /**
     * Ajoute le pied de page
     */
    private void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        
        Paragraph footer = new Paragraph(
                "Merci de votre confiance. Cette facture est générée automatiquement.",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    /**
     * Ajoute l'en-tête d'un tableau
     */
    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
            cell.setBackgroundColor(new BaseColor(0, 123, 255));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    /**
     * Ajoute une cellule de tableau
     */
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Formate un montant
     */
    private String formatMontant(Double montant) {
        return String.format("%,d", montant.longValue());
    }
}