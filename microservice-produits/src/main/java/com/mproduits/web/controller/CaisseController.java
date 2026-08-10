/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfRectangle;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.mproduits.dto.ClientDto;
import com.mproduits.dto.ProduitDto;
import com.mproduits.dto.UserDTO;
import com.mproduits.dto.VenteDto;
import com.mproduits.model.Client;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Paiement;
import com.mproduits.model.Vente;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.BarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Paths;
import java.sql.Date;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
public class CaisseController {

    @Autowired
    BarcodeService barcodeService;
    @Autowired
    BoutiqueAccessGuard boutiqueAccessGuard;

    @GetMapping("/top-articles/{boutiqueid}")
    @Operation(summary = "Articles populaires",
            description = "Retourne les 50 articles les plus populaires pour l'affichage initial")
    @ApiResponse(responseCode = "200", description = "Liste des articles populaires")
    public ResponseEntity<List<ProduitDto>> getTopArticles(@PathVariable(name = "boutiqueid") long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<ProduitDto> articles = barcodeService.getTopArticles(boutiqueid);

        // Cache plus long pour les articles populaires
        CacheControl cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES)
                .cachePublic();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(articles);
    }
    
    
    @GetMapping("/all-client")
    public ResponseEntity<List<ClientDto>> allClients() {
        List<ClientDto> clientsDatDtos=barcodeService.allClient();

        return ResponseEntity.ok(clientsDatDtos);
    }
    
    @PostMapping("/create-client")
    public ResponseEntity<Client>  createClient(@RequestBody ClientDto clientDto) {
        Client cl=barcodeService.createClientCaisse(clientDto);

        return ResponseEntity.ok(cl);
    }


    @GetMapping("/barcode")
    public ResponseEntity<ProduitDto> getByBarcode(@RequestParam String barcode) {
        ProduitDto produits = barcodeService.chargeProduitByBarcode(barcode);

        return ResponseEntity.ok(produits);
    }

    @PostMapping("/vente/{numerocommande}")
    public ResponseEntity<Long> getByBarcode(@PathVariable(name = "numerocommande") long numerocommande,@RequestBody VenteDto venteDto) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(venteDto.getBoutiqueid());
        Long vendid =  numerocommande==0L ? barcodeService.valideVente(venteDto): barcodeService.valideVente(venteDto, numerocommande,venteDto.getBoutiqueid());
        return ResponseEntity.ok(vendid);
    }
     @PostMapping("/vente/e-com/{numerocommande}")
    public ResponseEntity<Long> getVenteByEcom(@PathVariable(name = "numerocommande") long numerocommande,@RequestBody VenteDto venteDto) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(venteDto.getBoutiqueid());
        Long vendid = barcodeService.valideVente(venteDto,numerocommande,venteDto.getBoutiqueid());
        return ResponseEntity.ok(vendid);
    }

    @GetMapping("/reference/{boutiqueid}")
    public ResponseEntity<ProduitDto> getProduitByReferenceSearch(@PathVariable Long boutiqueid,@RequestParam String reference) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        ProduitDto pdto = barcodeService.getArticlesByReference(reference,boutiqueid);
        return ResponseEntity.ok(pdto);

    }

    @GetMapping("/produit/{produitid}/{boutiqueid}")
    public ResponseEntity<ProduitDto> getProduitById(@PathVariable Long produitid,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        ProduitDto pdto = barcodeService.getArticlesByProduitId(produitid,boutiqueid);
        return ResponseEntity.ok(pdto);

    }

    // pour le ticket 
    @GetMapping("/ticket/{venteid}/download-ticket")
    public ResponseEntity<Resource> telechargerTicket(@PathVariable("venteid") Long venteId) {
        try {
            Vente vente = barcodeService.getVenteById(venteId);
            if ("".equals(vente.getCheminPatheTicket()) || vente.getCheminPatheTicket() == null) {
                barcodeService.createTicketCaisseTXT(vente);
                vente = barcodeService.getVenteById(venteId);
            }

            File fichierTxt = new File(vente.getCheminPatheTicket());

            java.nio.file.Path path = fichierTxt.toPath();
            Resource resource = new UrlResource(path.toUri());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(fichierTxt.getName()).build());
            headers.setContentType(MediaType.TEXT_PLAIN);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/tickets/{venteid}/download")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long venteid) throws DocumentException, IOException {
//    ByteArrayOutputStream out = new ByteArrayOutputStream();
//    Document document = new Document(PageSize.A6); // Format ticket
//    PdfWriter.getInstance(document, out);
//    document.open();
//
//    // Logo (optionnel)
//    try {
//        Image logo = Image.getInstance("C:/Users/USER01/cloud-config-gescom/logo/logo.png");
//        logo.scaleToFit(80, 200);
//        logo.setAlignment(Element.ALIGN_CENTER);
//        document.add(logo);
//    } catch (Exception e) {
//        // ignore si logo manquant
//        System.err.println("logo manquant");
//    }
//
//    // Titre
//    Font titleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
//    Paragraph title = new Paragraph("Ticket de Vente\n", titleFont);
//    title.setAlignment(Element.ALIGN_CENTER);
//    document.add(title);
//
//    // Infos client et vente
//      Vente vente=barcodeService.getVenteById(venteid);
//    document.add(new Paragraph("Vente N° : " + vente.getNumeroTicket()));
//    document.add(new Paragraph("Date : " + vente.getDateVente()));
//    document.add(new Paragraph("Client : Client de passage\n"));
//
//    // Ligne de séparation
//    LineSeparator ls = new LineSeparator();
//    document.add(new Chunk(ls));
//
//    // Tableau des articles
//    PdfPTable table = new PdfPTable(4); // 4 colonnes : Désignation, Qté, PU, Total
//    table.setWidthPercentage(100);
//    table.setWidths(new float[]{3, 1, 1, 1});
//
//    addTableHeader(table, "Désignation", "Qté", "PU", "Total");
//
//    // Exemples d’articles (à remplacer par vos données réelles)
        ////    List<ArticleVendu> articles = List.of(
////        new ArticleVendu("Savon", 2, 500),
////        new ArticleVendu("Brosse", 1, 1500)
////    );
//List<LigneVente> listeVentes=barcodeService.listeLigneVenteByVente(vente);
//    int total = 0;
//    for ( LigneVente lgv: listeVentes) {
//        int totalLigne = lgv.getQuantite().intValue() * lgv.getPrixUnitaire().intValue();
//        total += totalLigne;
//        table.addCell(lgv.getProduit().getLibelle());
//        table.addCell(String.valueOf( lgv.getQuantite().intValue()));
//        table.addCell(lgv.getPrixUnitaire().intValue() + " FCFA");
//        table.addCell(totalLigne + " FCFA");
//    }
//
//    document.add(table);
//
//    // Remise
//    int remise = 0;
//    int netAPayer = total - remise;
//
//    document.add(new Paragraph("\nTotal brut : " + total + " FCFA"));
//    document.add(new Paragraph("Remise : " + remise + " FCFA"));
//    document.add(new Paragraph("Net à payer : " + netAPayer + " FCFA\n"));
//
//    document.add(new Chunk(ls));
//    document.add(new Paragraph("Merci pour votre achat !", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));
//    document.close();
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_PDF);
//    headers.setContentDispositionFormData("attachment", "ticket-vente-" + venteid + ".pdf");
//
//    return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

  try {
            byte[] pdfBytes = genererTicketPDFPPOS(venteid);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "ticket-vente-" + venteid + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            // Log de l'erreur complète
            e.printStackTrace();

            // Retour d'erreur avec détails
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors de la génération du ticket: " + e.getMessage()).getBytes());
        }
    }

    private byte[] genererTicketPDF(Long venteid) throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Configuration du document avec marges personnalisées
        // Rectangle pageSize = new Rectangle(PageSize.A6);
        //  Document document = new Document(pageSize, 20, 20, 20, 20);
        // ✅ Utilise une largeur adaptée à l’imprimante Epson
        Rectangle pageSize = new Rectangle(226.77f, 1000f); // Pour 80mm
        Document document = new Document(pageSize, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        try {
            // Récupération des données avec vérification
            Vente vente = barcodeService.getVenteById(venteid);
            if (vente == null) {
                throw new RuntimeException("Vente introuvable avec l'ID: " + venteid);
            }

            List<LigneVente> listeVentes = barcodeService.listeLigneVenteByVente(vente);
            if (listeVentes == null) {
                listeVentes = new ArrayList<>();
            }

            // === SECTION HEADER ===
            ajouterHeader(document);

            // === INFORMATIONS VENTE ===
            ajouterInfosVente(document, vente);

            // === TABLEAU DES ARTICLES ===
            ajouterTableauArticles(document, listeVentes);

            // === TOTAUX ===
            ajouterTotaux(document, listeVentes);

            // === FOOTER ===
            ajouterFooter(document);

        } finally {
            document.close();
        }

        // return out.toByteArray();
        byte[] pdfPlein = out.toByteArray();
        byte[] pdfRogne = rognerHauteurPDF(pdfPlein);
        return pdfRogne;
    }

    /**
     * Ajoute le header avec logo et titre
     */
    private void ajouterHeader(Document document) throws DocumentException {
        try {
            // Tentative de chargement du logo
            // Paths.get(System.getProperty("user.home")
            String userHome = System.getProperty("user.home");

            // Construit le chemin complet
            String cheminLogo = Paths.get(userHome, "cloud-config-gescom", "logo", "logo.png").toString();
            Image logo = Image.getInstance(cheminLogo);
            logo.scaleToFit(60, 60);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(10f);
            document.add(logo);
        } catch (Exception e) {
            // Logo alternatif en cas d'erreur
            Font logoFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Paragraph logoText = new Paragraph("VOTRE ENTREPRISE", logoFont);
            logoText.setAlignment(Element.ALIGN_CENTER);
            logoText.setSpacingAfter(0f);
            document.add(logoText);
        }

        // Titre principal
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        titleFont.setColor(new BaseColor(33, 37, 41));

        Paragraph title = new Paragraph("TICKET DE VENTE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(0f);
        document.add(title);

        // Ligne de séparation
        ajouterLigneSeparation(document, 0.1f);
    }

    /**
     * Ajoute les informations de la vente
     */
    private void ajouterInfosVente(Document document, Vente vente) throws DocumentException {
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        // Formatage sécurisé de la date
        String dateFormatee = "N/A";
        if (vente.getDateVente() != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                dateFormatee = dateFormat.format(vente.getDateVente());
            } catch (Exception e) {
                dateFormatee = vente.getDateVente().toString();
            }
        }

        // Formatage sécurisé du numéro de ticket
        //String numeroTicket = "N/A";
        //if (vente.getNumeroTicket() != null) {
        String numeroTicket = vente.getNumeroTicket();
        // }

        // Création du tableau d'informations
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        infoTable.setSpacingBefore(0f);
        infoTable.setSpacingAfter(0f);

        // Ajout des informations
        ajouterCelluleInfo(infoTable, "N° Ticket:", numeroTicket, labelFont, infoFont);
        ajouterCelluleInfo(infoTable, "Date:", dateFormatee, labelFont, infoFont);
        ajouterCelluleInfo(infoTable, "Client:", "Client de passage", labelFont, infoFont);
        ajouterCelluleInfo(infoTable, "Caissier:", getCurrentUser(vente), labelFont, infoFont);

        document.add(infoTable);
        ajouterLigneSeparation(document, 0.1f);
    }

    /**
     * Ajoute le tableau des articles - VERSION CORRIGÉE
     */
    private void ajouterTableauArticles(Document document, List<LigneVente> listeVentes) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.5f, 1f, 1.5f, 1.5f});
        table.setSpacingBefore(0f);
        table.setSpacingAfter(0f);

        // En-têtes
        ajouterHeaderTableau(table, "Article", "Qté", "P.U.", "Total");

        // Ajout des lignes avec gestion d'erreur renforcée
        for (int i = 0; i < listeVentes.size(); i++) {
            LigneVente lgv = listeVentes.get(i);

            try {
                // Vérifications et conversions sécurisées
                String libelle = (lgv.getProduit() != null && lgv.getProduit().getLibelle() != null)
                        ? lgv.getProduit().getLibelle() : "Article inconnu";

                // CORRECTION PRINCIPALE : Gestion sécurisée des BigDecimal
                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = quantite.multiply(prixUnitaire);

                BaseColor backgroundColor = (i % 2 == 0) ? BaseColor.WHITE : new BaseColor(248, 249, 250);

                ajouterLigneArticle(table,
                        libelle,
                        formatQuantite(quantite), // Méthode sécurisée
                        formatMontant(prixUnitaire), // Méthode sécurisée
                        formatMontant(totalLigne), // Méthode sécurisée
                        backgroundColor
                );

            } catch (Exception e) {
                // En cas d'erreur sur une ligne, on ajoute une ligne d'erreur
                ajouterLigneArticle(table,
                        "Erreur ligne " + (i + 1),
                        "0",
                        "0 FCFA",
                        "0 FCFA",
                        BaseColor.LIGHT_GRAY
                );
            }
        }

        document.add(table);
    }

    /**
     * Ajoute la section des totaux - VERSION CORRIGÉE
     */
    private void ajouterTotaux(Document document, List<LigneVente> listeVentes) throws DocumentException {
        // Calcul sécurisé des totaux
        BigDecimal totalBrut = BigDecimal.ZERO;

        for (LigneVente lgv : listeVentes) {
            try {
                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = quantite.multiply(prixUnitaire);
                totalBrut = totalBrut.add(totalLigne);
            } catch (Exception e) {
                // Ignore les erreurs de calcul sur les lignes individuelles
                continue;
            }
        }

        LigneVente lv = (LigneVente) listeVentes.toArray()[0];
        String modePaiement = barcodeService.getModePaiementLabel(lv.getVente());

        BigDecimal remise = BigDecimal.ZERO;
        BigDecimal netAPayer = BigDecimal.ZERO;
        if (lv.getVente() != null && lv.getVente().getTotalRemise().intValue() != 0) {
            remise = lv.getVente().getTotalRemise();
            netAPayer = totalBrut.subtract(remise);
        } else {
            netAPayer = totalBrut;
        }

        // Tableau pour les totaux
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(70);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(0f);
        totalTable.setSpacingAfter(0f);
        totalTable.setWidths(new float[]{1, 1});

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font totalBoldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);

        // Lignes des totaux
        ajouterLigneTotal(totalTable, "payer par:", modePaiement, totalFont, false);
        ajouterLigneTotal(totalTable, "Total brut:", formatMontant(totalBrut), totalFont, false);

        if (remise.compareTo(BigDecimal.ZERO) > 0) {
            ajouterLigneTotal(totalTable, "Remise:", "-" + formatMontant(remise), totalFont, false);
        }

        ajouterLigneTotal(totalTable, "NET À PAYER:", formatMontant(netAPayer), totalBoldFont, true);

        document.add(totalTable);
    }

    /**
     * Ajoute le footer du ticket
     */
    private void ajouterFooter(Document document) throws DocumentException {
        ajouterLigneSeparation(document, 0.5f);

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
        footerFont.setColor(new BaseColor(108, 117, 125));

        Paragraph merci = new Paragraph("Merci pour votre achat !", footerFont);
        merci.setAlignment(Element.ALIGN_CENTER);
        merci.setSpacingBefore(0f);
        merci.setSpacingAfter(0f);
        document.add(merci);

        Font infoFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        infoFont.setColor(new BaseColor(108, 117, 125));

        Paragraph info = new Paragraph("Service client: 00 00 00 00\nwww.votre-site.com", infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        document.add(info);
    }

    // === MÉTHODES UTILITAIRES CORRIGÉES ===
    private void ajouterLigneSeparation(Document document, float epaisseur) throws DocumentException {
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(epaisseur);
        ls.setLineColor(new BaseColor(206, 212, 218));
        document.add(new Chunk(ls));
    }

    private void ajouterCelluleInfo(PdfPTable table, String label, String valeur, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(3f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(valeur, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(3f);
        table.addCell(valueCell);
    }

    private void ajouterHeaderTableau(PdfPTable table, String... headers) {
        Font headFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new BaseColor(52, 58, 64));
            cell.setPadding(6f);
            cell.setBorderWidth(0.5f);
            cell.setBorderColor(BaseColor.GRAY);
            table.addCell(cell);
        }
    }

    private void ajouterLigneArticle(PdfPTable table, String article, String quantite,
            String prixUnitaire, String total, BaseColor backgroundColor) {
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

        // Création des cellules avec gestion d'erreur
        ajouterCelluleTableau(table, article, Element.ALIGN_LEFT, backgroundColor, cellFont);
        ajouterCelluleTableau(table, quantite, Element.ALIGN_CENTER, backgroundColor, cellFont);
        ajouterCelluleTableau(table, prixUnitaire, Element.ALIGN_RIGHT, backgroundColor, cellFont);
        ajouterCelluleTableau(table, total, Element.ALIGN_RIGHT, backgroundColor, cellFont);
    }

    private void ajouterCelluleTableau(PdfPTable table, String contenu, int alignement,
            BaseColor backgroundColor, Font font) {
        try {
            PdfPCell cell = new PdfPCell(new Phrase(contenu != null ? contenu : "", font));
            cell.setHorizontalAlignment(alignement);
            cell.setBackgroundColor(backgroundColor);
            cell.setPadding(4f);
            cell.setBorderWidth(0.3f);
            cell.setBorderColor(new BaseColor(233, 236, 239));
            table.addCell(cell);
        } catch (Exception e) {
            // Cellule de secours en cas d'erreur
            PdfPCell errorCell = new PdfPCell(new Phrase("", font));
            errorCell.setHorizontalAlignment(alignement);
            errorCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            errorCell.setPadding(4f);
            table.addCell(errorCell);
        }
    }

    private void ajouterLigneTotal(PdfPTable table, String label, String montant, Font font, boolean isFinal) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(3f);

        PdfPCell montantCell = new PdfPCell(new Phrase(montant, font));
        montantCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        montantCell.setBorder(Rectangle.NO_BORDER);
        montantCell.setPaddingBottom(3f);

        if (isFinal) {
            labelCell.setBackgroundColor(new BaseColor(248, 249, 250));
            labelCell.setPadding(5f);
            labelCell.setBorder(Rectangle.BOX);
            labelCell.setBorderColor(new BaseColor(206, 212, 218));

            montantCell.setBackgroundColor(new BaseColor(248, 249, 250));
            montantCell.setPadding(5f);
            montantCell.setBorder(Rectangle.BOX);
            montantCell.setBorderColor(new BaseColor(206, 212, 218));
        }

        table.addCell(labelCell);
        table.addCell(montantCell);
    }

    /**
     * MÉTHODES DE FORMATAGE SÉCURISÉES - CORRECTION PRINCIPALE
     */
    private String formatMontant(BigDecimal montant) {
        if (montant == null) {
            return "0 FCFA";
        }

        try {
            DecimalFormat formatter = new DecimalFormat("#,##0");
            return formatter.format(montant) + " FCFA";
        } catch (Exception e) {
            // Fallback en cas d'erreur de formatage
            return montant.toString() + " FCFA";
        }
    }

    private String formatQuantite(BigDecimal quantite) {
        if (quantite == null) {
            return "0";
        }

        try {
            // Conversion sécurisée en entier si c'est un nombre entier
            if (quantite.scale() == 0 || quantite.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO)) {
                return String.valueOf(quantite.intValue());
            } else {
                return quantite.toString();
            }
        } catch (Exception e) {
            return quantite.toString();
        }
    }

    private String getCurrentUser(Vente v) {
        // À implémenter selon votre système d'authentification
        return v.getVendeur().getNom() + ' ' + v.getVendeur().getPrenom(); // Placeholder
    }

    private byte[] rognerHauteurPDF(byte[] pdfData) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final float[] maxY = {0f};

        // Parser pour texte + images
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(1, new RenderListener() {
            public void beginTextBlock() {
            }

            public void endTextBlock() {
            }

            public void renderText(TextRenderInfo textRenderInfo) {
                LineSegment baseline = textRenderInfo.getBaseline();
                if (baseline != null) {
                    float y = baseline.getStartPoint().get(1);
                    if (y > maxY[0]) {
                        maxY[0] = y;
                    }
                }
            }

            public void renderImage(ImageRenderInfo renderInfo) {
                try {
                    Matrix ctm = renderInfo.getImageCTM();
                    float imageY = ctm.get(Matrix.I31); // Position Y de l’image
                    float imageHeight = ctm.get(Matrix.I22); // Hauteur de l’image

                    float yTop = imageY + imageHeight;
                    if (yTop > maxY[0]) {
                        maxY[0] = yTop;
                    }
                    System.out.println("Hauteur max détectée (Y haut du contenu) : " + maxY[0]);
                    //if (maxY[0] < 180f) {
                    //   maxY[0] = 180f; // corrige pour prendre le logo en compte
                    // }
                    System.out.println("Hauteur max détectée (Y haut du contenu) : " + maxY[0]);
                } catch (Exception e) {
                    // Ignore en cas de souci de lecture image
                }
            }
        });

        // Lecture de la MediaBox actuelle
        PdfDictionary pageDict = reader.getPageN(1);
        PdfArray mediaBox = pageDict.getAsArray(PdfName.MEDIABOX);

        float llx = mediaBox.getAsNumber(0).floatValue();
        float lly = mediaBox.getAsNumber(1).floatValue();
        float urx = mediaBox.getAsNumber(2).floatValue();

        // Ajout d'une marge de sécurité pour ne pas couper haut/bas
        float ury = maxY[0] + 40f;
        ury = Math.max(ury, 250f);

        // Mise à jour de la MediaBox
        PdfStamper stamper = new PdfStamper(reader, out);
        reader.getPageN(1).put(PdfName.MEDIABOX, new PdfRectangle(llx, lly, urx, ury));
        stamper.close();
        reader.close();

        return out.toByteArray();
    }

    private byte[] genererTicketPDFOPTIMISE(Long venteid) throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // ✅ CORRECTION PRINCIPALE : Taille adaptée aux imprimantes thermiques Epson
        // Largeur : 80mm = 226.77 points
        // Hauteur : Commence petit, sera ajustée automatiquement
        Rectangle pageSize = new Rectangle(226.77f, 400f); // Hauteur réduite initialement

        // ✅ MARGES MINIMALES pour éviter les espaces vides
        Document document = new Document(pageSize, 5, 5, 5, 5); // Marges réduites à 5 points

        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        try {
            // Récupération des données avec vérification
            Vente vente = barcodeService.getVenteById(venteid);
            if (vente == null) {
                throw new RuntimeException("Vente introuvable avec l'ID: " + venteid);
            }

            List<LigneVente> listeVentes = barcodeService.listeLigneVenteByVente(vente);
            if (listeVentes == null) {
                listeVentes = new ArrayList<>();
            }

            // === SECTION HEADER ===
            ajouterHeaderOptimise(document);

            // === INFORMATIONS VENTE ===
            ajouterInfosVenteOptimise(document, vente);

            // === TABLEAU DES ARTICLES ===
            ajouterTableauArticlesOptimise(document, listeVentes);

            // === TOTAUX ===
            ajouterTotauxOptimise(document, listeVentes);

            // === FOOTER ===
            ajouterFooterOptimise(document);

        } finally {
            document.close();
        }

        // ✅ CORRECTION : Rognage optimisé pour imprimantes thermiques
        byte[] pdfPlein = out.toByteArray();
        byte[] pdfRogne = rognerHauteurPDFOptimise(pdfPlein);
        return pdfRogne;
    }

    /**
     * ✅ Header optimisé pour imprimantes thermiques
     */
    private void ajouterHeaderOptimise(Document document) throws DocumentException {
        try {
            // Logo plus petit pour imprimante thermique
            String userHome = System.getProperty("user.home");
            String cheminLogo = Paths.get(userHome, "cloud-config-gescom", "logo", "logo.png").toString();
            Image logo = Image.getInstance(cheminLogo);
            logo.scaleToFit(40, 40); // ✅ Taille réduite
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(2f); // ✅ Espacement réduit
            document.add(logo);
        } catch (Exception e) {
            // Logo alternatif compact
            Font logoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Paragraph logoText = new Paragraph("VOTRE ENTREPRISE", logoFont);
            logoText.setAlignment(Element.ALIGN_CENTER);
            logoText.setSpacingAfter(2f);
            document.add(logoText);
        }

        // Titre principal compact
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Paragraph title = new Paragraph("TICKET DE VENTE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(3f); // ✅ Espacement réduit
        document.add(title);

        // Ligne de séparation fine
        ajouterLigneSeparationOptimise(document);
    }

    /**
     * ✅ Informations vente optimisées
     */
    private void ajouterInfosVenteOptimise(Document document, Vente vente) throws DocumentException {
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL); // ✅ Police plus petite
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);

        // Formatage de la date
        String dateFormatee = "N/A";
        if (vente.getDateVente() != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm"); // ✅ Format court
                dateFormatee = dateFormat.format(vente.getDateVente());
            } catch (Exception e) {
                dateFormatee = vente.getDateVente().toString();
            }
        }

        String numeroTicket = vente.getNumeroTicket();

        // ✅ Tableau compact sans bordures
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1.5f});
        infoTable.setSpacingBefore(2f);
        infoTable.setSpacingAfter(2f);

        // Ajout des informations compactes
        ajouterCelluleInfoOptimise(infoTable, "N° Ticket:", numeroTicket, labelFont, infoFont);
        ajouterCelluleInfoOptimise(infoTable, "Date:", dateFormatee, labelFont, infoFont);
        ajouterCelluleInfoOptimise(infoTable, "Caissier:", getCurrentUser(vente), labelFont, infoFont);

        document.add(infoTable);
        ajouterLigneSeparationOptimise(document);
    }

    /**
     * ✅ Tableau articles optimisé pour imprimante thermique
     */
    private void ajouterTableauArticlesOptimise(Document document, List<LigneVente> listeVentes) throws DocumentException {
        // ✅ Layout spécial pour imprimante thermique (colonnes réduites)
        PdfPTable table = new PdfPTable(3); // ✅ 3 colonnes au lieu de 4
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1f, 1.5f}); // Article, Qté, Total
        table.setSpacingBefore(2f);
        table.setSpacingAfter(2f);

        // En-têtes compacts
        ajouterHeaderTableauOptimise(table, "Article", "Qté", "Total");

        // Ajout des lignes
        for (int i = 0; i < listeVentes.size(); i++) {
            LigneVente lgv = listeVentes.get(i);
            try {
                String libelle = (lgv.getProduit() != null && lgv.getProduit().getLibelle() != null)
                        ? lgv.getProduit().getLibelle() : "Article inconnu";

                // ✅ Troncature du libellé si trop long
                if (libelle.length() > 20) {
                    libelle = libelle.substring(0, 17) + "...";
                }

                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = quantite.multiply(prixUnitaire);

                BaseColor backgroundColor = (i % 2 == 0) ? BaseColor.WHITE : new BaseColor(248, 249, 250);

                ajouterLigneArticleOptimise(table,
                        libelle,
                        formatQuantite(quantite),
                        formatMontant(totalLigne),
                        backgroundColor
                );

            } catch (Exception e) {
                ajouterLigneArticleOptimise(table,
                        "Erreur ligne " + (i + 1),
                        "0",
                        "0 FCFA",
                        BaseColor.LIGHT_GRAY
                );
            }
        }

        document.add(table);
    }

    /**
     * ✅ Totaux optimisés
     */
    private void ajouterTotauxOptimise(Document document, List<LigneVente> listeVentes) throws DocumentException {
        // Calcul des totaux
        BigDecimal totalBrut = BigDecimal.ZERO;
        for (LigneVente lgv : listeVentes) {
            try {
                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = quantite.multiply(prixUnitaire);
                totalBrut = totalBrut.add(totalLigne);
            } catch (Exception e) {
                continue;
            }
        }

        LigneVente lv = (LigneVente) listeVentes.toArray()[0];
        String modePaiement = barcodeService.getModePaiementLabel(lv.getVente());

        BigDecimal remise = BigDecimal.ZERO;
        BigDecimal netAPayer = totalBrut;
        if (lv.getVente() != null && lv.getVente().getTotalRemise().intValue() != 0) {
            remise = lv.getVente().getTotalRemise();
            netAPayer = totalBrut.subtract(remise);
        }

        // ✅ Tableau total compact
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(3f);
        totalTable.setSpacingAfter(2f);
        totalTable.setWidths(new float[]{1, 1});

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        Font totalBoldFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        // Lignes des totaux
        ajouterLigneTotalOptimise(totalTable, "Mode paiement:", modePaiement, totalFont, false);

        if (remise.compareTo(BigDecimal.ZERO) > 0) {
            ajouterLigneTotalOptimise(totalTable, "Remise:", "-" + formatMontant(remise), totalFont, false);
        }

        ajouterLigneTotalOptimise(totalTable, "TOTAL:", formatMontant(netAPayer), totalBoldFont, true);

        document.add(totalTable);
    }

    /**
     * ✅ Footer optimisé
     */
    private void ajouterFooterOptimise(Document document) throws DocumentException {
        ajouterLigneSeparationOptimise(document);

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC);
        Paragraph merci = new Paragraph("Merci pour votre achat !", footerFont);
        merci.setAlignment(Element.ALIGN_CENTER);
        merci.setSpacingBefore(3f);
        merci.setSpacingAfter(2f);
        document.add(merci);

        Font infoFont = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL);
        Paragraph info = new Paragraph("Service client: 00 00 00 00", infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(5f);
        document.add(info);
    }

// === MÉTHODES UTILITAIRES OPTIMISÉES ===
    private void ajouterLigneSeparationOptimise(Document document) throws DocumentException {
        LineSeparator ls = new LineSeparator();
        ls.setLineWidth(0.5f);
        ls.setLineColor(BaseColor.GRAY);
        document.add(new Chunk(ls));
        document.add(Chunk.NEWLINE);
    }

    private void ajouterCelluleInfoOptimise(PdfPTable table, String label, String valeur, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(1f);
        labelCell.setPaddingTop(1f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(valeur, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(1f);
        valueCell.setPaddingTop(1f);
        table.addCell(valueCell);
    }

    private void ajouterHeaderTableauOptimise(PdfPTable table, String... headers) {
        Font headFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new BaseColor(230, 230, 230));
            cell.setPadding(2f);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
    }

    private void ajouterLigneArticleOptimise(PdfPTable table, String article, String quantite, String total, BaseColor backgroundColor) {
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL);

        // Article
        PdfPCell articleCell = new PdfPCell(new Phrase(article, cellFont));
        articleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        articleCell.setBackgroundColor(backgroundColor);
        articleCell.setPadding(2f);
        articleCell.setBorderWidth(0.3f);
        table.addCell(articleCell);

        // Quantité
        PdfPCell qteCell = new PdfPCell(new Phrase(quantite, cellFont));
        qteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qteCell.setBackgroundColor(backgroundColor);
        qteCell.setPadding(2f);
        qteCell.setBorderWidth(0.3f);
        table.addCell(qteCell);

        // Total
        PdfPCell totalCell = new PdfPCell(new Phrase(total, cellFont));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setBackgroundColor(backgroundColor);
        totalCell.setPadding(2f);
        totalCell.setBorderWidth(0.3f);
        table.addCell(totalCell);
    }

    private void ajouterLigneTotalOptimise(PdfPTable table, String label, String montant, Font font, boolean isFinal) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2f);

        PdfPCell montantCell = new PdfPCell(new Phrase(montant, font));
        montantCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        montantCell.setBorder(Rectangle.NO_BORDER);
        montantCell.setPadding(2f);

        if (isFinal) {
            labelCell.setBackgroundColor(new BaseColor(240, 240, 240));
            montantCell.setBackgroundColor(new BaseColor(240, 240, 240));
            labelCell.setBorder(Rectangle.BOX);
            montantCell.setBorder(Rectangle.BOX);
        }

        table.addCell(labelCell);
        table.addCell(montantCell);
    }

    /**
     * ✅ Rognage optimisé pour imprimantes thermiques
     */
    private byte[] rognerHauteurPDFOptimise(byte[] pdfData) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final float[] maxY = {0f};

        // Analyse du contenu
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(1, new RenderListener() {
            public void beginTextBlock() {
            }

            public void endTextBlock() {
            }

            public void renderText(TextRenderInfo textRenderInfo) {
                LineSegment baseline = textRenderInfo.getBaseline();
                if (baseline != null) {
                    float y = baseline.getStartPoint().get(1);
                    if (y > maxY[0]) {
                        maxY[0] = y;
                    }
                }
            }

            public void renderImage(ImageRenderInfo renderInfo) {
                try {
                    Matrix ctm = renderInfo.getImageCTM();
                    float imageY = ctm.get(Matrix.I31);
                    float imageHeight = ctm.get(Matrix.I22);
                    float yTop = imageY + imageHeight;
                    if (yTop > maxY[0]) {
                        maxY[0] = yTop;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        });

        // ✅ CORRECTION PRINCIPALE : Calcul optimisé de la hauteur
        PdfDictionary pageDict = reader.getPageN(1);
        PdfArray mediaBox = pageDict.getAsArray(PdfName.MEDIABOX);

        float llx = mediaBox.getAsNumber(0).floatValue();
        float lly = mediaBox.getAsNumber(1).floatValue();
        float urx = mediaBox.getAsNumber(2).floatValue();

        // ✅ Hauteur minimale pour éviter les coupures + marge de sécurité réduite
        float ury = Math.max(maxY[0] + 20f, 200f); // Marge réduite à 20 points

        // Mise à jour de la MediaBox
        PdfStamper stamper = new PdfStamper(reader, out);
        reader.getPageN(1).put(PdfName.MEDIABOX, new PdfRectangle(llx, lly, urx, ury));
        stamper.close();
        reader.close();

        return out.toByteArray();
    }

    private byte[] genererTicketPDFPPOS(Long venteid) throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // ✅ SPÉCIFIQUE POS-80 : 72mm utilisables sur 80mm de papier
        // 72mm = 204 points (72 * 72/25.4)
        // Hauteur : Exactement ce qui est nécessaire, pas plus
        Rectangle pageSize = new Rectangle(204f, 600f);

        // ✅ MARGES NULLES pour POS-80 (l'imprimante gère ses propres marges)
        Document document = new Document(pageSize, 0, 0, 0, 0);

        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        try {
            // Récupération des données
            Vente vente = barcodeService.getVenteById(venteid);
            if (vente == null) {
                throw new RuntimeException("Vente introuvable avec l'ID: " + venteid);
            }

            List<LigneVente> listeVentes = barcodeService.listeLigneVenteByVente(vente);
            if (listeVentes == null) {
                listeVentes = new ArrayList<>();
            }

            // === CONSTRUCTION DU TICKET SANS ESPACES ===
            ajouterHeaderPOS80(document);
            ajouterInfosVentePOS80(document, vente);
            ajouterTableauArticlesPOS80(document, listeVentes);
            ajouterTotauxPOS80(document, listeVentes);
            ajouterFooterPOS80(document);

        } finally {
            document.close();
        }

        // ✅ PAS DE ROGNAGE - Retour direct pour POS-80
        return out.toByteArray();
    }

    /**
     * ✅ Header compact pour POS-80
     */
    private void ajouterHeaderPOS80(Document document) throws DocumentException {
        try {
            // Logo très petit pour POS-80
            String userHome = System.getProperty("user.home");
            String cheminLogo = Paths.get(userHome, "cloud-config-gescom", "logo", "logo.png").toString();
            Image logo = Image.getInstance(cheminLogo);
            logo.scaleToFit(30, 30); // ✅ Logo très petit
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            // ✅ Espace minimal après logo
            document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 4)));

        } catch (Exception e) {
            // Titre entreprise si pas de logo
            Font logoFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            Paragraph logoText = new Paragraph("VOTRE ENTREPRISE", logoFont);
            logoText.setAlignment(Element.ALIGN_CENTER);
            document.add(logoText);
            document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 2)));
        }

        // Titre ticket
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        Paragraph title = new Paragraph("TICKET DE VENTE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Ligne de séparation
        ajouterSeparateurPOS80(document);
    }

    /**
     * ✅ Informations vente compactes POS-80
     */
    private void ajouterInfosVentePOS80(Document document, Vente vente) throws DocumentException {
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 7);

        // Date formatée
        String dateFormatee = "N/A";
        if (vente.getDateVente() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
            dateFormatee = dateFormat.format(vente.getDateVente());
        }

        // ✅ Informations en format simple (pas de tableau)
        document.add(new Paragraph("N° Ticket: " + vente.getNumeroTicket(), infoFont));
        document.add(new Paragraph("Date: " + dateFormatee, infoFont));
        document.add(new Paragraph("Caissier: " + getCurrentUser(vente), infoFont));

        ajouterSeparateurPOS80(document);
    }

    /**
     * ✅ Articles format POS-80 (pas de tableau, format texte)
     */
    private void ajouterTableauArticlesPOS80(Document document, List<LigneVente> listeVentes) throws DocumentException {
        Font articleFont = new Font(Font.FontFamily.HELVETICA, 7);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD);

        // En-tête
        document.add(new Paragraph("ARTICLES:", boldFont));

        // Liste des articles
        for (LigneVente lgv : listeVentes) {
            try {
                String libelle = (lgv.getProduit() != null && lgv.getProduit().getLibelle() != null)
                        ? lgv.getProduit().getLibelle() : "Article inconnu";

                // ✅ Troncature pour POS-80
                if (libelle.length() > 24) {
                    libelle = libelle.substring(0, 21) + "...";
                }

                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                BigDecimal totalLigne = quantite.multiply(prixUnitaire);

                // ✅ Format compact : Article sur une ligne, qté/prix sur la suivante
                document.add(new Paragraph(libelle, articleFont));

                String ligneDetails = String.format("%s x %s = %s",
                        formatQuantite(quantite),
                        formatMontantSimple(prixUnitaire),
                        formatMontantSimple(totalLigne)
                );

                Paragraph details = new Paragraph("  " + ligneDetails, articleFont);
                document.add(details);

                // Petit espace entre articles
                document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 2)));

            } catch (Exception e) {
                document.add(new Paragraph("Erreur article", articleFont));
            }
        }
    }

    /**
     * ✅ Totaux format POS-80
     */
    private void ajouterTotauxPOS80(Document document, List<LigneVente> listeVentes) throws DocumentException {
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 8);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

        ajouterSeparateurPOS80(document);

        // Calcul total
        BigDecimal totalBrut = BigDecimal.ZERO;
        for (LigneVente lgv : listeVentes) {
            try {
                BigDecimal quantite = (lgv.getQuantite() != null) ? lgv.getQuantite() : BigDecimal.ZERO;
                BigDecimal prixUnitaire = (lgv.getPrixUnitaire() != null) ? lgv.getPrixUnitaire() : BigDecimal.ZERO;
                totalBrut = totalBrut.add(quantite.multiply(prixUnitaire));
            } catch (Exception e) {
                continue;
            }
        }

        LigneVente lv = (LigneVente) listeVentes.toArray()[0];
        String modePaiement = barcodeService.getModePaiementLabel(lv.getVente());

        BigDecimal remise = BigDecimal.ZERO;
        BigDecimal netAPayer = totalBrut;
        if (lv.getVente() != null && lv.getVente().getTotalRemise().intValue() != 0) {
            remise = lv.getVente().getTotalRemise();
            netAPayer = totalBrut.subtract(remise);
        }

        // Affichage totaux
        document.add(new Paragraph("Mode paiement: " + modePaiement, totalFont));

        if (remise.compareTo(BigDecimal.ZERO) > 0) {
            document.add(new Paragraph("Remise: -" + formatMontantSimple(remise), totalFont));
        }

        // ✅ Total final bien visible
        Paragraph totalFinal = new Paragraph("TOTAL: " + formatMontantSimple(netAPayer), boldFont);
        totalFinal.setAlignment(Element.ALIGN_CENTER);
        document.add(totalFinal);
    }

    /**
     * ✅ Footer compact POS-80
     */
    private void ajouterFooterPOS80(Document document) throws DocumentException {
        ajouterSeparateurPOS80(document);

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 7);

        Paragraph merci = new Paragraph("Merci pour votre achat !", footerFont);
        merci.setAlignment(Element.ALIGN_CENTER);
        document.add(merci);

        Paragraph contact = new Paragraph("Tel: 00 00 00 00", footerFont);
        contact.setAlignment(Element.ALIGN_CENTER);
        document.add(contact);

        // ✅ Espaces finaux pour faciliter la découpe
        document.add(new Paragraph(" ", footerFont));
        document.add(new Paragraph(" ", footerFont));
        document.add(new Paragraph(" ", footerFont));
    }

// === MÉTHODES UTILITAIRES POS-80 ===
    private void ajouterSeparateurPOS80(Document document) throws DocumentException {
        Font separatorFont = new Font(Font.FontFamily.HELVETICA, 6);
        Paragraph separator = new Paragraph("--------------------------------", separatorFont);
        separator.setAlignment(Element.ALIGN_CENTER);
        document.add(separator);
    }

    private String formatMontantSimple(BigDecimal montant) {
        if (montant == null) {
            return "0";
        }

        try {
            // ✅ Format simple sans espaces pour POS-80
            DecimalFormat formatter = new DecimalFormat("#,##0");
            return formatter.format(montant) + "F";
        } catch (Exception e) {
            return montant.toString() + "F";
        }
    }

     @GetMapping("/allcaissier/{boutiqueid}")
     public ResponseEntity<List<UserDTO>> allCaissier(@PathVariable Long boutiqueid){
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<UserDTO> caissiers= barcodeService.listeCaissiers(boutiqueid);
            return ResponseEntity.ok(caissiers);
     }
     
     

}
