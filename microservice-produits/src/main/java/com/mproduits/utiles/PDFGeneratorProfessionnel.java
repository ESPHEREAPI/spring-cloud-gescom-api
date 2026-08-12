package com.mproduits.utiles;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mproduits.dto.RecuPaiementDTO;
import com.mproduits.model.Devis;
import com.mproduits.model.DevisItem;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Facture;
import com.mproduits.model.FactureItem;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.security.TenantContext;
import com.mproduits.services.EntrepriseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * Générateur de PDF PROFESSIONNEL pour les factures
 *
 * Génère une facture complète prête pour la comptabilité avec : - Informations
 * complètes de l'entreprise - Zone de signature et cachet - Mentions légales -
 * Mise en page professionnelle
 *
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 2.0 - Version Professionnelle
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PDFGeneratorProfessionnel {

    private final EntrepriseRepositories entrepriseRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;
    private final ResourceLoader resourceLoader;
    private Entreprise entreprise;

    // Polices
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
    private static final Font FONT_COMPANY = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(0, 51, 102));
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

    // Couleurs
    private static final BaseColor COLOR_PRIMARY = new BaseColor(0, 51, 102); // Bleu foncé
    private static final BaseColor COLOR_SECONDARY = new BaseColor(41, 128, 185); // Bleu clair
    private static final BaseColor COLOR_GRAY_LIGHT = new BaseColor(245, 245, 245);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère le PDF d'une facture professionnelle
     *
     * @param facture La facture à générer
     * @return Le contenu du PDF en bytes
     */
    private Entreprise getEntrepriseConfig() {
        return entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
    }

    public byte[] genererFacturePDF(Facture facture) {
        log.info("Génération du PDF professionnel pour la facture: {}", facture.getNumeroFacture());
        this.entreprise = getEntrepriseConfig();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Ajouter pied de page avec numéro de page
            writer.setPageEvent(new PageNumberEvent(entreprise));

            document.open();

            // ==================== EN-TÊTE ====================
            addEntrepriseHeader(document, facture);

            document.add(new Paragraph("\n"));

            // ==================== INFORMATIONS FACTURE ====================
            addFactureInfo(document, facture);

            document.add(new Paragraph("\n"));

            // ==================== TABLEAU DES LIGNES ====================
            addLignesTable(document, facture);

            document.add(new Paragraph("\n"));

            // ==================== TOTAUX ====================
            addTotaux(document, facture);

            document.add(new Paragraph("\n"));

            // ==================== CONDITIONS ET REMARQUES ====================
            if (facture.getConditionsPaiement() != null && !facture.getConditionsPaiement().isEmpty()) {
                addConditionsPaiement(document, facture);
            }

            if (facture.getRemarques() != null && !facture.getRemarques().isEmpty()) {
                addRemarques(document, facture);
            }

            // ==================== ZONE SIGNATURE ET CACHET ====================
            addSignatureZone(document);

            // ==================== MENTIONS LÉGALES ====================
            addMentionsLegales(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Génère le PDF professionnel d'un reçu de paiement (versement client),
     * dans le meme style que la facture (en-tete entreprise reel, tableaux
     * bordes, zone signature) - remplace l'ancienne version en Paragraph
     * brut de RecuPaiementService qui utilisait en plus des constantes
     * d'entreprise en dur ("Votre Entreprise", email placeholder, etc.).
     */
    public byte[] genererRecuPaiementPDF(RecuPaiementDTO recu) {
        log.info("Génération du PDF professionnel du reçu: {}", recu.getNumeroRecu());
        this.entreprise = getEntrepriseConfig();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new PageNumberEvent(entreprise));

            document.open();

            addRecuHeader(document, recu);
            document.add(new Paragraph("\n"));

            addRecuPaiementDetails(document, recu);
            document.add(new Paragraph("\n"));

            addRecuMontant(document, recu);

            if (recu.getFactureSoldeRestant() != null) {
                document.add(new Paragraph("\n"));
                addRecuSolde(document, recu);
            }

            addSignatureZone(document);
            addMentionsLegales(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF du reçu", e);
            throw new RuntimeException("Erreur lors de la génération du PDF du reçu: " + e.getMessage(), e);
        }
    }

    /**
     * Génère le PDF professionnel d'un devis, dans le meme style que la
     * facture. Aucun endpoint ne generait ce PDF auparavant - le bouton
     * "Telecharger PDF" du frontend (devis-list) appelait une route
     * inexistante cote backend (500/404 selon le point d'entree).
     */
    public byte[] genererDevisPDF(Devis devis) {
        log.info("Génération du PDF professionnel pour le devis: {}", devis.getNumeroDevis());
        this.entreprise = getEntrepriseConfig();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new PageNumberEvent(entreprise));

            document.open();

            addDevisHeader(document, devis);
            document.add(new Paragraph("\n"));

            addDevisLignesTable(document, devis);
            document.add(new Paragraph("\n"));

            addDevisTotaux(document, devis);
            document.add(new Paragraph("\n"));

            if (devis.getConditions() != null && !devis.getConditions().isEmpty()) {
                Paragraph condTitle = new Paragraph("Conditions", FONT_LABEL);
                condTitle.setSpacingBefore(10);
                document.add(condTitle);
                document.add(new Paragraph(devis.getConditions(), FONT_NORMAL));
            }

            if (devis.getRemarques() != null && !devis.getRemarques().isEmpty()) {
                Paragraph remTitle = new Paragraph("Remarques", FONT_LABEL);
                remTitle.setSpacingBefore(10);
                document.add(remTitle);
                document.add(new Paragraph(devis.getRemarques(), FONT_NORMAL));
            }

            addSignatureZone(document);
            addMentionsLegales(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF du devis", e);
            throw new RuntimeException("Erreur lors de la génération du PDF du devis: " + e.getMessage(), e);
        }
    }

    private void addDevisHeader(Document document, Devis devis) throws DocumentException {
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.2f, 1});

        // ========== COLONNE GAUCHE : ENTREPRISE ==========
        PdfPCell entrepriseCell = new PdfPCell();
        entrepriseCell.setBorder(Rectangle.NO_BORDER);
        entrepriseCell.setPaddingRight(10);

        Paragraph companyName = new Paragraph(entreprise.getCompagnie().getNom(), FONT_COMPANY);
        companyName.setSpacingAfter(5);
        entrepriseCell.addElement(companyName);

        entrepriseCell.addElement(new Paragraph("Tél: " + entreprise.getCompagnie().getTel(), FONT_NORMAL));
        if (entreprise.getCompagnie().getEmail() != null) {
            entrepriseCell.addElement(new Paragraph("Email: " + entreprise.getCompagnie().getEmail(), FONT_NORMAL));
        }
        entrepriseCell.addElement(new Paragraph(" ", FONT_SMALL));
        entrepriseCell.addElement(new Paragraph("RCCM: " + entreprise.getCompagnie().getRccm(), FONT_SMALL));
        entrepriseCell.addElement(new Paragraph("NIU: " + entreprise.getCompagnie().getNui(), FONT_SMALL));

        mainTable.addCell(entrepriseCell);

        // ========== COLONNE DROITE : TITRE, NUMERO, CLIENT ==========
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(10);

        Paragraph devisTitle = new Paragraph("DEVIS", FONT_TITLE);
        devisTitle.setAlignment(Element.ALIGN_RIGHT);
        devisTitle.setSpacingAfter(5);
        rightCell.addElement(devisTitle);

        PdfPTable numeroTable = new PdfPTable(1);
        numeroTable.setWidthPercentage(100);
        numeroTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell numeroCell = new PdfPCell(new Phrase(devis.getNumeroDevis(), FONT_BOLD));
        numeroCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        numeroCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numeroCell.setPadding(8);
        numeroCell.setBorderColor(COLOR_PRIMARY);
        numeroCell.setBorderWidth(2);
        numeroTable.addCell(numeroCell);
        rightCell.addElement(numeroTable);

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));

        addKeyValue(rightCell, "Date d'émission:", devis.getDateDevis().toString());
        if (devis.getDateExpiration() != null) {
            addKeyValue(rightCell, "Valable jusqu'au:", devis.getDateExpiration().toString());
        }
        addKeyValue(rightCell, "Statut:", devis.getStatut().getLibelle());

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));

        PdfPTable clientTable = new PdfPTable(1);
        clientTable.setWidthPercentage(100);
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        clientCell.setPadding(8);
        clientCell.setBorderColor(COLOR_SECONDARY);
        clientCell.setBorderWidth(1);

        Paragraph clientTitle = new Paragraph("ADRESSÉ À", FONT_LABEL);
        clientTitle.setSpacingAfter(5);
        clientCell.addElement(clientTitle);

        clientCell.addElement(new Paragraph(devis.getClient().getNom(), FONT_BOLD));
        if (devis.getClient().getAdresse() != null) {
            clientCell.addElement(new Paragraph(devis.getClient().getAdresse(), FONT_NORMAL));
        }
        if (devis.getClient().getTelephone() != null) {
            clientCell.addElement(new Paragraph("Tél: " + devis.getClient().getTelephone(), FONT_NORMAL));
        }
        if (devis.getClient().getEmail() != null) {
            clientCell.addElement(new Paragraph("Email: " + devis.getClient().getEmail(), FONT_NORMAL));
        }

        clientTable.addCell(clientCell);
        rightCell.addElement(clientTable);

        mainTable.addCell(rightCell);
        document.add(mainTable);

        LineSeparator line = new LineSeparator(2, 100, COLOR_PRIMARY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));
    }

    private void addDevisLignesTable(Document document, Devis devis) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 3f, 1f, 1.2f, 1f, 1.5f});
        table.setSpacingBefore(10);

        String[] headers = {"N°", "Désignation", "Qté", "P.U. HT", "TVA %", "Total TTC"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        int numero = 1;
        for (DevisItem ligne : devis.getItems()) {
            addTableCellCenter(table, String.valueOf(numero++), FONT_NORMAL);
            addTableCellLeft(table, ligne.getProduitLibelle(), FONT_NORMAL);
            addTableCellCenter(table, String.valueOf(ligne.getQuantite()), FONT_NORMAL);
            addTableCellRight(table, formatMontant(ligne.getPrixUnitaire().doubleValue()), FONT_NORMAL);
            addTableCellCenter(table, ligne.getTauxTVA() + "%", FONT_NORMAL);
            addTableCellRight(table, formatMontant(ligne.getMontantTTC().doubleValue()), FONT_BOLD);
        }

        document.add(table);
    }

    private void addDevisTotaux(Document document, Devis devis) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(10);

        addTotalRow(table, "Total HT", formatMontant(devis.getMontantHT().doubleValue()) + " XAF", false);
        addTotalRow(table, "TVA", formatMontant(devis.getTotalTVA().doubleValue()) + " XAF", false);
        addTotalRow(table, "TOTAL TTC", formatMontant(devis.getTotal().doubleValue()) + " XAF", true);

        document.add(table);
    }

    private void addRecuHeader(Document document, RecuPaiementDTO recu) throws DocumentException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.2f, 1});

        // ========== COLONNE GAUCHE : ENTREPRISE ==========
        PdfPCell entrepriseCell = new PdfPCell();
        entrepriseCell.setBorder(Rectangle.NO_BORDER);
        entrepriseCell.setPaddingRight(10);

        Paragraph companyName = new Paragraph(entreprise.getCompagnie().getNom(), FONT_COMPANY);
        companyName.setSpacingAfter(5);
        entrepriseCell.addElement(companyName);

        entrepriseCell.addElement(new Paragraph("Tél: " + entreprise.getCompagnie().getTel(), FONT_NORMAL));
        if (entreprise.getCompagnie().getEmail() != null) {
            entrepriseCell.addElement(new Paragraph("Email: " + entreprise.getCompagnie().getEmail(), FONT_NORMAL));
        }
        entrepriseCell.addElement(new Paragraph(" ", FONT_SMALL));
        entrepriseCell.addElement(new Paragraph("RCCM: " + entreprise.getCompagnie().getRccm(), FONT_SMALL));
        entrepriseCell.addElement(new Paragraph("NIU: " + entreprise.getCompagnie().getNui(), FONT_SMALL));

        mainTable.addCell(entrepriseCell);

        // ========== COLONNE DROITE : TITRE, NUMERO, CLIENT ==========
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(10);

        Paragraph titre = new Paragraph("REÇU DE PAIEMENT", FONT_TITLE);
        titre.setAlignment(Element.ALIGN_RIGHT);
        titre.setSpacingAfter(5);
        rightCell.addElement(titre);

        PdfPTable numeroTable = new PdfPTable(1);
        numeroTable.setWidthPercentage(100);
        numeroTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell numeroCell = new PdfPCell(new Phrase(recu.getNumeroRecu(), FONT_BOLD));
        numeroCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        numeroCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numeroCell.setPadding(8);
        numeroCell.setBorderColor(COLOR_PRIMARY);
        numeroCell.setBorderWidth(2);
        numeroTable.addCell(numeroCell);
        rightCell.addElement(numeroTable);

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));
        addKeyValue(rightCell, "Date:", sdf.format(recu.getDateEmission()));

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));

        PdfPTable clientTable = new PdfPTable(1);
        clientTable.setWidthPercentage(100);
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        clientCell.setPadding(8);
        clientCell.setBorderColor(COLOR_SECONDARY);
        clientCell.setBorderWidth(1);

        Paragraph clientTitle = new Paragraph("REÇU DE", FONT_LABEL);
        clientTitle.setSpacingAfter(5);
        clientCell.addElement(clientTitle);

        clientCell.addElement(new Paragraph(recu.getClientNom(), FONT_BOLD));
        if (recu.getClientAdresse() != null) {
            clientCell.addElement(new Paragraph(recu.getClientAdresse(), FONT_NORMAL));
        }
        if (recu.getClientTelephone() != null) {
            clientCell.addElement(new Paragraph("Tél: " + recu.getClientTelephone(), FONT_NORMAL));
        }

        clientTable.addCell(clientCell);
        rightCell.addElement(clientTable);

        mainTable.addCell(rightCell);
        document.add(mainTable);

        LineSeparator line = new LineSeparator(2, 100, COLOR_PRIMARY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));
    }

    private void addRecuPaiementDetails(Document document, RecuPaiementDTO recu) throws DocumentException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.3f, 2f});
        table.setSpacingBefore(10);

        String[][] lignes = {
            {"Facture N°", recu.getFactureNumero()},
            {"Date de versement", sdf.format(recu.getDateVersement())},
            {"Mode de paiement", recu.getModePaiement()},
            {"Référence", recu.getReferencePaiement() != null ? recu.getReferencePaiement() : "-"}
        };

        for (String[] ligne : lignes) {
            PdfPCell labelCell = new PdfPCell(new Phrase(ligne[0], FONT_LABEL));
            labelCell.setPadding(6);
            labelCell.setBackgroundColor(COLOR_GRAY_LIGHT);
            labelCell.setBorderColor(BaseColor.WHITE);
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(ligne[1], FONT_NORMAL));
            valueCell.setPadding(6);
            valueCell.setBorderColor(COLOR_GRAY_LIGHT);
            table.addCell(valueCell);
        }

        document.add(table);
    }

    private void addRecuMontant(Document document, RecuPaiementDTO recu) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_GRAY_LIGHT);
        cell.setBorderColor(COLOR_PRIMARY);
        cell.setBorderWidth(2);
        cell.setPadding(12);

        Font fontMontant = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARY);
        Paragraph montant = new Paragraph(
                "MONTANT VERSÉ : " + formatMontant(recu.getMontant().doubleValue()) + " XAF", fontMontant);
        montant.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(montant);

        Paragraph lettres = new Paragraph(recu.getMontantEnLettres(), FONT_SMALL);
        lettres.setAlignment(Element.ALIGN_CENTER);
        lettres.setSpacingBefore(5);
        cell.addElement(lettres);

        table.addCell(cell);
        document.add(table);
    }

    private void addRecuSolde(Document document, RecuPaiementDTO recu) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(5);

        addTotalRow(table, "Total facture", formatMontant(recu.getFactureTotalTtc().doubleValue()) + " XAF", false);
        addTotalRow(table, "SOLDE RESTANT", formatMontant(recu.getFactureSoldeRestant().doubleValue()) + " XAF", true);

        document.add(table);
    }

    /**
     * Ajoute l'en-tête complet avec informations de l'entreprise et client
     */
    private void addEntrepriseHeader(Document document, Facture facture) throws DocumentException {
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.2f, 1});

        // ========== COLONNE GAUCHE : ENTREPRISE ==========
        PdfPCell entrepriseCell = new PdfPCell();
        entrepriseCell.setBorder(Rectangle.NO_BORDER);
        entrepriseCell.setPaddingRight(10);

        // Logo (si disponible)
        try {
            Resource logoResource = resourceLoader.getResource("logo");
            if (logoResource.exists()) {
                Image logo = Image.getInstance(logoResource.getURL());
                logo.scaleToFit(80, 80);
                entrepriseCell.addElement(logo);
                entrepriseCell.addElement(new Paragraph("\n", FONT_SMALL));
            }
        } catch (Exception e) {
            log.debug("Logo non disponible");
        }

        // Nom de l'entreprise
        Paragraph companyName = new Paragraph(entreprise.getCompagnie().getNom(), FONT_COMPANY);
        companyName.setSpacingAfter(5);
        entrepriseCell.addElement(companyName);

        // Forme juridique et capital
        Paragraph juridique = new Paragraph(
                "ETS - Capital: " + (entreprise.getCapital() != null ? entreprise.getCapital() : "10 000 000 XFA"),
                FONT_SMALL);
        juridique.setSpacingAfter(3);
        entrepriseCell.addElement(juridique);

        // Adresse
        entrepriseCell.addElement(new Paragraph(""+entreprise.getCompagnie().getTel(), FONT_NORMAL));
        entrepriseCell.addElement(new Paragraph("", FONT_NORMAL));
        entrepriseCell.addElement(new Paragraph(" ", FONT_SMALL));

        // Contact
        entrepriseCell.addElement(new Paragraph("Tél: " + entreprise.getCompagnie().getTel(), FONT_NORMAL));
//        if (entrepriseConfig.getTelephoneSecondaire() != null) {
//            entrepriseCell.addElement(new Paragraph("     " + entrepriseConfig.getTelephoneSecondaire(), FONT_NORMAL));
//        }
        if (entreprise.getCompagnie().getEmail() != null) {
            entrepriseCell.addElement(new Paragraph("Email: " + entreprise.getCompagnie().getEmail(), FONT_NORMAL));
        }
        if (entreprise.getSiteWeb() != null) {
            entrepriseCell.addElement(new Paragraph("Web: " + entreprise.getSiteWeb(), FONT_NORMAL));
        }
        entrepriseCell.addElement(new Paragraph(" ", FONT_SMALL));

        // Identifiants
        entrepriseCell.addElement(new Paragraph("RCCM: " + entreprise.getCompagnie().getRccm(), FONT_SMALL));
        entrepriseCell.addElement(new Paragraph("NIU: " + entreprise.getCompagnie().getNui(), FONT_SMALL));
        if (entreprise.getCompagnie().getNumeroContribuable() != null) {
            entrepriseCell.addElement(new Paragraph("N° Contribuable: " +  entreprise.getCompagnie().getNumeroContribuable(), FONT_SMALL));
        }

        mainTable.addCell(entrepriseCell);

        // ========== COLONNE DROITE : CLIENT ET FACTURE ==========
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(10);

        // Titre FACTURE
        Paragraph factureTitle = new Paragraph("FACTURE", FONT_TITLE);
        factureTitle.setAlignment(Element.ALIGN_RIGHT);
        factureTitle.setSpacingAfter(5);
        rightCell.addElement(factureTitle);

        // Numéro de facture dans un encadré
        PdfPTable numeroTable = new PdfPTable(1);
        numeroTable.setWidthPercentage(100);
        numeroTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell numeroCell = new PdfPCell(new Phrase(facture.getNumeroFacture(), FONT_BOLD));
        numeroCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        numeroCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numeroCell.setPadding(8);
        numeroCell.setBorderColor(COLOR_PRIMARY);
        numeroCell.setBorderWidth(2);
        numeroTable.addCell(numeroCell);
        rightCell.addElement(numeroTable);

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));

        // Dates
        addKeyValue(rightCell, "Date d'émission:", facture.getDateFacture().toString());
        addKeyValue(rightCell, "Date d'échéance:", facture.getDateEcheance().toString());
        addKeyValue(rightCell, "Statut:", facture.getStatut().getLibelle());

        rightCell.addElement(new Paragraph("\n", FONT_SMALL));

        // Client dans un encadré
        PdfPTable clientTable = new PdfPTable(1);
        clientTable.setWidthPercentage(100);
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBackgroundColor(COLOR_GRAY_LIGHT);
        clientCell.setPadding(8);
        clientCell.setBorderColor(COLOR_SECONDARY);
        clientCell.setBorderWidth(1);

        Paragraph clientTitle = new Paragraph("FACTURÉ À", FONT_LABEL);
        clientTitle.setSpacingAfter(5);
        clientCell.addElement(clientTitle);

        clientCell.addElement(new Paragraph(facture.getClient().getNom(), FONT_BOLD));
        if (facture.getClient().getAdresse() != null) {
            clientCell.addElement(new Paragraph(facture.getClient().getAdresse(), FONT_NORMAL));
        }
        if (facture.getClient().getTelephone() != null) {
            clientCell.addElement(new Paragraph("Tél: " + facture.getClient().getTelephone(), FONT_NORMAL));
        }
        if (facture.getClient().getEmail() != null) {
            clientCell.addElement(new Paragraph("Email: " + facture.getClient().getEmail(), FONT_NORMAL));
        }

        clientTable.addCell(clientCell);
        rightCell.addElement(clientTable);

        mainTable.addCell(rightCell);

        document.add(mainTable);

        // Ligne de séparation
        LineSeparator line = new LineSeparator(2, 100, COLOR_PRIMARY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));
    }

    /**
     * Ajoute les informations complémentaires de la facture
     */
    private void addFactureInfo(Document document, Facture facture) throws DocumentException {
        // Informations supplémentaires si nécessaires
        // (Référence commande, etc.)
    }

    /**
     * Ajoute le tableau des lignes de facture
     */
    private void addLignesTable(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 3f, 1f, 1.2f, 1f, 1.5f});
        table.setSpacingBefore(10);

        // En-tête du tableau
        String[] headers = {"N°", "Désignation", "Qté", "P.U. HT", "TVA %", "Total TTC"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        // Lignes de facture
        int numero = 1;
        for (FactureItem ligne : facture.getItems()) {
            // Numéro
            addTableCellCenter(table, String.valueOf(numero++), FONT_NORMAL);

            // Désignation
            addTableCellLeft(table, ligne.getProduit().getLibelle(), FONT_NORMAL);

            // Quantité
            addTableCellCenter(table, ligne.getQuantite().toString(), FONT_NORMAL);

            // Prix unitaire
            addTableCellRight(table, formatMontant(ligne.getPrixUnitaireTTC().doubleValue()), FONT_NORMAL);

            // TVA
            addTableCellCenter(table, ligne.getTauxTVA() + "%", FONT_NORMAL);

            // Total TTC
            addTableCellRight(table, formatMontant(ligne.getMontantTTC().doubleValue()), FONT_BOLD);
        }

        document.add(table);
    }

    /**
     * Ajoute les totaux
     */
    private void addTotaux(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(10);

        // Total HT
        addTotalRow(table, "Total HT", formatMontant(facture.getTotalHt().doubleValue()) + " XAF", false);

        // TVA
        Double montantTVA = facture.getTotalTVA().doubleValue();
        addTotalRow(table, "TVA", formatMontant(montantTVA) + " XAF", false);

        // Total TTC
        addTotalRow(table, "TOTAL TTC", formatMontant(facture.getTotalTtc().doubleValue()) + " XAF", true);

        // Montant payé
        if (facture.getMontantPaye().doubleValue() > 0) {
            addTotalRow(table, "Montant payé", formatMontant(facture.getMontantPaye().doubleValue()) + " XAF", false);

            // Solde restant
            addTotalRow(table, "SOLDE RESTANT", formatMontant(facture.getSoldeRestant().doubleValue()) + " XAF", true);
        }

        // Informations bancaires
        if (facture.getSoldeRestant().doubleValue() > 0) {
            PdfPCell emptyCell = new PdfPCell(new Phrase(" ", FONT_SMALL));
            emptyCell.setColspan(2);
            emptyCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(emptyCell);

            PdfPCell banqueCell = new PdfPCell();
            banqueCell.setColspan(2);
            banqueCell.setBorder(Rectangle.NO_BORDER);
            banqueCell.setBackgroundColor(COLOR_GRAY_LIGHT);
            banqueCell.setPadding(8);

            banqueCell.addElement(new Paragraph("Coordonnées bancaires", FONT_LABEL));
            banqueCell.addElement(new Paragraph("", FONT_SMALL));
          //  banqueCell.addElement(new Paragraph("IBAN: " + entrepriseConfig.getIban(), FONT_SMALL));

            table.addCell(banqueCell);
        }

        document.add(table);
    }

    /**
     * Ajoute une ligne de total
     */
    private void addTotalRow(PdfPTable table, String label, String value, boolean highlighted) {
        Font font = highlighted ? FONT_BOLD : FONT_NORMAL;
        BaseColor bgColor = highlighted ? COLOR_GRAY_LIGHT : BaseColor.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(bgColor);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        valueCell.setBackgroundColor(bgColor);
        if (highlighted) {
            valueCell.setBorderColor(COLOR_PRIMARY);
            valueCell.setBorderWidth(1);
            valueCell.setBorder(Rectangle.BOX);
        }
        table.addCell(valueCell);
    }

    /**
     * Ajoute les conditions de paiement
     */
    private void addConditionsPaiement(Document document, Facture facture) throws DocumentException {
        Paragraph title = new Paragraph("Conditions de paiement", FONT_LABEL);
        title.setSpacingBefore(10);
        document.add(title);

        Paragraph content = new Paragraph(facture.getConditionsPaiement(), FONT_NORMAL);
        content.setSpacingAfter(5);
        document.add(content);
    }

    /**
     * Ajoute les remarques
     */
    private void addRemarques(Document document, Facture facture) throws DocumentException {
        Paragraph title = new Paragraph("Remarques", FONT_LABEL);
        title.setSpacingBefore(10);
        document.add(title);

        Paragraph content = new Paragraph(facture.getRemarques(), FONT_NORMAL);
        content.setSpacingAfter(5);
        document.add(content);
    }

    /**
     * Ajoute la zone de signature et cachet
     */
    private void addSignatureZone(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{1, 1});

        // Colonne gauche : Cachet
        PdfPCell cachetCell = new PdfPCell();
        cachetCell.setBorder(Rectangle.NO_BORDER);
        cachetCell.setPaddingRight(20);

        Paragraph cachetTitle = new Paragraph("Cachet de l'entreprise", FONT_LABEL);
        cachetTitle.setAlignment(Element.ALIGN_CENTER);
        cachetCell.addElement(cachetTitle);

        // Zone pour le cachet
        PdfPTable cachetBox = new PdfPTable(1);
        cachetBox.setWidthPercentage(80);
        cachetBox.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell cachetBoxCell = new PdfPCell(new Phrase("\n\n\n\n", FONT_NORMAL));
        cachetBoxCell.setFixedHeight(80);
        cachetBoxCell.setBorderColor(COLOR_SECONDARY);
        cachetBoxCell.setBorderWidth(1);
        cachetBoxCell.setBackgroundColor(new BaseColor(250, 250, 250));
        cachetBox.addCell(cachetBoxCell);
        cachetCell.addElement(cachetBox);

        signatureTable.addCell(cachetCell);

        // Colonne droite : Signature
        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);
        signatureCell.setPaddingLeft(20);

        Paragraph signatureTitle = new Paragraph("Service Commercial", FONT_LABEL);
        signatureTitle.setAlignment(Element.ALIGN_CENTER);
        signatureCell.addElement(signatureTitle);

        // Zone pour la signature
        PdfPTable signatureBox = new PdfPTable(1);
        signatureBox.setWidthPercentage(80);
        signatureBox.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell signatureBoxCell = new PdfPCell();
        signatureBoxCell.setFixedHeight(80);
        signatureBoxCell.setBorderColor(COLOR_SECONDARY);
        signatureBoxCell.setBorderWidth(1);
        signatureBoxCell.setBackgroundColor(new BaseColor(250, 250, 250));

        // Essayer d'ajouter l'image de signature si disponible
        try {
            Resource signatureResource = resourceLoader.getResource("");
            if (signatureResource.exists()) {
                Image signature = Image.getInstance(signatureResource.getURL());
                signature.scaleToFit(150, 70);
                signature.setAlignment(Element.ALIGN_CENTER);
                signatureBoxCell.addElement(signature);
            } else {
                signatureBoxCell.addElement(new Phrase("\n\n\n\n", FONT_NORMAL));
            }
        } catch (Exception e) {
            signatureBoxCell.addElement(new Phrase("\n\n\n\n", FONT_NORMAL));
        }

        signatureBox.addCell(signatureBoxCell);
        signatureCell.addElement(signatureBox);

        Paragraph signataireName = new Paragraph(entreprise.getDirecteur(), FONT_NORMAL);
        signataireName.setAlignment(Element.ALIGN_CENTER);
        signataireName.setSpacingBefore(5);
        signatureCell.addElement(signataireName);

        signatureTable.addCell(signatureCell);

        document.add(signatureTable);
    }

    /**
     * Ajoute les mentions légales en pied de page
     */
    private void addMentionsLegales(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));

        LineSeparator line = new LineSeparator(1, 100, COLOR_SECONDARY, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));

        Paragraph mentions = new Paragraph("", FONT_SMALL);
        mentions.setAlignment(Element.ALIGN_CENTER);
        mentions.setSpacingBefore(5);
        document.add(mentions);
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    private void addKeyValue(PdfPCell cell, String key, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(key + " ", FONT_LABEL));
        p.add(new Chunk(value, FONT_NORMAL));
        p.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(p);
    }

    private void addTableCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_GRAY_LIGHT);
        table.addCell(cell);
    }

    private void addTableCellLeft(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_GRAY_LIGHT);
        table.addCell(cell);
    }

    private void addTableCellRight(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_GRAY_LIGHT);
        table.addCell(cell);
    }

    private String formatMontant(Double montant) {
        if (montant == null) {
            return "0";
        }
        return String.format("%,d", montant.longValue());
    }

    /**
     * Event pour ajouter le numéro de page et informations en pied de page
     */
    private static class PageNumberEvent extends PdfPageEventHelper {

        private final Entreprise config;

        public PageNumberEvent(Entreprise config) {
            this.config = config;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(3);
            try {
                footer.setWidths(new int[]{1, 1, 1});
                footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
                footer.setLockedWidth(true);

                // Gauche : Entreprise
                PdfPCell left = new PdfPCell(new Phrase(config.getCompagnie().getNom(), FONT_SMALL));
                left.setBorder(Rectangle.NO_BORDER);
                left.setHorizontalAlignment(Element.ALIGN_LEFT);
                footer.addCell(left);

                // Centre : Date
                PdfPCell center = new PdfPCell(new Phrase("Document généré le "
                        + java.time.LocalDate.now().format(DATE_FORMATTER), FONT_SMALL));
                center.setBorder(Rectangle.NO_BORDER);
                center.setHorizontalAlignment(Element.ALIGN_CENTER);
                footer.addCell(center);

                // Droite : Numéro de page
                PdfPCell right = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), FONT_SMALL));
                right.setBorder(Rectangle.NO_BORDER);
                right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(right);

                footer.writeSelectedRows(0, -1, document.leftMargin(),
                        document.bottomMargin() - 10, writer.getDirectContent());
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
