@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

REM ============================================
REM  Ce script lance les services en profil PROD par defaut
REM  (JAR deja construits = scenario de deploiement).
REM  Variables d'environnement Windows a definir AVANT d'executer
REM  ce script - elles ne sont JAMAIS codees en dur ici :
REM    SPRING_CLOUD_CONFIG_URI     (optionnel, defaut http://localhost:9101)
REM    SPRING_DATASOURCE_URL       (ex: jdbc:mysql://localhost:3306/easycom_db?...)
REM    SPRING_DATASOURCE_USERNAME
REM    SPRING_DATASOURCE_PASSWORD
REM    APP_JWTSECRET               (secret JWT du service-admin)
REM  Exemple (PowerShell, avant de lancer start.bat) :
REM    $env:SPRING_DATASOURCE_PASSWORD = "..."
REM    $env:APP_JWTSECRET = "..."
REM  Pour repasser en profil dev (config/BDD locales), definir avant :
REM    SET SPRING_PROFILES_ACTIVE=dev
REM ============================================
IF NOT DEFINED SPRING_PROFILES_ACTIVE SET "SPRING_PROFILES_ACTIVE=prod"

REM Configuration
SET "JAR_DIR=%~dp0"
SET "JAVA_OPTS=-Xmx1024m -Xms512m"
SET "LOG_DIR=%JAR_DIR%logs"

REM Délais d'attente entre les services (en secondes)
SET "CONFIG_DELAY=30"
SET "EUREKA_DELAY=30"
SET "GATEWAY_DELAY=30"
SET "ADMIN_DELAY=5"
SET "MPRODUITS_DELAY=5"

REM Créer dossier logs s'il n'existe pas
IF NOT EXIST "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
)

echo ============================================
echo    DEMARRAGE SEQUENTIEL DES SERVICES JAR
echo ============================================
echo Repertoire: %JAR_DIR%
echo Logs: %LOG_DIR%
echo.

REM Démarrage séquentiel des services
CALL :startJarSequential config-serveur.jar %CONFIG_DELAY% "Configuration Server"
CALL :startJarSequential eureka-server.jar %EUREKA_DELAY% "Eureka Discovery Server"
CALL :startJarSequential gateway-proxy.jar %GATEWAY_DELAY% "API Gateway"
CALL :startJarSequential service-admin.jar %ADMIN_DELAY% "Admin Service"
CALL :startJarSequential mproduits.jar %MPRODUITS_DELAY% "Service Produits"

echo.
echo ============================================
echo    TOUS LES SERVICES SONT DEMARRES
echo ============================================
echo.
echo Appuyez sur une touche pour voir les logs en temps reel...
pause > nul

REM Afficher les logs en temps réel
CALL :showLogs

GOTO :EOF

:startJarSequential
SET "JAR_FILE=%~1"
SET "DELAY=%~2"
SET "SERVICE_NAME=%~3"
SET "LOG_FILE=%LOG_DIR%\%~n1.log"

echo [%TIME%] Demarrage de %SERVICE_NAME% (%JAR_FILE%)...

IF NOT EXIST "%JAR_DIR%%JAR_FILE%" (
    echo [ERREUR] Le fichier %JAR_FILE% est introuvable.
    echo.
    GOTO :EOF
)

REM Créer un fichier de log vide
echo. > "%LOG_FILE%"

REM Lancer le service en arrière-plan
start /B "Java - %JAR_FILE%" cmd /c "java %JAVA_OPTS% -jar "%JAR_DIR%%JAR_FILE%" >> "%LOG_FILE%" 2>&1"

echo [%TIME%] ✓ %SERVICE_NAME% lance avec succes.
echo [%TIME%] → Attente de %DELAY% secondes avant le prochain service...
echo.

REM Attendre avant de lancer le service suivant
timeout /t %DELAY% /nobreak > nul

GOTO :EOF

:showLogs
echo Choix d'affichage des logs:
echo 1. Affichage en temps reel de tous les logs
echo 2. Affichage d'un service specifique
echo 3. Retour au menu principal
echo.
SET /P "choice=Votre choix (1-3): "

IF "%choice%"=="1" GOTO :showAllLogs
IF "%choice%"=="2" GOTO :showSpecificLog
IF "%choice%"=="3" GOTO :mainMenu

:showAllLogs
echo.
echo ============================================
echo    LOGS EN TEMPS REEL - TOUS LES SERVICES
echo ============================================
echo Appuyez sur Ctrl+C pour arreter l'affichage
echo.

REM Utiliser PowerShell pour afficher les logs en temps réel
powershell -Command "Get-ChildItem '%LOG_DIR%\*.log' | ForEach-Object { Get-Content $_.FullName -Wait -Tail 10 | ForEach-Object { Write-Host \"[$($_.Name)] $_\" } }"

GOTO :EOF

:showSpecificLog
echo.
echo Services disponibles:
echo 1. config-serveur
echo 2. eureka-server  
echo 3. gateway-proxy
echo 4. service-admin
echo 5. mproduits
echo.
SET /P "service_choice=Choisissez un service (1-5): "

IF "%service_choice%"=="1" SET "LOG_FILE=%LOG_DIR%\config-serveur.log"
IF "%service_choice%"=="2" SET "LOG_FILE=%LOG_DIR%\eureka-server.log"
IF "%service_choice%"=="3" SET "LOG_FILE=%LOG_DIR%\gateway-proxy.log"
IF "%service_choice%"=="4" SET "LOG_FILE=%LOG_DIR%\service-admin.log"
IF "%service_choice%"=="5" SET "LOG_FILE=%LOG_DIR%\mproduits.log"

IF NOT DEFINED LOG_FILE (
    echo Choix invalide.
    GOTO :showSpecificLog
)

echo.
echo ============================================
echo    LOGS EN TEMPS REEL - %LOG_FILE%
echo ============================================
echo Appuyez sur Ctrl+C pour arreter l'affichage
echo.

REM Afficher les logs du service spécifique
powershell -Command "Get-Content '%LOG_FILE%' -Wait -Tail 10"

GOTO :EOF

:mainMenu
echo.
echo Que souhaitez-vous faire?
echo 1. Voir les logs
echo 2. Redemarrer tous les services
echo 3. Arreter tous les services
echo 4. Quitter
echo.
SET /P "main_choice=Votre choix (1-4): "

IF "%main_choice%"=="1" GOTO :showLogs
IF "%main_choice%"=="2" GOTO :restartServices
IF "%main_choice%"=="3" GOTO :stopServices
IF "%main_choice%"=="4" GOTO :EOF

GOTO :mainMenu

:restartServices
echo.
echo Arret des services en cours...
REM Arrêter tous les processus Java
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find /c /v ""') do (
    if %%i gtr 1 (
        taskkill /F /IM java.exe > nul 2>&1
    )
)
timeout /t 5 /nobreak > nul
echo Services arretes.
echo.
echo Redemarrage des services...
GOTO :EOF

:stopServices
echo.
echo Arret de tous les services Java...
REM Arrêter tous les processus Java liés aux JAR
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find /c /v ""') do (
    if %%i gtr 1 (
        echo Services Java detectes, arret en cours...
        taskkill /F /IM java.exe > nul 2>&1
    )
)
echo Services arretes.
echo.
pause
GOTO :EOF