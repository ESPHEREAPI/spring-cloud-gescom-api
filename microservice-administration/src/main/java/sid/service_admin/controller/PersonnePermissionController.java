package sid.service_admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.PersonneMenuActionsDTO;
import sid.service_admin.dto.PersonnePermissionExceptionRequest;
import sid.service_admin.enums.ExceptionType;
import sid.service_admin.exceptions.BadRequestException;
import sid.service_admin.service.PersonnePermissionService;

/**
 * Droits effectifs d'un utilisateur precis (Profil + exceptions), voir
 * PersonnePermissionService. Meme reserve que la matrice Profil : un
 * COMPANY_ADMIN gere ses propres utilisateurs comme un SUPER_ADMIN/
 * SYSTEM_ADMIN.
 */
@RestController
@RequestMapping("/personne")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
public class PersonnePermissionController {

    private final PersonnePermissionService service;

    @org.springframework.web.bind.annotation.GetMapping("/{id}/permissions-effectives")
    public ResponseEntity<List<PersonneMenuActionsDTO>> getPermissionsEffectives(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPermissionsEffectives(id));
    }

    @PostMapping("/{id}/permission-exception")
    public ResponseEntity<Void> definirException(@PathVariable Long id,
            @RequestBody PersonnePermissionExceptionRequest request, Authentication authentication) {
        ExceptionType type = parseType(request.getType());
        String createdBy = authentication != null ? authentication.getName() : "systeme";
        service.definirException(id, request.getMenuId(), request.getAction(), type, createdBy);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permission-exception/{exceptionId}")
    public ResponseEntity<Void> supprimerException(@PathVariable Long id, @PathVariable Long exceptionId) {
        service.supprimerException(id, exceptionId);
        return ResponseEntity.noContent().build();
    }

    private ExceptionType parseType(String type) {
        if (type == null) {
            throw new BadRequestException("Le type d'exception (GRANT/REVOKE) est obligatoire");
        }
        try {
            return ExceptionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Type d'exception invalide : " + type);
        }
    }
}
