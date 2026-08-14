# CLAUDE.md

Ce fichier fournit des repères à Claude Code (claude.ai/code) pour travailler dans ce dépôt.

## Vue d'ensemble du projet

**EasyCom-Pro / GESCOM** — un backend en microservices Spring Cloud pour un système de gestion commerciale (points de vente, stock, facturation, clients), avec un frontend Angular (AdminLTE). Le code (commentaires, fichiers `.properties`, termes métier) est en français.

## Architecture

Microservices Spring Cloud : découverte de service via Eureka, configuration centralisée via Spring Cloud Config Server, point d'entrée unique via Spring Cloud Gateway.

| Module | Artifact / package | Port | Rôle |
|---|---|---|---|
| `config-serveur/` | `config-serveur` (`com.mcommerce.config_serveur`) | 9101 | Serveur Spring Cloud Config. Récupère la config depuis le dépôt git **distant** `https://github.com/ESPHEREAPI/cloud-config-gescom.git` (branche `main`), pas depuis le dossier local `cloud-config-gescom/` — ce dossier est une copie de travail locale du même dépôt de config, conservée ici pour édition avant push. |
| `eureka-server/` | `eureka-server` (package `zuul_server`, classe principale `ZuulServerApplication`) | 8761 | Serveur de découverte Eureka. **À noter** : malgré le nom du package/de la classe qui évoque "Zuul", ce module est un simple serveur Eureka, pas une gateway Zuul — nommage hérité d'une itération antérieure du projet. |
| `gateway-proxy/` | `gateway-proxy` (`gateway_proxy`) | 8080 (docker) / 8181 (`bootstrap.properties` local, `spring.application.name=api-gateway`) | Spring Cloud Gateway. Routes : `/gateway-proxy/api/microservice-produits/**` → microservice-produits, `/gateway-proxy/suivi/**` → microservice-expedition, `/gateway-proxy/api/**` → microservice-admin (l'ordre compte : la route admin est un catch-all et doit rester en dernier). Centralise la config CORS de tout le système. |
| `microservice-produits/` | `mproduits` (`com.mproduits`) | 9001 | Le service métier/gescom principal : articles, stock, boutiques/magasins, caisse, clients, commandes, devis, facturation/factures, transferts, ventes, versements, photocopies, reconduction, tableaux de bord. JPA + MySQL, PDF (iText), Excel (Apache POI), cache Caffeine. |
| `microservice-administration/` | `service-admin` (`sid.service_admin`) | 9008 | Authentification (JWT via `io.jsonwebtoken`), utilisateurs, rôles/permissions. JPA + MySQL. |
| `mexpedition/microservice-expedition/` | `microservice_expedition` | 9006 (d'après la route gateway) | Microservice d'expédition/suivi. Actuellement un simple squelette Spring Boot (aucun contrôleur/service) — **non branché** dans `docker-compose.yml` ni `start.bat`, contrairement aux autres services. |
| `web/easycompro-gescom/AdminLTE-3_2_0-angular/adminlte-angular-app/` | — | 4200 (dev) | Frontend Angular 18 + AdminLTE 3. Dossiers clés : `bookshoop/` (ecommerce/boutique), `user-manager/`, `module-users/`, `dashboard/`, `auth/`. |

Tous les services Spring Boot tournent en Java 21 / Spring Boot 3.4.x / Spring Cloud 2024.0.1, lisent leur config au démarrage depuis le Config Server (`spring.cloud.config.uri`, codé en dur à `http://localhost:9101` dans le `bootstrap.properties`/`application.properties` de chaque module pour le dev local — Docker le surcharge via la variable d'environnement `SPRING_CLOUD_CONFIG_URI`), et s'enregistrent auprès d'Eureka.

Les deux services métier (`microservice-produits`, `microservice-administration`) pointent vers la même base/schéma MySQL (`easycom_db`), avec `spring.jpa.hibernate.ddl-auto=update`.

## Commandes

Chaque module backend est un projet Maven indépendant (son propre `pom.xml`, son propre `mvnw`) — il n'y a pas de POM parent/reactor qui les relie, donc les commandes de build/run doivent être lancées depuis l'intérieur de chaque dossier de module.

```bash
# build + tests d'un module (depuis l'intérieur de ex. microservice-produits/)
./mvnw clean package
# sans les tests
./mvnw clean package -DskipTests
# lancer une seule classe de test
./mvnw test -Dtest=NomDeLaClasseTest
# lancer un module en local (nécessite config-server + eureka déjà démarrés)
./mvnw spring-boot:run
```

Frontend (`web/easycompro-gescom/AdminLTE-3_2_0-angular/adminlte-angular-app/`) :
```bash
npm install
npm start        # ng serve, http://localhost:4200
npm run build     # build de production
npm test          # tests unitaires Karma/Jasmine
```

### Lancer la stack complète

L'ordre de démarrage compte : config-server → eureka-server → gateway → (admin, produits) → expedition.

- **Docker (recommandé, proche de la prod) :** `docker compose up -d` (fichier `docker-compose.yml` à la racine) — build et lance MySQL, config-server, eureka-server, gateway, microservice-produits, microservice-admin, phpMyAdmin (`:8090`), le frontend Angular et Caddy, tous sur le réseau bridge `easycom-network` avec des `depends_on` conditionnés par healthcheck. **Point d'entrée public unique : Caddy (`:80`/`:443`, config dans `Caddyfile` à la racine)** — gateway et frontend ne sont plus exposés sur l'hôte (`8080`/`8081` fermés), seul le réseau docker interne y accède ; Caddy termine le TLS (certificat Let's Encrypt obtenu/renouvelé automatiquement) et reverse-proxy tout vers `frontend:80`, qui proxy lui-même en interne `/gateway-proxy/` et `/api/` vers `gateway:8080` (voir `nginx.conf` du frontend). Nom de domaine actuellement un sous-domaine `sslip.io` temporaire (résout vers l'IP du serveur sans DNS à configurer) — à remplacer par le vrai domaine dans `Caddyfile` dès qu'il est disponible, seule ligne à changer. `environment.prod.ts` du frontend utilise `apiUrl: ''` (chemin relatif, même origine que la page) pour que ça fonctionne en HTTPS sans contenu mixte ni CORS.
- **Docker (dev, frontend en hot-reload) :** `docker-compose.dev.yml` — jeu de services plus léger (mysql, config-server, eureka, gateway, frontend sur `:4200`), mais suppose une arborescence `./backend/*` et `./frontend` qui **ne correspond pas** à la structure actuelle du dépôt (`config-serveur/`, `eureka-server/`, etc. directement à la racine) — à considérer comme obsolète/aspirationnel tant que ce n'est pas réconcilié.
- **Sur poste Windows, à partir des JAR déjà construits :** `start.bat` — attend `config-serveur.jar`, `eureka-server.jar`, `gateway-proxy.jar`, `service-admin.jar`, `mproduits.jar` à côté de lui, et les lance séquentiellement avec des délais fixes (30s/30s/30s/5s/5s) via `java -jar`, en journalisant dans `.\logs\`.

En local (hors Docker, profil `dev`), la BDD par défaut est `jdbc:mysql://localhost:3306/easycom_db` avec `root` / `DeepWater@2021`, définie dans `cloud-config-gescom/<service>-dev.properties` (pas dans le module lui-même — voir « Travailler avec le dépôt de config » ci-dessous). En profil `prod`, aucun identifiant DB n'est défini dans un fichier versionné : `docker-compose.yml` exige `MYSQL_ROOT_PASSWORD` via `.env` (pas de valeur par défaut, le démarrage échoue si absent), exposé sur le port hôte `3307`. Ce sont deux chemins de configuration distincts — ne pas supposer que les valeurs du `.env` s'appliquent à un lancement local (hors Docker).

## Travailler avec le dépôt de config

`cloud-config-gescom/` à la racine du dépôt contient des fichiers `.properties` par service (`api-gateway.properties`, `config-serve.properties`, `eureka-server.properties`, `microservice-admin.properties`, `microservice-produits.properties`) qui reflètent ce qui est attendu dans le dépôt git distant `ESPHEREAPI/cloud-config-gescom`, celui que le config-server sert réellement. Les changements ici ne prennent effet qu'une fois poussés (push) vers ce dépôt distant et après un `force-pull`/refresh du config-server — modifier cette copie locale seule ne change pas le comportement à l'exécution.

## Incohérences connues à garder en tête

- Le package/la classe Java du module `eureka-server` est `zuul_server.ZuulServerApplication` — c'est un serveur Eureka, pas Zuul.
- `gateway-proxy` écoute sur le port 8181 dans son propre `bootstrap.properties` (`spring.application.name=api-gateway`), mais toutes les autres références (docker-compose, `cloud-config-gescom`, routes gateway utilisées par le frontend) supposent le port 8080 — vérifier laquelle des deux configs est réellement chargée (bootstrap local vs. config fournie par le config-server) avant de faire confiance à l'une ou l'autre.
- Le nom du dossier de module, l'artifact Maven et le package Java de base diffèrent selon les services (ex. dossier `microservice-administration` → artifact `service-admin` → package `sid.service_admin` ; dossier `microservice-produits` → artifact `mproduits` → package `com.mproduits`). Ne pas déduire l'un à partir de l'autre.
- `docker-compose.dev.yml` suppose une arborescence en sous-dossiers `backend/`/`frontend/` qui ne correspond pas à l'organisation actuelle des modules à la racine du dépôt.
- `mexpedition/microservice-expedition` existe comme squelette mais n'est inclus ni dans `docker-compose.yml` ni dans `start.bat` ; à considérer comme non terminé/hors du système en fonctionnement, sauf si vous travaillez activement dessus.
