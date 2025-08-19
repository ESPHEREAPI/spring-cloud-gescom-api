/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.utiles;

import com.mproduits.enums.TypePaiement;
import com.mproduits.model.LigneVente;
import com.mproduits.model.Mois;
import com.mproduits.model.Property;
import com.mproduits.model.Vente;
import com.mproduits.services.ParamModuleImpl;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarException;
import javax.print.PrintException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author USER01
 */
public class GlobalFonctions implements Serializable {

    @Autowired
    ParamModuleImpl paramModuleImpl;
    public final static String ETAB_ACTIF = "etabActif";
    //session de l'user connecte
    public final static String SESSION_USER = "sessionuser";
    //session de configuration
    public final static String SESSION_CONFIG = "sessionconfig";

    public final static String ENTREPRISE_ACTIF = "entreprise";
    public final static String MOIS = "mois";
    public static final String dossierTicket = "C:\\Ticket";

    public static final char espace = ' ';
    public static final Double nombreJourByAnnee = 365.0;
//    private String currentFolder = "/tickets";
    public final static String CARACTER_ASCII = "²";
    public final static String REMPLACE = "0";

    public static String getCodeBare(String codeBare) {
        String cde = "";
        if (codeBare.contains(CARACTER_ASCII)) {
            String codes[] = codeBare.split(CARACTER_ASCII);
            for (int i = 0; i < codes.length; i++) {

                cde += codes[i];
                if (cde.length() == 13) {
                    break;
                }
                cde += REMPLACE;
            }

        } else {
            return codeBare;
        }
        return cde;
    }

    public static String replaceASpecialCharWithAscii(String stringOr, int nbre) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < stringOr.length(); ++i) {
            char x = stringOr.charAt(i);
            int cast = (int) x;
            int codePoint = String.valueOf(x).codePointAt(0);
            char again = (char) codePoint;
            if (codePoint != nbre) {
                str.append(x);
            }
        }
        return str.toString();
    }

    public static double getNumberNet(Double d) {
        System.out.println("valeur entree :" + d);
        BigDecimal pi = new BigDecimal(d);

        // Arrondir le nombre; retenir deux chiffres après la virgule
        pi = pi.setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println("valeur arroundir 2chiffres :" + pi);
        //recuperation de l entier
        int i = pi.intValue();
        System.out.println("valeur entier :" + i);
        //transformation du double en string
        String valeur = pi.toString();
        System.out.println("valeur Stingr :" + valeur);
        System.out.println("valeur different :" + (d - d.intValue()));
        //convertir  le int en string
        String j = "" + i;
        // la taille du string convertir
        int l = j.length();
        // recuperation du caractetere apres la virgule
        char c = valeur.charAt(l + 1);
        System.out.println("valeur apres lavirgule:" + c);
        String sc = "" + c;
        Long stringSc = Long.valueOf(sc);
        //convertir le strin le caractere c en integer
        int chiffre = stringSc.intValue();

        double nombre = 0.0;
        System.out.println("valeur exact :" + chiffre);
        if (chiffre < 5) {

            nombre = Math.floor(d.doubleValue());

        } else if (chiffre > 5 || chiffre == 5) {
            nombre = Math.ceil(d.doubleValue());
        }

        System.out.println("valeur exact :" + nombre);
        return nombre;
    }

    public static int getNumberNetForInt(Double d) {
        System.out.println("valeur entree :" + d);
        BigDecimal pi = new BigDecimal(d);

        // Arrondir le nombre; retenir deux chiffres après la virgule
        pi = pi.setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println("valeur arroundir 2chiffres :" + pi);
        //recuperation de l entier
        int i = pi.intValue();
        System.out.println("valeur entier :" + i);
        //transformation du double en string
        String valeur = pi.toString();
        System.out.println("valeur Stingr :" + valeur);
        System.out.println("valeur different :" + (d - d.intValue()));
        //convertir  le int en string
        String j = "" + i;
        // la taille du string convertir
        int l = j.length();
        // recuperation du caractetere apres la virgule
        char c = valeur.charAt(l + 1);
        System.out.println("valeur apres lavirgule:" + c);
        String sc = "" + c;
        Long stringSc = Long.valueOf(sc);
        //convertir le strin le caractere c en integer
        int chiffre = stringSc.intValue();

        Double nombre = 0.0;
        System.out.println("valeur exact :" + chiffre);
        if (chiffre < 5) {

            nombre = Math.floor(d.doubleValue());

        } else if (chiffre > 5 || chiffre == 5) {
            nombre = Math.ceil(d.doubleValue());
        }

        System.out.println("valeur exact :" + nombre);
        return nombre.intValue();
    }

    public static String formatDateForNameTicket(Date d) {
        if (d != null) {
            int jour = IdleDate.getDayMonth(d);
            int mois = IdleDate.getMonth(d);
            int annee = IdleDate.getYear(d);
            String jr = "" + jour;
            String m = "" + mois;
            if (jr.length() == 1) {
                jr = "0" + jour;

            }
            if (m.length() == 1) {
                m = "0" + mois;
            }
            return "" + jr + "_" + m + "_" + annee;
        }
        return null;
    }

    public static String printTicket(Collection<?> data, Map<String, Object> parameters, Property p, Boolean enregistrer, Boolean remboursement_avec_bon_achat, TypePaiement tp, String mois) throws IOException, JarException, PrintException {

//        FacesContext ctx = FacesContext.getCurrentInstance();
//        ExternalContext extContext = ctx.getExternalContext();
        Boolean impressionDirect = p.getImpressionDirect();
        String cheminFile = p.getCheminTicket();
        Vente vente = new Vente();
        Integer nbreArticle = 0;
        LigneVente lv = null;
        try {
            lv = (LigneVente) data.toArray()[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }

        java.util.GregorianCalendar calendar = new GregorianCalendar();
        if (cheminFile == null || "".equals(cheminFile)) {
            // dossier par defaut
            cheminFile = "C:/Ticket";
//            String sous dossierPrincipal="C:/Ticket/"
//            cheminFile = "C:/ticket.txt";
        }
        File ticket = new File(cheminFile.trim());
//        Document document = null;
        if (ticket.exists() == false) {
            File dossier = new File(cheminFile.trim());
            dossier.mkdir();
//            if (dossier.exists() == true) {
//                // dossier existe 
//                ticket.createNewFile();
//            }
        
        ////            ticket.createNewFile();
        }
        cheminFile += "\\" + lv.getVente().getEntreprise().getAnnee();
        File annee = new File(cheminFile.trim());
//        Document document = null;
        if (annee.exists() == false) {
            File dossier = new File(cheminFile.trim());
            dossier.mkdir();
//            if (dossier.exists() == true) {
//                // dossier existe 
//                ticket.createNewFile();
//            }
        
        ////            ticket.createNewFile();
        }
        cheminFile += "\\" + mois;
        File pathFile = new File(cheminFile.trim());
//        Document document = null;
        if (pathFile.exists() == false) {
            File dossier = new File(cheminFile.trim());
            dossier.mkdir();
//            if (dossier.exists() == true) {
//                // dossier existe 
//                ticket.createNewFile();
//            }
        
        ////            ticket.createNewFile();
        }
        String caissier = vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom() == null ? "" : vente.getVendeur().getPrenom();
        if (caissier.contains(" ") == Boolean.TRUE) {
            caissier = caissier.replace(" ", "_");
        }

        cheminFile += "\\" + GlobalFonctions.formatDateForNameTicket(calendar.getTime()) + "_" + caissier;
        System.out.println("chemin : " + cheminFile);
        File file = new File(cheminFile.trim());
        if (file.exists() == Boolean.FALSE) {

            File dossier = new File(cheminFile.trim());
            dossier.mkdir();
        }
        cheminFile+= "\\" + vente.getNumeroTicket() + ".txt";
       
        System.out.println("chemin2 : " + cheminFile);
        File file_texte = new File(cheminFile.trim());
        if (file_texte.exists() == Boolean.FALSE) {
            file_texte.createNewFile();
        }
//        PrintWriter writer = new PrintWriter(ticket);
//        writer.print("");
        FileOutputStream fos = new FileOutputStream(file_texte);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        String societe = "", tt = "", client = "";
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String string = entry.getKey();
            if ("societe".equals(string)) {
                societe = entry.getValue().toString();
            }
            if (Objects.equals(p.getClientTicket(), Boolean.TRUE)) {
                if ("client".equals(string)) {
                    try {
                        client = entry.getValue().toString();
                    } catch (NullPointerException n) {
                        client = "";
                    }

                }
            }

            if ("tt".equals(string)) {
                tt = entry.getValue().toString();
            }

        }
        bw.write("");
//        Ticket t = (Ticket) data.toArray()[0];
        bw.write(societe + "  " + vente.getEntreprise().getEmployeur().getSociete());
        bw.newLine();
        bw.write(vente.getEntreprise().getEmployeur().getPersonne().getQuartier() + " " + vente.getEntreprise().getEmployeur().getPersonne().getVille());
        bw.newLine();
        bw.write("BP:" + vente.getEntreprise().getEmployeur().getPersonne().getBp());
        bw.newLine();
        bw.write(vente.getEntreprise().getEmployeur().getPersonne().getTel());
        bw.newLine();
//        java.util.GregorianCalendar calendar = new GregorianCalendar();
//        bw.write(calendar.getTime().toString());
//        bw.newLine();
        if (client != null && !"".equals(client)) {
            bw.write("Client :" + client);
            bw.newLine();
        }

        bw.write("Fait par : " + vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom() == null ? "" : vente.getVendeur().getPrenom());
//        java.util.GregorianCalendar calendar = new GregorianCalendar();
        bw.write(" " + "");
        bw.write(GlobalFonctions.formatDate(calendar.getTime()));
        bw.newLine();
        vente.setDateTicketPrint(new Date());
        bw.write("Heure : " + vente.getDateTicketPrint());
        bw.newLine();
        bw.write("/NumTicket:" + vente.getNumeroTicket());
        bw.newLine();

        bw.write("---------------------------------");
        bw.newLine();
        LigneVente l;
        for (Object object : data) {
            l = (LigneVente) object;
            nbreArticle = nbreArticle + l.getQuantite().intValue();
            if (l.getProduit().getLibelle().length() > 30) {
                bw.write(l.getProduit().getLibelle().substring(0, 29));
            } else {
                bw.write(l.getProduit().getLibelle());
            }

            bw.newLine();

            bw.write(l.getQuantite().toString() + "x" + l.getPrixUnitaire().toString() + "=" + l.getTotalLigne().toString());
            bw.newLine();
        }

        bw.write("---------------------------------");
        bw.newLine();
        bw.write("TOTAL             : " + tt);
        bw.newLine();
        bw.write("Nbre d' articles  : " + nbreArticle.toString());
        bw.newLine();
        bw.write("Paiement Par      : " + valueObject(tp == null ? TypePaiement.ESPECES : tp));
        if (vente.getTotalRemise().intValue() == 0) {
            bw.newLine();

            if (Objects.equals(remboursement_avec_bon_achat, Boolean.TRUE)) {
                bw.write("Rendu         : Bon d'Achat");
            } else {
                bw.write("Total FCFA        : " + vente.getTotalBrut());
                  bw.newLine();
                   bw.write("Montant Recu FCFA        : " + formatNumberGeneral(vente.getTotalrecu().longValue()));
                  bw.newLine();
                  BigDecimal rendu=vente.getTotalBrut().subtract(vente.getTotalrecu());
                bw.write("Rendu FCFA        : " + formatNumberGeneral(rendu.longValue()));
            }

        } else {
            bw.newLine();
            bw.write("Remise              : " + formatNumberGeneral(vente.getTotalRemise().longValue()));
            bw.newLine();
//            bw.write("Rendu FCFA        : " + t.getReste());
            if (Objects.equals(remboursement_avec_bon_achat, Boolean.TRUE)) {
                bw.write("Rendu           : Bon d'Achat");
            } else {
                BigDecimal rendu=vente.getTotalNet().subtract(vente.getTotalrecu());
                bw.write("Rendu FCFA      : " + formatNumberGeneral(rendu.longValue()));
            }
            bw.newLine();
            bw.write("Net à payer FCFA    : " + vente.getTotalNet());

        }

        bw.newLine();
        bw.write("Les Marchandises Vendues ne sont");
        bw.newLine();
        bw.write("ni Reprises ni Echangées");
        bw.newLine();
        bw.write("Merci de votre visite à Bientot !!!");
        bw.newLine();
        bw.write("__");
//        bw.newLine();

        bw.close();
//        if (Objects.equals(p.getImpressionDirect(), Boolean.FALSE) || p.getImpressionDirect() == null) {
//            downloadFile(file_texte);
//        }
        return cheminFile;
    }
  

    public static String formatNumberGeneral(long number) {

        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols s = format.getDecimalFormatSymbols();

        s.setGroupingSeparator(GlobalFonctions.espace);
        format.setDecimalFormatSymbols(s);

        return format.format(number);

    }
     public static String valueObject(Object t) {
        TypePaiement tp =TypePaiement.valueOf(t.toString());
     
        if (tp!=null) {
            return tp.name();
        }
        return "";
    }

  public static String formatDate(Date d) {
        if (d != null) {
            int jour = IdleDate.getDayMonth(d);
            int mois = IdleDate.getMonth(d);
            int annee = IdleDate.getYear(d);
            String jr = "" + jour;
            String m = "" + mois;
            if (jr.length() == 1) {
                jr = "0" + jour;

            }
            if (m.length() == 1) {
                m = "0" + mois;
            }
            return "" + jr + "/" + m + "/" + annee;
        }
        return null;
    }
 
}
