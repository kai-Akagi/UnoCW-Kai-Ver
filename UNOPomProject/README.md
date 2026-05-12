# UNO Game — Proyecto Maven Multi-Módulo

## Estructura de módulos

```
uno-maven/
├── pom.xml                    ← POM raíz
├── eventbus/                  ← Dominio + EventBus + todos los eventos
├── effectrevolver/            ← CardEffect + implementaciones (Strategy)
├── cardfactory/               ← CardFactory (108 cartas)
├── gamemodel/                 ← GameModel (motor del juego)
├── networklayer/              ← NetworkLayer + sockets + GameSession
├── presentacionMVC/           ← Vistas Swing + Controladores + MainWindow
├── libs/
│   └── AbsoluteLayout.jar     ← Requerido por los formularios NetBeans
├── install-deps.sh            ← Instala AbsoluteLayout en Maven local (Linux/Mac)
├── install-deps.bat           ← Mismo para Windows
└── build.sh                   ← Build manual sin Maven Central
```

## Cadena de dependencias (sin ciclos)

```
eventbus ← effectrevolver ← cardfactory ← gamemodel ← networklayer ← presentacionMVC
```

## ▶ Cómo correr el juego

### Opción A — Maven (recomendado si tienes internet)

**Paso 1 — Una sola vez:** registrar AbsoluteLayout en tu repo Maven local:
```bash
# Linux / Mac
./install-deps.sh

# Windows
install-deps.bat
```

**Paso 2 — Compilar todos los módulos:**
```bash
mvn install -DskipTests
```

**Paso 3 — Ejecutar:**
```bash
# Con exec plugin (desarrollo)
mvn exec:java -pl presentacionMVC

# O con el fat JAR (producción)
mvn package -pl presentacionMVC --also-make
java -jar presentacionMVC/target/presentacionMVC-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Opción B — Script manual (sin Maven Central, solo JDK)
```bash
./build.sh
java -jar target/uno-game.jar
```

## ¿Por qué el error NoClassDefFoundError: AbsoluteLayout?

Maven con `scope=system` **excluye** el JAR del classpath de runtime cuando se usa
`mvn exec:java`. La solución es instalar el JAR en el repositorio local (`~/.m2`)
con `install-deps.sh`, lo que lo convierte en una dependencia normal con
`scope=compile`.

Como respaldo adicional, el POM de `presentacionMVC` incluye una directiva
`<additionalClasspathElement>` en el exec-plugin que apunta directamente a
`libs/AbsoluteLayout.jar`, garantizando que funcione incluso antes de correr
`install-deps.sh`.

## Bugs corregidos respecto al proyecto Ant original

1. **EventBus y Deck Singleton sin thread-safety** — `volatile` + doble-checked locking.
2. **`checkStartCondition()` llamaba `setStartEnabled()` dos veces** — Duplicado eliminado.
3. **`drawFirstNonWild()` ponía comodines en el descarte** — Corregido.
4. **`drawCard()` no limpiaba `pendingUnoPlayer`** — El periodo de gracia UNO podía quedar activo para siempre. Corregido.
5. **`onCapacityChanged()` podía lanzar NPE** — Añadido null guard.
6. **Dependencia circular `Deck → CardFactory`** — `Deck.reset(List<Card>)` resuelve el ciclo.
