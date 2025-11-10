/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.repositories.VersementClientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author USER01
 */

/**
 * Service de génération de numéros uniques pour :
 * - Factures
 * - Versements
 * - Reçus de paiement
 * - Autres documents
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NumeroGeneratorService {
    
 
    private final FactureRepository factureRepository;
    private final VersementClientRepository versementRepository;
    
    // Compteurs atomiques pour thread-safety
    private final AtomicLong compteurFacture = new AtomicLong(0);
    private final AtomicLong compteurVersement = new AtomicLong(0);
    private final AtomicLong compteurRecu = new AtomicLong(0);
    
    // Préfixes configurables
    private static final String PREFIX_FACTURE = "FAC";
    private static final String PREFIX_VERSEMENT = "VERS";
    private static final String PREFIX_RECU = "REC";
    private static final String PREFIX_DEVIS = "DEV";
    private static final String PREFIX_AVOIR = "AVO";
    private static final String PREFIX_COMMANDE = "CMD";

    // ========================================================================
    // GÉNÉRATION NUMÉRO DE FACTURE
    // ========================================================================
    
    /**
     * Génère un numéro de facture unique au format: FAC-YYYY-XXXXXX
     * Exemple: FAC-2025-000001
     */
    @Transactional(readOnly = true)
    public String genererNumeroFacture() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = factureRepository.count() + 1;
        
        // Synchronisation pour éviter les doublons
        synchronized (compteurFacture) {
            if (compteurFacture.get() == 0) {
                compteurFacture.set(count);
            } else {
                compteurFacture.incrementAndGet();
            }
            count = compteurFacture.get();
        }
        
        String numero = String.format("%s-%d-%06d", PREFIX_FACTURE, year, count);
        log.debug("Numéro de facture généré: {}", numero);
        return numero;
    }

    /**
     * Génère un numéro de facture avec préfixe personnalisé
     */
    public String genererNumeroFacturePerso(String prefixePersonnalise) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = factureRepository.count() + 1;
        return String.format("%s-%d-%06d", prefixePersonnalise, year, count);
    }

    /**
     * Génère un numéro de facture pour un point de vente spécifique
     * Format: FAC-PV01-YYYY-XXXXXX
     */
    public String genererNumeroFacture(String codePointVente) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = factureRepository.count() + 1;
        return String.format("%s-%s-%d-%06d", PREFIX_FACTURE, codePointVente, year, count);
    }

    // ========================================================================
    // GÉNÉRATION NUMÉRO DE VERSEMENT
    // ========================================================================
    
    /**
     * Génère un numéro de versement unique au format: VERS-YYYY-XXXXXX
     * Exemple: VERS-2025-000001
     */
    @Transactional(readOnly = true)
    public String genererNumeroVersement() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = versementRepository.count() + 1;
        
        synchronized (compteurVersement) {
            if (compteurVersement.get() == 0) {
                compteurVersement.set(count);
            } else {
                compteurVersement.incrementAndGet();
            }
            count = compteurVersement.get();
        }
        
        String numero = String.format("%s-%d-%06d", PREFIX_VERSEMENT, year, count);
        log.debug("Numéro de versement généré: {}", numero);
        return numero;
    }

    /**
     * Génère une référence de paiement unique basée sur la date et l'heure
     * Format: VERS-YYYYMMDD-HHMMSS-XXX
     */
    public String genererReferencePaiement() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        
        String dateStr = today.format(dateFormatter);
        String timeStr = java.time.LocalTime.now().format(timeFormatter);
        
        long randomSuffix = (long) (Math.random() * 1000);
        
        String reference = String.format("%s-%s-%s-%03d", 
            PREFIX_VERSEMENT, dateStr, timeStr, randomSuffix);
        
        log.debug("Référence de paiement générée: {}", reference);
        return reference;
    }

    // ========================================================================
    // GÉNÉRATION NUMÉRO DE REÇU
    // ========================================================================
    
    /**
     * Génère un numéro de reçu unique au format: REC-YYYY-XXXXXX
     * Exemple: REC-2025-000001
     */
    public String genererNumeroRecu() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        synchronized (compteurRecu) {
            compteurRecu.incrementAndGet();
            long count = compteurRecu.get();
            
            String numero = String.format("%s-%d-%06d", PREFIX_RECU, year, count);
            log.debug("Numéro de reçu généré: {}", numero);
            return numero;
        }
    }

    /**
     * Génère un numéro de reçu basé sur l'ID du versement
     * Format: REC-VERS-XXXXXX
     */
    public String genererNumeroRecuFromVersement(Long versementId) {
        String numero = String.format("%s-%s-%06d", PREFIX_RECU, PREFIX_VERSEMENT, versementId);
        log.debug("Numéro de reçu généré pour versement {}: {}", versementId, numero);
        return numero;
    }

    /**
     * Génère un numéro de reçu avec date
     * Format: REC-YYYYMMDD-XXXXXX
     */
    public String genererNumeroRecuAvecDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = today.format(formatter);
        
        synchronized (compteurRecu) {
            compteurRecu.incrementAndGet();
            long count = compteurRecu.get();
            
            String numero = String.format("%s-%s-%06d", PREFIX_RECU, dateStr, count);
            log.debug("Numéro de reçu avec date généré: {}", numero);
            return numero;
        }
    }

    // ========================================================================
    // GÉNÉRATION AUTRES NUMÉROS
    // ========================================================================
    
    /**
     * Génère un numéro de devis
     * Format: DEV-YYYY-XXXXXX
     */
    public String genererNumeroDevis() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = System.currentTimeMillis() % 1000000;
        return String.format("%s-%d-%06d", PREFIX_DEVIS, year, count);
    }

    /**
     * Génère un numéro d'avoir
     * Format: AVO-YYYY-XXXXXX
     */
    public String genererNumeroAvoir() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = System.currentTimeMillis() % 1000000;
        return String.format("%s-%d-%06d", PREFIX_AVOIR, year, count);
    }

    /**
     * Génère un numéro de commande
     * Format: CMD-YYYY-XXXXXX
     */
    public String genererNumeroCommande() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long count = System.currentTimeMillis() % 1000000;
        return String.format("%s-%d-%06d", PREFIX_COMMANDE, year, count);
    }

    /**
     * Génère un numéro personnalisé avec préfixe et suffixe
     */
    public String genererNumeroPersonnalise(String prefixe, int longueur) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        long randomNumber = (long) (Math.random() * Math.pow(10, longueur));
        String format = "%s-%d-%0" + longueur + "d";
        return String.format(format, prefixe, year, randomNumber);
    }

    // ========================================================================
    // VALIDATION DE NUMÉROS
    // ========================================================================
    
    /**
     * Valide le format d'un numéro de facture
     */
    public boolean isNumeroFactureValide(String numero) {
        if (numero == null || numero.isEmpty()) {
            return false;
        }
        // Format attendu: FAC-YYYY-XXXXXX
        String regex = "^" + PREFIX_FACTURE + "-\\d{4}-\\d{6}$";
        return numero.matches(regex);
    }

    /**
     * Valide le format d'un numéro de versement
     */
    public boolean isNumeroVersementValide(String numero) {
        if (numero == null || numero.isEmpty()) {
            return false;
        }
        String regex = "^" + PREFIX_VERSEMENT + "-\\d{4}-\\d{6}$";
        return numero.matches(regex);
    }

    /**
     * Valide le format d'un numéro de reçu
     */
    public boolean isNumeroRecuValide(String numero) {
        if (numero == null || numero.isEmpty()) {
            return false;
        }
        String regex = "^" + PREFIX_RECU + "-(\\d{4}-\\d{6}|" + PREFIX_VERSEMENT + "-\\d{6}|\\d{8}-\\d{6})$";
        return numero.matches(regex);
    }

    // ========================================================================
    // EXTRACTION D'INFORMATIONS DES NUMÉROS
    // ========================================================================
    
    /**
     * Extrait l'année d'un numéro de facture
     */
    public Integer extraireAnneeFacture(String numeroFacture) {
        if (!isNumeroFactureValide(numeroFacture)) {
            return null;
        }
        String[] parts = numeroFacture.split("-");
        return Integer.parseInt(parts[1]);
    }

    /**
     * Extrait le numéro séquentiel d'une facture
     */
    public Long extraireSequenceFacture(String numeroFacture) {
        if (!isNumeroFactureValide(numeroFacture)) {
            return null;
        }
        String[] parts = numeroFacture.split("-");
        return Long.parseLong(parts[2]);
    }

    // ========================================================================
    // RÉINITIALISATION DES COMPTEURS (À UTILISER AVEC PRÉCAUTION)
    // ========================================================================
    
    /**
     * Réinitialise le compteur de factures (admin uniquement)
     */
    public void reinitialiserCompteurFacture() {
        compteurFacture.set(0);
        log.warn("Compteur de factures réinitialisé");
    }

    /**
     * Réinitialise le compteur de versements (admin uniquement)
     */
    public void reinitialiserCompteurVersement() {
        compteurVersement.set(0);
        log.warn("Compteur de versements réinitialisé");
    }

    /**
     * Réinitialise le compteur de reçus (admin uniquement)
     */
    public void reinitialiserCompteurRecu() {
        compteurRecu.set(0);
        log.warn("Compteur de reçus réinitialisé");
    }

    /**
     * Réinitialise tous les compteurs (admin uniquement - à utiliser en début d'année)
     */
    public void reinitialiserTousLesCompteurs() {
        reinitialiserCompteurFacture();
        reinitialiserCompteurVersement();
        reinitialiserCompteurRecu();
        log.warn("Tous les compteurs ont été réinitialisés");
    }   
}
