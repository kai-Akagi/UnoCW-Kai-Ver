package Logica;

import Logica.CardEffect;
import Dominio.GameState;

/**
 * Efecto de la carta SKIP (Saltar).
 *
 * <p>Cuando se juega esta carta, el siguiente jugador en el turno
 * pierde su oportunidad de jugar. El juego continúa con el jugador
 * que está dos posiciones después del jugador actual.
 *
 * <p><b>Patrón Strategy:</b> Esta clase implementa {@link CardEffect},
 * que es el "contrato" que deben cumplir todos los efectos de cartas.
 * Así el motor del juego no necesita saber qué tipo de carta es;
 * solo llama a {@code effect.apply(gameState)}.
 */
class SkipEffect implements CardEffect {

    /**
     * Aplica el efecto SKIP: salta al siguiente jugador y pasa el turno
     * al jugador que está dos posiciones adelante.
     *
     * @param gameState El estado actual del juego.
     */
    @Override
    public void apply(GameState gameState) {
        // skipNextPlayer() avanza dos posiciones en vez de una
        gameState.skipNextPlayer();
    }

    @Override
    public String getDescription() {
        return "Salta el turno del siguiente jugador.";
    }
}


// ─────────────────────────────────────────────────────────────────────────────


/**
 * Efecto de la carta REVERSE (Invertir).
 *
 * <p>Cuando se juega esta carta, la dirección del juego se invierte.
 * Si el juego iba de izquierda a derecha, ahora va de derecha a izquierda.
 */
class ReverseEffect implements CardEffect {

    /**
     * Aplica el efecto REVERSE: invierte la dirección y avanza el turno
     * en la nueva dirección.
     *
     * @param gameState El estado actual del juego.
     */
    @Override
    public void apply(GameState gameState) {
        gameState.flipDirection();
        if (gameState.getPlayers().size() == 2) {
            // Regla oficial 2 jugadores: REVERSE actúa como SKIP (el mismo jugador vuelve a jugar)
            gameState.skipNextPlayer();
        } else {
            gameState.advanceTurn();
        }
    }

    @Override
    public String getDescription() {
        return "Invierte la dirección del juego.";
    }
}


// ─────────────────────────────────────────────────────────────────────────────


/**
 * Efecto de la carta DRAW TWO (+2).
 *
 * <p>El siguiente jugador debe robar 2 cartas del mazo
 * y pierde su turno.
 */
class DrawTwoEffect implements CardEffect {

    /**
     * Aplica el efecto DRAW TWO: da 2 cartas al siguiente jugador
     * y luego salta su turno.
     *
     * @param gameState El estado actual del juego.
     */
    @Override
    public void apply(GameState gameState) {
        // Primero damos 2 cartas al siguiente jugador
        gameState.giveCards(gameState.getNextPlayer(), 2);
        // Luego saltamos su turno (avanzamos dos posiciones)
        gameState.skipNextPlayer();
    }

    @Override
    public String getDescription() {
        return "El siguiente jugador roba 2 cartas y pierde su turno.";
    }
}


// ─────────────────────────────────────────────────────────────────────────────


/**
 * Efecto de la carta WILD (Comodín simple).
 *
 * <p>El jugador que la juega elige el color que continuará.
 * En esta fase, el color elegido se establece por fuera (en el GameModel),
 * así que aquí solo avanzamos el turno normalmente.
 *
 * <p>La selección de color se maneja como un evento aparte en la UI.
 */
class WildEffect implements CardEffect {

    /**
     * El efecto Wild solo avanza el turno. El cambio de color
     * se gestiona en el {@code GameModel} cuando el jugador elige el color.
     *
     * @param gameState El estado actual del juego.
     */
    @Override
    public void apply(GameState gameState) {
        gameState.advanceTurn();
    }

    @Override
    public String getDescription() {
        return "El jugador elige el color que continúa.";
    }
}


// ─────────────────────────────────────────────────────────────────────────────


/**
 * Efecto de la carta WILD DRAW FOUR (+4 Comodín).
 *
 * <p>El jugador que la juega elige el color que continúa,
 * y el siguiente jugador debe robar 4 cartas y pierde su turno.
 *
 * <p>Igual que {@link WildEffect}, la selección de color ocurre en la UI.
 */
class WildDrawFourEffect implements CardEffect {

    /**
     * Aplica el efecto Wild Draw Four: da 4 cartas al siguiente jugador
     * y salta su turno.
     *
     * @param gameState El estado actual del juego.
     */
    @Override
    public void apply(GameState gameState) {
        gameState.giveCards(gameState.getNextPlayer(), 4);
        gameState.skipNextPlayer();
    }

    @Override
    public String getDescription() {
        return "El siguiente jugador roba 4 cartas y pierde su turno. El jugador elige el color.";
    }
}
