-- Nettoyage : la table `employeur` n'est plus utilisee - Entreprise est
-- desormais rattachee directement a `compagnie` (voir refactor Entreprise =
-- Annee x Compagnie, session du 2026-08-04). Toutes les donnees fiscales
-- qu'elle portait (societe, NUI, numero contribuable, logo) sont deja
-- couvertes par la table `compagnie`.
--
-- A EXECUTER SOI-MEME via phpMyAdmin (base easycom_db), apres relecture.
-- Rien n'est execute automatiquement.

-- 0) Verification avant suppression : la table doit etre vide (confirme le
--    2026-08-04 avant le refactor - aucun Employeur n'avait jamais ete cree).
SELECT COUNT(*) AS nb_employeurs FROM employeur;

-- 1) Si la verification ci-dessus renvoie bien 0, on peut supprimer la table.
--    (Si elle renvoie autre chose que 0, NE PAS executer cette ligne - cela
--    signifierait qu'un Employeur a ete cree depuis, a analyser avant d'agir.)
DROP TABLE IF EXISTS employeur;

-- 2) La cle composite d'Entreprise est passee de (Anneeid, Employeurid) a
--    (Anneeid, compagnie_id). Hibernate (ddl-auto=update) a bien ajoute la
--    nouvelle colonne compagnie_id, mais ne supprime jamais une colonne
--    existante par securite - l'ancienne colonne "employeurid" reste donc en
--    base, NOT NULL sans valeur par defaut, ce qui bloque toute creation
--    d'Entreprise (INSERT rejete par MySQL). A verifier avant suppression :
--    la table doit etre vide (confirme le 2026-08-04).
SELECT COUNT(*) AS nb_entreprises FROM entreprise;

-- 3) Si la verification ci-dessus renvoie bien 0, on peut retirer l'ancienne colonne.
ALTER TABLE entreprise DROP COLUMN employeurid;
