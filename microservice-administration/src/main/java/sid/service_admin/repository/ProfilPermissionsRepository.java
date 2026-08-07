package sid.service_admin.repository;

import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sid.service_admin.model.Menu;
import sid.service_admin.model.Permission;
import sid.service_admin.model.Profil;
import sid.service_admin.model.ProfilPermissions;

/**
 *
 * @author USER01
 */
@Repository
public interface ProfilPermissionsRepository extends JpaRepository<ProfilPermissions, Long> {

    List<ProfilPermissions> findByProfil(Profil profil);

    boolean existsByProfilAndPermission(Profil profil, Permission permission);

    void deleteByProfilAndPermission(Profil profil, Permission permission);

    // Menus visibles pour un profil : tout menu ou le profil a au moins une permission.
    @Query("SELECT DISTINCT pp.permission.menu FROM ProfilPermissions pp WHERE pp.profil.id = :profilId AND pp.permission.menu IS NOT NULL")
    List<Menu> findMenusVisiblesByProfil(@Param("profilId") Long profilId);
}
