#!/bin/bash
# ============================================================
# Script de compilación manual (sin Maven Central)
# ============================================================
set -e
BASE=$(dirname "$(realpath "$0")")
BUILD="$BASE/target-classes"
ABSLAY="$BASE/libs/AbsoluteLayout.jar"
OUT="$BASE/target"

echo "=== Limpiando compilaciones anteriores ==="
rm -rf "$BUILD" "$OUT"
mkdir -p "$BUILD"/{eventbus,effectrevolver,cardfactory,gamemodel,networklayer,presentacionMVC}
mkdir -p "$OUT/all-classes"

echo "=== [1/6] Compilando eventbus ==="
javac -encoding UTF-8 -d "$BUILD/eventbus" \
  $(find "$BASE/eventbus/src" -name "*.java")

echo "=== [2/6] Compilando effectrevolver ==="
javac -encoding UTF-8 -cp "$BUILD/eventbus" \
  -d "$BUILD/effectrevolver" \
  $(find "$BASE/effectrevolver/src" -name "*.java")

echo "=== [3/6] Compilando cardfactory ==="
javac -encoding UTF-8 \
  -cp "$BUILD/eventbus:$BUILD/effectrevolver" \
  -d "$BUILD/cardfactory" \
  $(find "$BASE/cardfactory/src" -name "*.java")

echo "=== [4/6] Compilando gamemodel ==="
javac -encoding UTF-8 \
  -cp "$BUILD/eventbus:$BUILD/effectrevolver:$BUILD/cardfactory" \
  -d "$BUILD/gamemodel" \
  $(find "$BASE/gamemodel/src" -name "*.java")

echo "=== [5/6] Compilando networklayer ==="
javac -encoding UTF-8 \
  -cp "$BUILD/eventbus:$BUILD/effectrevolver:$BUILD/cardfactory:$BUILD/gamemodel" \
  -d "$BUILD/networklayer" \
  $(find "$BASE/networklayer/src" -name "*.java")

echo "=== [6/6] Compilando presentacionMVC ==="
javac -encoding UTF-8 \
  -cp "$BUILD/eventbus:$BUILD/effectrevolver:$BUILD/cardfactory:$BUILD/gamemodel:$BUILD/networklayer:$ABSLAY" \
  -d "$BUILD/presentacionMVC" \
  $(find "$BASE/presentacionMVC/src" -name "*.java")

echo ""
echo "=== Empaquetando fat JAR ==="
cp -r "$BUILD"/*/. "$OUT/all-classes/"

# Extraer AbsoluteLayout DENTRO del fat JAR
cd "$OUT/all-classes"
jar xf "$ABSLAY"
cd "$BASE"

cp -r "$BASE/presentacionMVC/src/main/resources/." "$OUT/all-classes/" 2>/dev/null || true

jar --create --file="$OUT/uno-game.jar" \
    --main-class=Main.MainWindow \
    -C "$OUT/all-classes" .

echo ""
echo "✅  Build exitoso!"
echo "   JAR: $OUT/uno-game.jar"
echo ""
echo "Para ejecutar: java -jar $OUT/uno-game.jar"
