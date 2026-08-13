package com.mproduits.repositories;

import com.mproduits.model.ProfilPermissions;
import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfilPermissionsRepositories extends JpaRepository<ProfilPermissions, Long> {

    // Couples (code menu, code action) accordes a un profil - evite de
    // charger les entites completes, seul le calcul des autorites
    // PERM_<MENU>_<ACTION> en a besoin (voir EffectivePermissionService).
    @Query("SELECT pp.permission.menu.code, pp.permission.action.code FROM ProfilPermissions pp "
            + "WHERE pp.profil.id = :profilId AND pp.permission.menu IS NOT NULL AND pp.permission.action IS NOT NULL")
    List<Object[]> findMenuActionCodesByProfilId(@Param("profilId") Long profilId);
}
