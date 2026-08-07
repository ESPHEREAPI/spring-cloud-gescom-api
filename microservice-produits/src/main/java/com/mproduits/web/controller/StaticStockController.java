package com.mproduits.web.controller;

import com.mproduits.dto.ApiResponse;
import com.mproduits.dto.LibelleStockDTO;
import com.mproduits.model.PrixArticles;
import com.mproduits.services.StaticStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Etat du stock par boutique ("libelle de stock" = boutique de la compagnie
 * courante). Backend de l'ecran Static Stock.
 */
@RestController
@RequestMapping("/microservice-produits")
@RequiredArgsConstructor
public class StaticStockController {

    private final StaticStockService staticStockService;

    @GetMapping("/libelles")
    public ResponseEntity<ApiResponse<List<LibelleStockDTO>>> getLibelles() {
        return ResponseEntity.ok(ApiResponse.success(staticStockService.getLibelles(), "Libelles recuperes"));
    }

    @GetMapping("/by-point-vente")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByPointVente(
            @RequestParam Long libelleStockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String libelle,
            @RequestParam(required = false) String globalFilter) {

        Page<PrixArticles> resultat = staticStockService.getStockByPointVente(libelleStockId, page, size);
        Map<String, Object> body = Map.of(
                "data", resultat.getContent(),
                "total", resultat.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(body, "Stock recupere"));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam Long libelleStockId) {
        byte[] pdf = staticStockService.exportPdf(libelleStockId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "static-stock.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
