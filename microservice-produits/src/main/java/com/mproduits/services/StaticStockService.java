package com.mproduits.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mproduits.dto.LibelleStockDTO;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PrixArticles;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Consultation de l'etat du stock par boutique ("libelle de stock" = boutique
 * de la compagnie courante). Utilise par l'ecran Static Stock.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaticStockService {

    private final BoutiqueRepositories boutiqueRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    public List<LibelleStockDTO> getLibelles() {
        return boutiqueRepositories.findByCompagnie_Id(compagnieCourante()).stream()
                .map(b -> new LibelleStockDTO(b.getId(), b.getNom(), b.getCode(), b.getQuartier(), Boolean.TRUE))
                .toList();
    }

    public Page<PrixArticles> getStockByPointVente(Long libelleStockId, int page, int size) {
        Long compagnieId = compagnieCourante();
        Boutique boutique = boutiqueRepositories.findByIdAndCompagnie_Id(libelleStockId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvee avec l'ID: " + libelleStockId));
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        Pageable pageable = PageRequest.of(page, size);
        return prixArticlesRepositories.findAllByEntrepriseProduitActif(entreprise, Boolean.TRUE, pageable, boutique.getId());
    }

    public byte[] exportPdf(Long libelleStockId) {
        Long compagnieId = compagnieCourante();
        Boutique boutique = boutiqueRepositories.findByIdAndCompagnie_Id(libelleStockId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvee avec l'ID: " + libelleStockId));
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        List<PrixArticles> lignes = prixArticlesRepositories
                .findAllByEntrepriseProduitActif(entreprise, Boolean.TRUE, Pageable.unpaged(), boutique.getId())
                .getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Etat du stock - " + boutique.getNom(),
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 4, 2, 2, 2});
            for (String header : new String[]{"Reference", "Libelle", "Stock final", "Prix vente net", "Prix vente TTC"}) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (PrixArticles pa : lignes) {
                var produit = pa.getPointVente() != null ? pa.getPointVente().getProduit() : null;
                table.addCell(produit != null ? produit.getReference() : "-");
                table.addCell(produit != null ? produit.getLibelle() : "-");
                table.addCell(pa.getPointVente() != null && pa.getPointVente().getStockFinalTheorie() != null
                        ? pa.getPointVente().getStockFinalTheorie().toString() : "-");
                table.addCell(pa.getPrixVenteNet() != null ? pa.getPrixVenteNet().toString() : "-");
                table.addCell(pa.getPrixVenteTTC() != null ? pa.getPrixVenteTTC().toString() : "-");
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du PDF: " + e.getMessage(), e);
        }
    }
}
