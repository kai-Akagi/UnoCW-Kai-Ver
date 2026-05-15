package Logica;

import Logica.CardEffect;

/**
 * Fábrica de efectos de cartas.
 *
 * <p>Esta clase es el único punto de acceso para crear efectos de cartas.
 * Las clases concretas de efectos ({@code SkipEffect}, {@code ReverseEffect}, etc.)
 * son de acceso de paquete (sin {@code public}), así que nadie fuera de este paquete
 * puede crearlas directamente. Deben pedirlas a través de esta clase.
 *
 * <p><b>¿Por qué hacerlo así?</b><br>
 * Ocultar las clases concretas y exponer solo esta fábrica nos permite:
 * <ul>
 *   <li>Cambiar cómo se implementa un efecto sin que el resto del código lo sepa.</li>
 *   <li>Tener un solo lugar donde se crean los efectos (control centralizado).</li>
 *   <li>Hacer el código más legible: {@code CardEffectFactory.createSkip()} es claro.</li>
 * </ul>
 * 
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class CardEffectFactory {

    /**
     * Constructor privado: no se puede instanciar esta clase.
     * Todos sus métodos son estáticos, así que no necesitas crear un objeto.
     */
    private CardEffectFactory() {}

    /**
     * Crea el efecto de la carta SKIP (Saltar).
     *
     * @return Un efecto que salta el turno del siguiente jugador.
     */
    public static CardEffect createSkip() {
        return new SkipEffect();
    }

    /**
     * Crea el efecto de la carta REVERSE (Invertir).
     *
     * @return Un efecto que invierte la dirección del juego.
     */
    public static CardEffect createReverse() {
        return new ReverseEffect();
    }

    /**
     * Crea el efecto de la carta DRAW TWO (+2).
     *
     * @return Un efecto que hace robar 2 cartas al siguiente jugador y lo salta.
     */
    public static CardEffect createDrawTwo() {
        return new DrawTwoEffect();
    }

    /**
     * Crea el efecto de la carta WILD (Comodín simple).
     *
     * @return Un efecto que permite al jugador elegir el color.
     */
    public static CardEffect createWild() {
        return new WildEffect();
    }

    /**
     * Crea el efecto de la carta WILD DRAW FOUR (+4 Comodín).
     *
     * @return Un efecto que hace robar 4 cartas al siguiente jugador y lo salta.
     */
    public static CardEffect createWildDrawFour() {
        return new WildDrawFourEffect();
    }
}
