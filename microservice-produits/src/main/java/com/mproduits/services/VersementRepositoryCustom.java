/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.VersementStatistiques;
import java.util.Date;

/**
 *
 * @author USER01
 */
public interface VersementRepositoryCustom {
    VersementStatistiques calculerStatistiques(Date dateDebut, Date dateFin);
}
