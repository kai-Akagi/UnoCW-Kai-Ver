package Dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El mazo de cartas del juego UNO.
 *
 * <p>
 * Existe exactamente un mazo por partida. Esta clase aplica el patrón
 * <b>Singleton</b> para garantizarlo: no importa cuántas veces pidas el mazo,
 * siempre recibes el mismo objeto.
 *
 * <p>
 * <b>¿Por qué Singleton aquí?</b><br>
 * Imagina que dos partes del juego pudieran crear su propio mazo. Un jugador
 * robaría cartas de un mazo y otro de otro distinto. Las cartas se duplicarían
 * y el juego sería incoherente. Con Singleton garantizamos que todos comparten
 * el mismo mazo físico.
 *
 * <p>
 * <b>Responsabilidades de esta clase:</b>
 * <ul>
 * <li>Guardar las cartas que aún no han sido repartidas (la pila de robo).</li>
 * <li>Entregar cartas una por una cuando alguien roba.</li>
 * <li>Recargar la pila de robo usando las cartas del descarte cuando se
 * acaba.</li>
 * </ul>
 *
 * <p>
 * <b>Nota importante:</b> En una partida nueva, hay que llamar a reset() para
 * reinicializar el mazo con las 108 cartas frescas. Esto permite reutilizar el
 * Singleton entre partidas sin reiniciar el programa.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class Deck {

    /**
     * La única instancia del mazo que existirá en toda la aplicación. Se crea
     * la primera vez que alguien llama a {@link #getInstance()}.
     */
    private static Deck instance;

    /**
     * La pila de robo: las cartas que los jugadores pueden tomar. Cuando se
     * acaba, se recarga con las cartas del descarte.
     */
    private List<Card> drawPile;

    /**
     * La pila de descarte: las cartas ya jugadas. La carta en la cima (última
     * de la lista) es la carta activa en la mesa.
     */
    private List<Card> discardPile;

    /**
     * Constructor privado: nadie puede crear un Deck con {@code new Deck()}. La
     * única forma de obtenerlo es a través de {@link #getInstance()}.
     */
    private Deck() {
        this.drawPile = new ArrayList<>();
        this.discardPile = new ArrayList<>();
    }

    /**
     * Devuelve la única instancia del mazo.
     *
     * <p>
     * Si el mazo aún no existe, lo crea. Si ya existe, devuelve el mismo. Esto
     * es la esencia del patrón Singleton.
     *
     * <p>
     * <b>Thread-safety:</b> En este proyecto simplificado no necesitamos
     * sincronización aquí, ya que el mazo solo se accede desde el hilo del
     * juego.
     *
     * @return La instancia única del mazo.
     */
    public static Deck getInstance() {
        if (instance == null) {
            instance = new Deck();
        }
        return instance;
    }

    /**
     * Reinicia el mazo con las 108 cartas completas de UNO.
     *
     * reset() recibe la lista de cartas desde fuera
     *
     * @param fullDeck Lista completa de cartas que se usará para reconstruir el
     * mazo.
     *
     */
    public void reset(List<Card> fullDeck) {
        drawPile = new ArrayList<>(fullDeck);
        discardPile = new ArrayList<>();
    }

    /**
     * Saca y devuelve la carta que está en la cima de la pila de robo.
     *
     * <p>
     * Si la pila de robo está vacía, intenta recargarla con las cartas del
     * descarte (excepto la carta que está en la mesa actualmente).
     *
     * @return La carta robada, o {@code null} si no hay cartas disponibles.
     */
    public Card drawCard() {
        if (drawPile.isEmpty()) {
            reloadFromDiscard();
        }
        if (drawPile.isEmpty()) {
            return null; // Caso extremo: no hay cartas en ninguna pila
        }
        // Quitamos y devolvemos la última carta (es como tomar de arriba de la pila)
        return drawPile.remove(drawPile.size() - 1);
    }

    /**
     * Agrega una carta a la pila de descarte. Se usa cuando un jugador juega
     * una carta.
     *
     * @param card La carta que se jugó.
     */
    public void discard(Card card) {
        discardPile.add(card);
    }

    /**
     * Devuelve la carta que está en la cima del descarte (la carta activa en la
     * mesa).
     *
     * @return La carta activa, o {@code null} si aún no se ha jugado ninguna.
     */
    public Card getTopDiscard() {
        if (discardPile.isEmpty()) {
            return null;
        }
        return discardPile.get(discardPile.size() - 1);
    }

    /**
     * Indica cuántas cartas quedan en la pila de robo.
     *
     * @return El número de cartas disponibles para robar.
     */
    public int remainingCards() {
        return drawPile.size();
    }

    /**
     * Recarga la pila de robo usando las cartas del descarte.
     *
     * <p>
     * Tomamos todas las cartas del descarte <em>excepto</em> la que está en la
     * cima (que debe quedarse como la carta activa de la mesa), las mezclamos y
     * las convertimos en la nueva pila de robo.
     *
     * <p>
     * Esta operación se llama automáticamente cuando la pila de robo se vacía.
     */
    private void reloadFromDiscard() {
        if (discardPile.size() <= 1) {
            return; // No hay suficientes para recargar
        }
        // Guardamos la carta de la cima (la carta activa)
        Card topCard = discardPile.remove(discardPile.size() - 1);

        // El resto del descarte pasa a ser la nueva pila de robo, mezclada
        drawPile = new ArrayList<>(discardPile);
        Collections.shuffle(drawPile);
        discardPile.clear();

        // Volvemos a poner la carta activa en el descarte
        discardPile.add(topCard);
    }
}
