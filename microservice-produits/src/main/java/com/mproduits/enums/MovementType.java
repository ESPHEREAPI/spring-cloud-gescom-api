/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
  public enum MovementType {
        ENTREE("Entrée stock"),
        SORTIE_VENTE("Sortie vente"),
        RETOUR("Retour client"),
        INVENTAIRE("Ajustement inventaire"),
        AJUSTEMENT("Ajustement"),
        SORTIE_FACTURE("Sortie Facture"),
        ENTREE_ANNULATION("Entree Annulation"),
        INITIALISATION("Restauration de stock");
        
        private final String label;
        
        MovementType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
