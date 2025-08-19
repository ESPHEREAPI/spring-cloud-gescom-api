/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author USER01
 */
public class JwtExpiration {
     // Option 1: Utiliser une chaîne secrète
    //private String jwtSecret = "P4ssw0rd#2024!S3cur1ty@K3y$ForJWT&T0k3n*Authent1cat10n";
    
    // Option 2: Générer une clé cryptographique (recommandé)
    private static Key jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static Date expiresAt(long jwtExpirationMs) {
        //long jwtExpirationMs = 86400000; // 24 heures en millisecondes

        // Obtenir l'instant actuel
        Instant now = Instant.now();

        // Calculer l'instant d'expiration
        Instant expirationInstant = now.plusMillis(jwtExpirationMs);

        // Convertir en LocalDateTime pour un formatage plus facile
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(
                expirationInstant, ZoneId.systemDefault());

        // Formater la date de façon lisible
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = expirationDateTime.format(formatter);

        System.out.println("Date d'expiration du token : " + formattedDate);
        
        // convertie en date 
        // Chaîne de date à convertir
        String dateString = "2025-05-07 14:30:00";
        
        // Définir le format de la date
        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            // Convertir la chaîne en Date
            Date date = formatterDate.parse(formattedDate);
            System.out.println("Date convertie : " + date);
            return date;
            
          
        } catch (ParseException e) {
            System.out.println("Erreur lors de la conversion de la date : " + e.getMessage());
        }
        return new Date();
    }
    
     public static String generateJwtToken(String username, Date expiryDate ) {
         Date now = new Date();
        //Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        // Utiliser la chaîne secrète
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS512, jwtSecret)
//                .compact();
                
        // OU utiliser la clé cryptographique (méthode recommandée)
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtKey)
                .compact();
        
    }
     
     
     }


