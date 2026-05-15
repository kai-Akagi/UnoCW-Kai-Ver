package Dominio;

import Logica.CardEffect;

/**
 * Representa una carta del juego UNO.
 *
 * <p>
 * Una carta tiene tres características:
 * <ul>
 * <li><b>Color:</b> ROJO, AZUL, VERDE, AMARILLO, o WILD (sin color).</li>
 * <li><b>Valor:</b> Un número del 0 al 9, o el nombre de una carta especial
 * (ej. "SKIP", "REVERSE", "DRAW_TWO", "WILD", "WILD_DRAW_FOUR").</li>
 * <li><b>Efecto:</b> La acción que ocurre al jugarla. Las numéricas no tienen.
 * Las especiales sí, usando el patrón Strategy.</li>
 * </ul>
 *
 * <p>
 * <b>Patrones aplicados:</b>
 * <ul>
 * <li><b>Strategy:</b> el campo {@code effect} encapsula el comportamiento
 * especial de la carta. Así evitamos un bloque enorme de ifs.</li>
 * <li><b>Factory:</b> estas cartas son creadas exclusivamente por
 * {@link uno.factory.CardFactory}, no directamente por el resto del
 * código.</li>
 * </ul>
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class Card {

    /**
     * Los colores posibles de una carta UNO. WILD se usa para cartas comodín
     * que no tienen color fijo.
     */
    public enum Color {
        RED, BLUE, GREEN, YELLOW, WILD
    }

    /**
     * El color de esta carta.
     */
    private final Color color;

    /**
     * El valor de la carta en texto. Ejemplos: "0", "7", "SKIP", "REVERSE",
     * "DRAW_TWO", "WILD", "WILD_DRAW_FOUR".
     */
    private final String value;

    /**
     * El comportamiento especial de esta carta. Es null si la carta es numérica
     * (no hace nada especial al jugarse).
     *
     * <p>
     * <b>Por qué así:</b> En vez de preguntar "¿qué tipo eres?" con ifs, le
     * decimos a la carta "ejecuta tu efecto" y ella sabe qué hacer. Eso es el
     * patrón Strategy: el comportamiento es intercambiable.
     */
    private final CardEffect effect;

    /**
     * Construye una carta con su color, valor y efecto.
     *
     * @param color El color de la carta.
     * @param value El valor o nombre de la carta.
     * @param effect El efecto especial, o {@code null} si no tiene.
     */
    public Card(Color color, String value, CardEffect effect) {
        this.color = color;
        this.value = value;
        this.effect = effect;
    }

    /**
     * @return El color de esta carta.
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return El valor de esta carta como texto.
     */
    public String getValue() {
        return value;
    }

    /**
     * Devuelve el efecto especial de esta carta.
     *
     * @return El efecto, o {@code null} si la carta es numérica.
     */
    public CardEffect getEffect() {
        return effect;
    }

    /**
     * Indica si esta carta tiene un efecto especial.
     *
     * @return {@code true} si tiene efecto, {@code false} si es numérica.
     */
    public boolean hasEffect() {
        return effect != null;
    }

    /**
     * Decide si esta carta puede jugarse sobre la carta que está en la mesa.
     *
     * <p>
     * Reglas de UNO:
     * <ul>
     * <li>Mismo color que la carta de la mesa, O</li>
     * <li>Mismo valor que la carta de la mesa, O</li>
     * <li>Es comodín (WILD), que siempre puede jugarse.</li>
     * </ul>
     *
     * @param topCard La carta que está encima de la pila de descarte.
     * @return {@code true} si se puede jugar, {@code false} si no.
     */
    public boolean canBePlacedOn(Card topCard) {
        return canBePlacedOn(topCard, topCard.getColor());
    }

    /**
     * Verifica si esta carta puede jugarse considerando el color activo elegido
     * tras un comodín. El color activo puede diferir del color de la carta
     * activa.
     *
     * @param topCard La carta en la cima del descarte.
     * @param activeColor El color que realmente rige (elegido tras un comodín).
     * @return {@code true} si esta carta se puede jugar.
     */
    public boolean canBePlacedOn(Card topCard, Color activeColor) {
        if (this.color == Color.WILD) {
            return true;
        }
        if (activeColor != null && activeColor != Color.WILD) {
            return this.color == activeColor || this.value.equals(topCard.value);
        }
        return this.color == topCard.color || this.value.equals(topCard.value);
    }

    /**
     * Devuelve esta carta como texto legible. Ejemplo: "RED-7", "BLUE-SKIP",
     * "WILD_DRAW_FOUR".
     */
    @Override
    public String toString() {
        return (color == Color.WILD) ? value : color + "-" + value;
    }
}
