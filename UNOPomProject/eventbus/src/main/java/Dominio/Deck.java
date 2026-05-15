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
 * <b>Nota importante:</b> En una partida nueva, hay que llamar a
 * {@link #reset(List)} pasando el mazo generado por
 * {@code CardFactory.createFullDeck()}. Esto permite reutilizar el Singleton
 * entre partidas sin reiniciar el programa, y desacopla el Deck de la
 * CardFactory para evitar dependencias circulares entre módulos.
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
    private static volatile Deck instance;

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
     * Si el mazo aún no existe, lo crea. Si ya existe, devuelve el mismo.
     *
     * @return La instancia única del mazo.
     */
    /**
     * Devuelve la única instancia del mazo (Singleton con doble-checked
     * locking). FIX: volatile + synchronized para evitar condición de carrera
     * entre hilos de red y el EDT de Swing.
     */
    public static Deck getInstance() {
        if (instance == null) {
            synchronized (Deck.class) {
                if (instance == null) {
                    instance = new Deck();
                }
            }
        }
        return instance;
    }

    /**
     * Reinicia el mazo con las cartas proporcionadas (ya mezcladas).
     *
     * <p>
     * Se llama al inicio de cada partida nueva pasando el resultado de
     * {@code CardFactory.createFullDeck()}. Al recibir las cartas como
     * parámetro, el Deck no necesita importar CardFactory y se evita una
     * dependencia circular entre los módulos {@code cardfactory} y
     * {@code eventbus}.
     *
     * @param cards Lista de cartas con las que inicializar el mazo (108 en una
     * partida estándar, ya mezcladas por CardFactory).
     */
    public void reset(List<Card> cards) {
        drawPile = new ArrayList<>(cards);
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
            return null;
        }
        return drawPile.remove(drawPile.size() - 1);
    }

    /**
     * Agrega una carta a la pila de descarte.
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
     * Toma todas las cartas del descarte EXCEPTO la que está en la cima (que es
     * la carta activa de la mesa), las mezcla y las convierte en la nueva pila
     * de robo.
     */
    private void reloadFromDiscard() {
        if (discardPile.size() <= 1) {
            return;
        }

        Card topCard = discardPile.remove(discardPile.size() - 1);

        drawPile = new ArrayList<>(discardPile);
        Collections.shuffle(drawPile);
        discardPile.clear();

        discardPile.add(topCard);
    }
}
