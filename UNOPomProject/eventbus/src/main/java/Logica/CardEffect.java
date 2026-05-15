package Logica;

import Dominio.GameState;

/**
 * Contrato que define qué significa "tener un efecto" para una carta especial.
 *
 * <p>Este es el corazón del patrón <b>Strategy</b>: definimos una interfaz común
 * para todos los efectos posibles de una carta. Luego, cada tipo de carta especial
 * tiene su propia clase que implementa esta interfaz con su lógica particular.
 *
 * <p><b>¿Por qué esto es mejor que un if gigante?</b><br>
 * Sin Strategy, tendríamos algo así en el código:
 * <pre>
 *   if (carta.getValue().equals("SKIP")) {
 *       // saltar turno...
 *   } else if (carta.getValue().equals("REVERSE")) {
 *       // invertir dirección...
 *   } else if (carta.getValue().equals("DRAW_TWO")) {
 *       // robar 2 cartas...
 *   }
 *   // y así con cada carta especial...
 * </pre>
 * Con Strategy, simplemente hacemos:
 * <pre>
 *   carta.getEffect().apply(gameState);
 * </pre>
 * Cada carta sabe cómo aplicarse sola. Si mañana agregamos una carta nueva,
 * solo creamos una clase nueva sin tocar el código existente.
 *
 * <p><b>Principio que aplica:</b> Abierto para extensión, cerrado para modificación
 * (Open/Closed Principle). Podemos agregar efectos nuevos sin modificar los existentes.
 */
public interface CardEffect {

    /**
     * Aplica el efecto de esta carta al estado actual del juego.
     *
     * <p>Por ejemplo, un {@link SkipEffect} usará el {@code gameState}
     * para saltar al siguiente jugador. Un {@link DrawTwoEffect} le
     * asignará 2 cartas al siguiente jugador.
     *
     * @param gameState El estado actual del juego. Contiene información
     *                  sobre jugadores, turnos, mazo, etc.
     */
    void apply(GameState gameState);

    /**
     * Devuelve una descripción legible de este efecto.
     * Útil para mostrar en la interfaz o para depuración.
     *
     * @return Texto que describe el efecto, ej. "Salta el turno del siguiente jugador".
     */
    String getDescription();
}
