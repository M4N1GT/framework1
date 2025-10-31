@echo off
REM ------------------------------------------------------------------------
REM Script de déploiement Windows pour compiler le framework et préparer le projet de test
REM ------------------------------------------------------------------------

REM Définition des chemins (à adapter si besoin)
REM Répertoire du script (framework/) pour rendre le script portable
set "FRAMEWORK_DIR=%~dp0"
set "BUILD_DIR=%FRAMEWORK_DIR%build"
set "TEST_DIR=D:\ProNaina\apache-tomcat-10.1.34\webapps\testFramework"
set "SERVLET_JAR=%FRAMEWORK_DIR%jakarta.servlet-api_5.0.0.jar"
set "WEBXML_FILE=%FRAMEWORK_DIR%web.xml"

REM Création des dossiers de sortie du framework (avec nettoyage pour éviter les classes périmées)
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if exist "%BUILD_DIR%\classes" rmdir /S /Q "%BUILD_DIR%\classes"
mkdir "%BUILD_DIR%\classes"

REM Compilation récursive des sources Java du framework
echo Compilation du framework...
for /R "%FRAMEWORK_DIR%" %%f in (*.java) do (
    echo   Compilation de %%~nxf
    javac -classpath "%SERVLET_JAR%;%BUILD_DIR%\classes" -d "%BUILD_DIR%\classes" "%%f"
    if errorlevel 1 (
        echo Erreur de compilation du fichier %%~nxf
        exit /b 1
    )
)

REM Création du JAR du framework
echo Creation du JAR du framework...
cd /d "%BUILD_DIR%"
if exist "framework.jar" del "framework.jar"
jar cvf "framework.jar" -C "classes" .

REM Copie du framework.jar dans le projet Test
echo Copie du framework.jar dans le projet Test...
if not exist "%TEST_DIR%\WEB-INF\lib" mkdir "%TEST_DIR%\WEB-INF\lib"
xcopy "%BUILD_DIR%\framework.jar" "%TEST_DIR%\WEB-INF\lib\" /Y >nul

REM Copie du web.xml dans le projet Test
echo Copie du web.xml dans le projet Test...
if not exist "%TEST_DIR%\WEB-INF" mkdir "%TEST_DIR%\WEB-INF"
if exist "%WEBXML_FILE%" (
    xcopy "%WEBXML_FILE%" "%TEST_DIR%\WEB-INF\" /Y >nul
) else (
    echo ATTENTION : web.xml introuvable dans %WEBXML_FILE%
)

REM Compilation de l'application test et copie des classes dans WEB-INF/classes
echo Compilation de l'application test...
set "APP_SRC=D:\framework\TP1\framework1\testFramework\src"
set "APP_CLASSES=%TEST_DIR%\WEB-INF\classes"

REM Nettoyage des classes de l'application pour éviter les artefacts périmés
if exist "%APP_CLASSES%" rmdir /S /Q "%APP_CLASSES%"
mkdir "%APP_CLASSES%"

for /R "%APP_SRC%" %%f in (*.java) do (
    echo   Compilation de %%~nxf
    javac -classpath "%SERVLET_JAR%;%BUILD_DIR%\framework.jar" -d "%APP_CLASSES%" "%%f"
    if errorlevel 1 (
        echo Erreur de compilation du fichier %%~nxf
        exit /b 1
    )
)

REM Démarrage de Tomcat si un chemin est passé en paramètre
if not "%~1"=="" (
    echo Demarrage de Tomcat...
    call "%~1\bin\startup.bat"
)

echo Deploiement terminé.
