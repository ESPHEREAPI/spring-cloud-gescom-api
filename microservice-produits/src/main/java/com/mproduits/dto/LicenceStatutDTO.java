package com.mproduits.dto;

import java.util.Date;
import java.util.Set;
import lombok.Data;

/**
 * Miroir de sid.service_admin.dto.LicenceStatutDTO (microservice-administration)
 * — reponse de l'endpoint interne /internal/licences/compagnie/{id}/statut.
 */
@Data
public class LicenceStatutDTO {
    private String statut;
    private Date dateExpiration;
    private Integer maxUtilisateurs;
    private Integer maxBoutiques;
    private Set<String> modules;
}
