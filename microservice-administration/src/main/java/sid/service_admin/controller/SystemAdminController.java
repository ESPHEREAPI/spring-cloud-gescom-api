package sid.service_admin.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.AccountCreationResult;
import sid.service_admin.dto.ActivationDTO;
import sid.service_admin.dto.CreateSystemAdminDTO;
import sid.service_admin.dto.ResetPasswordResultDTO;
import sid.service_admin.dto.UserDTO;
import sid.service_admin.security.RoleNames;
import sid.service_admin.service.AdminAccountService;

/**
 * Creation des administrateurs systeme - reserve au super-administrateur.
 */
@RestController
@RequestMapping("/admin/system-admins")
public class SystemAdminController {

    private final AdminAccountService adminAccountService;

    public SystemAdminController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AccountCreationResult> create(@RequestBody CreateSystemAdminDTO dto, Authentication authentication) {
        return ResponseEntity.ok(adminAccountService.createSystemAdmin(dto, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserDTO>> listAll() {
        return ResponseEntity.ok(adminAccountService.listByRole(RoleNames.SYSTEM_ADMIN));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResetPasswordResultDTO> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(adminAccountService.resetSystemAdminPassword(id));
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> activer(@PathVariable Long id, @RequestBody ActivationDTO dto) {
        return ResponseEntity.ok(adminAccountService.setSystemAdminActive(id, true, dto.getMotif()));
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> desactiver(@PathVariable Long id, @RequestBody ActivationDTO dto) {
        return ResponseEntity.ok(adminAccountService.setSystemAdminActive(id, false, dto.getMotif()));
    }
}
