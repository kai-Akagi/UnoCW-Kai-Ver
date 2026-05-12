#!/bin/bash
# ============================================================
# Instala AbsoluteLayout.jar en el repositorio local de Maven.
# Ejecutar UNA sola vez antes del primer "mvn compile" o "mvn package".
# ============================================================
set -e
BASE=$(dirname "$(realpath "$0")")
JAR="$BASE/libs/AbsoluteLayout.jar"

echo "=== Instalando AbsoluteLayout en el repositorio Maven local ==="
mvn install:install-file \
    -Dfile="$JAR" \
    -DgroupId=org.netbeans.external \
    -DartifactId=AbsoluteLayout \
    -Dversion=RELEASE180 \
    -Dpackaging=jar \
    -DgeneratePom=true

echo ""
echo "✅ AbsoluteLayout instalado correctamente."
echo ""
echo "Ahora puedes compilar y ejecutar con:"
echo "  mvn install -DskipTests"
echo "  mvn exec:java -pl presentacionMVC"
echo ""
echo "O generar el fat JAR ejecutable:"
echo "  mvn package -pl presentacionMVC --also-make"
echo "  java -jar presentacionMVC/target/presentacionMVC-1.0-SNAPSHOT-jar-with-dependencies.jar"
