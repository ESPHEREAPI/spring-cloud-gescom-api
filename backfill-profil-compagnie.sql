-- Backfill : rattachement des profils orphelins (compagnie_id NULL) a la
-- compagnie 1 ("lipadi"), seule compagnie utilisee pendant les tests avant
-- l'ajout du lien Profil -> Compagnie, puis nettoyage des doublons de
-- profils par defaut re-semes automatiquement pour cette compagnie au
-- redemarrage de microservice-administration.
--
-- A EXECUTER SOI-MEME via phpMyAdmin (base easycom_db), apres relecture.
-- Rien n'est execute automatiquement.

-- 0) Verification avant modification : lister les profils orphelins actuels
--    (attendu : ADMIN/CAISSIER/COMMERCIAL/COMPTABLE/USER + CAISSIER_CODEBARRE).
SELECT id, code, description, compagnie_id FROM profil WHERE compagnie_id IS NULL;

-- 1) Rattache ces profils orphelins a la compagnie 1.
UPDATE profil SET compagnie_id = 1 WHERE compagnie_id IS NULL;

-- 2) Au redemarrage, le rattrapage automatique (SuperAdminBootstrap) a semé
--    un 2e jeu ADMIN/CAISSIER/COMMERCIAL/COMPTABLE/USER pour la compagnie 1
--    (elle n'avait alors, vue par le nouveau code, aucun profil rattaché).
--    On supprime ces doublons vides (aucune permission accordee, aucun
--    utilisateur assigne) en gardant la version la plus ancienne (id le plus
--    petit) pour chaque code - celle qui peut deja porter des permissions
--    configurees pendant les tests.
--    Verification avant suppression : doit ne renvoyer QUE des profils sans
--    permission ni utilisateur (sinon, ne pas executer l'etape 3 telle quelle).
SELECT p2.id, p2.code, p2.compagnie_id
FROM profil p1
JOIN profil p2 ON p1.code = p2.code AND p1.compagnie_id = p2.compagnie_id AND p2.id > p1.id
WHERE p1.compagnie_id = 1
  AND NOT EXISTS (SELECT 1 FROM profil_permissions pp WHERE pp.profil_id = p2.id)
  AND NOT EXISTS (SELECT 1 FROM personne pe WHERE pe.profilid = p2.id);

-- 3) Suppression effective des doublons identifies ci-dessus.
DELETE p2 FROM profil p1
JOIN profil p2 ON p1.code = p2.code AND p1.compagnie_id = p2.compagnie_id AND p2.id > p1.id
WHERE p1.compagnie_id = 1
  AND NOT EXISTS (SELECT 1 FROM profil_permissions pp WHERE pp.profil_id = p2.id)
  AND NOT EXISTS (SELECT 1 FROM personne pe WHERE pe.profilid = p2.id);

-- 4) Verification finale : chaque code ne doit plus apparaitre qu'une seule
--    fois pour la compagnie 1, plus aucun profil avec compagnie_id NULL.
SELECT id, code, description, compagnie_id FROM profil ORDER BY compagnie_id, code;
