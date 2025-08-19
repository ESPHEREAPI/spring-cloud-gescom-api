
package com.mproduits.exceptions;

/**
 *
 * @author Momo Ludovic
 */
public enum Error {
  
    DATAEMPTY("DATAEMPTY"),
   FATAL_ERROR("FATAL_ERROR"),
   DUPLICATE_ERROR_INSERT("DUPLICATE_ERROR_INSERT"),
   DUPLICATE_ERROR_UPDATE("DUPLICATE_ERROR_UPDATE"),
   INSERT_ERROR("INSERT_ERROR"),
   UPDATE_ERROR("UPDATE_ERROR"),
   DELETE_ERROR("DELETE_ERROR"),
   NORESULTS_FOUND("NORESULTS_FOUND"),
   ENTITY_RECORDS_EXIST("ENTITY_RECORDS_EXIST"),
   OPERATION_FAILED("OPERATION_FAILED"),
   QUANTITE_INSUFISSANT("QUANTITE_INSUFISSANT"),
   ERROR_ARTICLE("ARTICLE_ISNOT_EXISTE"),
   
   
   
   
   
   //pour les montants 
   CONTROL_MONTANT("CONTROL_MONTANT"),
  
   //pour les dates
   CONTROL_DATE("CONTROL_DATE"),
   
    CONTROL_DATE_CREATION("DATE CREATION ENTREPRISE "),
   //for PaymentException
   INFO_PARAMFRAIS("INFO_PARAMFRAIS"),
   CONTROL_NET_REDUIT("CONTROL_NET_REDUIT"),
   //pour cassociaux
   CONTROL_MONTANT_CAS("CONTROL_MONTANT_CAS"),
   CONTROL_PARAMETRAGE("CONTROL_PARAMETRAGE"),
   CONTROL_MONTANT_REDUIT("CONTROL_MONTANT_REDUIT"),
   CONTROL_TRANCHE("CONTROL_TRANCHE"),
   CONTROL_FULL_MONTANT("CONTROL_FULL_MONTANT");
   
   //pour etude
   
   

  private final String code;

  private Error( String code) {
   
    this.code = code;
  }

    public String getCode() {
        return code;
    }

  
  @Override
  public String toString() {
    return code ;
  }
}