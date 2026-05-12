@echo off
REM Instala AbsoluteLayout.jar en el repositorio local de Maven (Windows)
set BASE=%~dp0
set JAR=%BASE%libs\AbsoluteLayout.jar

echo === Instalando AbsoluteLayout en el repositorio Maven local ===
mvn install:install-file -Dfile="%JAR%" -DgroupId=org.netbeans.external -DartifactId=AbsoluteLayout -Dversion=RELEASE180 -Dpackaging=jar -DgeneratePom=true

echo.
echo Ahora ejecuta:
echo   mvn install -DskipTests
echo   mvn exec:java -pl presentacionMVC
