@echo off
REM ============================================================
REM Script de compilación para Windows
REM ============================================================
REM Uso: build.bat
REM Requiere: JDK 11+ en el PATH, libs\AbsoluteLayout.jar presente

setlocal EnableDelayedExpansion
set BASE=%~dp0
set BUILD=%BASE%target-classes
set ABSLAY=%BASE%libs\AbsoluteLayout.jar
set OUT=%BASE%target

echo === Limpiando compilaciones anteriores ===
if exist "%BUILD%" rmdir /s /q "%BUILD%"
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%BUILD%\eventbus"
mkdir "%BUILD%\effectrevolver"
mkdir "%BUILD%\cardfactory"
mkdir "%BUILD%\gamemodel"
mkdir "%BUILD%\networklayer"
mkdir "%BUILD%\presentacionMVC"
mkdir "%OUT%\all-classes"

echo === [1/6] Compilando eventbus ===
for /r "%BASE%eventbus\src" %%f in (*.java) do set SOURCES_EB=!SOURCES_EB! "%%f"
javac -encoding UTF-8 -d "%BUILD%\eventbus" %SOURCES_EB%

echo === [2/6] Compilando effectrevolver ===
for /r "%BASE%effectrevolver\src" %%f in (*.java) do set SOURCES_EF=!SOURCES_EF! "%%f"
javac -encoding UTF-8 -cp "%BUILD%\eventbus" -d "%BUILD%\effectrevolver" %SOURCES_EF%

echo === [3/6] Compilando cardfactory ===
for /r "%BASE%cardfactory\src" %%f in (*.java) do set SOURCES_CF=!SOURCES_CF! "%%f"
javac -encoding UTF-8 -cp "%BUILD%\eventbus;%BUILD%\effectrevolver" -d "%BUILD%\cardfactory" %SOURCES_CF%

echo === [4/6] Compilando gamemodel ===
for /r "%BASE%gamemodel\src" %%f in (*.java) do set SOURCES_GM=!SOURCES_GM! "%%f"
javac -encoding UTF-8 -cp "%BUILD%\eventbus;%BUILD%\effectrevolver;%BUILD%\cardfactory" -d "%BUILD%\gamemodel" %SOURCES_GM%

echo === [5/6] Compilando networklayer ===
for /r "%BASE%networklayer\src" %%f in (*.java) do set SOURCES_NL=!SOURCES_NL! "%%f"
javac -encoding UTF-8 -cp "%BUILD%\eventbus;%BUILD%\effectrevolver;%BUILD%\cardfactory;%BUILD%\gamemodel" -d "%BUILD%\networklayer" %SOURCES_NL%

echo === [6/6] Compilando presentacionMVC ===
for /r "%BASE%presentacionMVC\src" %%f in (*.java) do set SOURCES_MV=!SOURCES_MV! "%%f"
javac -encoding UTF-8 -cp "%BUILD%\eventbus;%BUILD%\effectrevolver;%BUILD%\cardfactory;%BUILD%\gamemodel;%BUILD%\networklayer;%ABSLAY%" -d "%BUILD%\presentacionMVC" %SOURCES_MV%

echo.
echo === Empaquetando JAR ejecutable ===

xcopy /e /q "%BUILD%\*" "%OUT%\all-classes\" >nul

cd "%OUT%\all-classes"
jar xf "%ABSLAY%"
cd "%BASE%"

if exist "%BASE%presentacionMVC\src\main\resources" (
  xcopy /e /q "%BASE%presentacionMVC\src\main\resources\*" "%OUT%\all-classes\" >nul
)

jar --create --file="%OUT%\uno-game.jar" --main-class=Main.MainWindow -C "%OUT%\all-classes" .

echo.
echo ✅  Build exitoso!
echo    JAR: %OUT%\uno-game.jar
echo.
echo Para ejecutar: java -jar %OUT%\uno-game.jar
