package com.mproduits.utiles;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mproduits.model.Compagnie;
import com.mproduits.model.Magasin;
import com.mproduits.model.TransfertStock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Genere le bordereau de livraison PDF d'un transfert de stock - piece
 * justificative de la reception, a faire signer par le depot destinataire
 * (voir tache : "chaque transfert doit generer un bordereau pour justifier
 * la reception"). Meme pattern de structure que PDFGeneratorFacture, en
 * plus simple (une seule ligne produit par transfert).
 */
@Slf4j
@Component
public class PDFGeneratorBordereauLivraison {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.BLACK);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FONT_SMALL_GRAY = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public byte[] genererBordereau(TransfertStock transfert, Compagnie compagnie) {
        log.info("Génération du bordereau de livraison pour le transfert: {}", transfert.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);

            document.open();

            addHeader(document, transfert, compagnie);
            document.add(Chunk.NEWLINE);

            addMagasinsInfo(document, transfert);
            document.add(Chunk.NEWLINE);

            addLigneTable(document, transfert);
            document.add(Chunk.NEWLINE);

            if (transfert.getNotes() != null && !transfert.getNotes().isBlank()) {
                addNotes(document, transfert);
            }

            addSignatures(document);
            addFooter(document, transfert);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du bordereau de livraison", e);
            throw new RuntimeException("Erreur lors de la génération du bordereau de livraison", e);
        } catch (IOException ex) {
            Logger.getLogger(PDFGeneratorBordereauLivraison.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Erreur lors de la génération du bordereau de livraison", ex);
        }
    }

    private void addHeader(Document document, TransfertStock transfert, Compagnie compagnie) throws DocumentException {
        if (compagnie != null && compagnie.getNom() != null) {
            Paragraph nomCompagnie = new Paragraph(compagnie.getNom(), FONT_SUBTITLE);
            document.add(nomCompagnie);
            if (compagnie.getAdresse() != null) {
                document.add(new Paragraph(compagnie.getAdresse(), FONT_NORMAL));
            }
            if (compagnie.getTel() != null) {
                document.add(new Paragraph("Tél: " + compagnie.getTel(), FONT_NORMAL));
            }
            document.add(Chunk.NEWLINE);
        }

        Paragraph title = new Paragraph("BORDEREAU DE LIVRAISON", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph numero = new Paragraph("N° BL-" + transfert.getId(), FONT_SUBTITLE);
        numero.setAlignment(Element.ALIGN_CENTER);
        document.add(numero);

        Paragraph date = new Paragraph("Date : " + DATE_FORMAT.format(transfert.getDateTransfert()), FONT_NORMAL);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);

        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.BLACK);
        document.add(new Chunk(line));
    }

    private void addMagasinsInfo(Document document, TransfertStock transfert) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.setSpacingBefore(10);

        table.addCell(magasinCell("EXPÉDITEUR (Dépôt source)", transfert.getSource()));
        table.addCell(magasinCell("DESTINATAIRE (Dépôt destination)", transfert.getDestination()));

        document.add(table);
    }

    private PdfPCell magasinCell(String titre, Magasin magasin) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);

        cell.addElement(new Paragraph(titre, FONT_BOLD));
        if (magasin != null) {
            cell.addElement(new Paragraph(magasin.getLibelle(), FONT_NORMAL));
            if (magasin.getCode() != null) {
                cell.addElement(new Paragraph("Code: " + magasin.getCode(), FONT_NORMAL));
            }
        }
        return cell;
    }

    private void addLigneTable(Document document, TransfertStock transfert) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 4, 1.5f});
        table.setSpacingBefore(10);

        addTableHeader(table, new String[]{"Référence", "Désignation", "Quantité transférée"});

        addTableCell(table, transfert.getProduit() != null ? transfert.getProduit().getReference() : "-", FONT_NORMAL);
        addTableCell(table, transfert.getProduit() != null ? transfert.getProduit().getLibelle() : "-", FONT_NORMAL);
        addTableCell(table, formatQuantite(transfert.getQuantite()), FONT_BOLD);

        document.add(table);
    }

    private void addNotes(Document document, TransfertStock transfert) throws DocumentException {
        Paragraph title = new Paragraph("Notes", FONT_BOLD);
        title.setSpacingBefore(6);
        document.add(title);
        document.add(new Paragraph(transfert.getNotes(), FONT_NORMAL));
    }

    // Deux blocs signature - c'est la piece justificative de la reception :
    // le destinataire signe pour attester avoir bien recu la quantite
    // indiquee (voir tache : "genere un bordereau pour justifier la reception").
    private void addSignatures(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.setSpacingBefore(30);

        PdfPCell expediteur = new PdfPCell();
        expediteur.setBorder(Rectangle.NO_BORDER);
        expediteur.addElement(new Paragraph("Signature Expéditeur", FONT_BOLD));
        expediteur.addElement(new Paragraph(" "));
        expediteur.addElement(new Paragraph("_______________________", FONT_NORMAL));
        table.addCell(expediteur);

        PdfPCell destinataire = new PdfPCell();
        destinataire.setBorder(Rectangle.NO_BORDER);
        destinataire.addElement(new Paragraph("Signature Destinataire (bon pour réception)", FONT_BOLD));
        destinataire.addElement(new Paragraph(" "));
        destinataire.addElement(new Paragraph("_______________________", FONT_NORMAL));
        table.addCell(destinataire);

        document.add(table);
    }

    private void addFooter(Document document, TransfertStock transfert) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "Émis par " + (transfert.getUtilisateur() != null ? transfert.getUtilisateur() : "-")
                        + " - Document généré automatiquement.",
                FONT_SMALL_GRAY);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
            cell.setBackgroundColor(new BaseColor(0, 123, 255));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String formatQuantite(java.math.BigDecimal quantite) {
        return quantite != null ? quantite.stripTrailingZeros().toPlainString() : "0";
    }
}
