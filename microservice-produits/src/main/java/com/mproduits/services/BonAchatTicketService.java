package com.mproduits.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfWriter;
import com.mproduits.model.BonAchat;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * Genere le ticket imprimable (format POS-80, comme un ticket de caisse) d'un
 * bon d'achat, avec un code-barres Code128 du code en bas - scannable en
 * caisse plutot que de devoir retaper le code a la main.
 */
@Service
public class BonAchatTicketService {

    public byte[] genererTicket(BonAchat bon) {
        Rectangle pageSize = new Rectangle(226.77f, 320f); // 80mm
        Document document = new Document(pageSize, 10, 10, 10, 10);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            Paragraph titre = new Paragraph("BON D'ACHAT", titleFont);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(new Paragraph(" "));

            Paragraph montant = new Paragraph(formatMontant(bon.getMontantTotal()) + " FCFA", boldFont);
            montant.setAlignment(Element.ALIGN_CENTER);
            document.add(montant);
            document.add(new Paragraph(" "));

            document.add(ligne("Client:",
                    bon.getClientBonAchat() != null ? bon.getClientBonAchat().getNom() : "-", normalFont));
            if (bon.getDateExpiration() != null) {
                document.add(ligne("Valable jusqu'au:",
                        new SimpleDateFormat("dd/MM/yyyy").format(bon.getDateExpiration()), normalFont));
            }
            document.add(new Paragraph(" "));

            Barcode128 barcode = new Barcode128();
            barcode.setCode(bon.getCodeBon());
            barcode.setCodeType(Barcode128.CODE128);
            barcode.setBarHeight(35f);
            Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeImage);

            Paragraph footer = new Paragraph("A présenter en caisse", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(6);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du ticket bon d'achat", e);
        }
        return baos.toByteArray();
    }

    private Paragraph ligne(String label, String valeur, Font font) {
        return new Paragraph(label + " " + valeur, font);
    }

    private String formatMontant(BigDecimal montant) {
        return montant != null ? montant.toBigInteger().toString() : "0";
    }
}
