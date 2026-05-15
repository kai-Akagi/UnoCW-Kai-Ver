package Dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Contiene el estado completo del juego en un momento dado.
 *
 * <p>
 * Esta clase es el "tablero de verdad": guarda todo lo que describe la
 * situación actual de la partida: quiénes juegan, de quién es el turno, en qué
 * dirección va el juego, y cuántas cartas tiene cada uno.
 *
 * <p>
 * <b>¿Por qué existe esta clase separada?</b><br>
 * Los efectos de las cartas (patrón Strategy) necesitan acceso al estado del
 * juego para funcionar. En vez de pasarles 5 parámetros distintos, les pasamos
 * un solo objeto GameState que lo contiene todo. Esto se llama
 * <b>objeto de contexto</b> y mantiene los métodos limpios.
 *
 * <p>
 * Esta clase también es usada por el {@link GameModel} para mantener y publicar
 * el estado actual a través del EventBus.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameState {

    /**
     * La lista de jugadores en la partida, en el orden en que juegan. El índice
     * 0 es quien empieza.
     */
    private final List<Player> players;

    /**
     * El índice del jugador que tiene el turno actualmente. Se usa con la lista
     * {@code players} para saber quién juega ahora.
     */
    private int currentPlayerIndex;

    /**
     * La dirección actual del juego. true = gira en sentido normal
     * (0→1→2→3→0...). false = gira al revés (0→3→2→1→0...), efecto del REVERSE.
     */
    private boolean clockwise;

    /**
     * La carta que está encima de la pila de descarte (la carta "activa" de la
     * mesa). Es la carta contra la que se compara para saber si puedes jugar.
     */
    private Card topCard;

    /**
     * El color activo después de jugar un comodín. Cuando topCard es WILD, este
     * campo indica el color que el jugador eligió. Es null cuando la carta
     * activa tiene su propio color.
     */
    private Card.Color activeColor;

    /**
     * El mazo del juego. Lo necesitamos aquí para que los efectos como DrawTwo
     * o WildDrawFour puedan robar cartas del mazo.
     */
    private final Deck deck;

    /**
     * Construye el estado inicial del juego.
     *
     * @param players La lista de jugadores que participarán.
     * @param deck El mazo de cartas ya inicializado.
     */
    public GameState(List<Player> players, Deck deck) {
        this.players = new ArrayList<>(players);
        this.deck = deck;
        this.currentPlayerIndex = 0;
        this.clockwise = true;
        this.topCard = null;
    }

    // ─────────────────────────────────────────────
    // Consultas sobre el estado
    // ─────────────────────────────────────────────
    /**
     * Devuelve el jugador que tiene el turno en este momento.
     *
     * @return El jugador actual.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Devuelve el jugador que jugará después del actual, respetando la
     * dirección de juego.
     *
     * @return El siguiente jugador.
     */
    public Player getNextPlayer() {
        int nextIndex = nextIndex(1);
        return players.get(nextIndex);
    }

    /**
     * Devuelve la carta que está en la cima de la pila de descarte.
     *
     * @return La carta activa en la mesa.
     */
    public Card getTopCard() {
        return topCard;
    }

    /**
     * Devuelve el color activo actualmente. Si la carta activa es un comodín,
     * devuelve el color elegido por el jugador. Si no, devuelve el color de la
     * carta activa.
     *
     * @return El color que determina qué cartas se pueden jugar ahora.
     */
    public Card.Color getActiveColor() {
        if (activeColor != null) {
            return activeColor;
        }
        return topCard != null ? topCard.getColor() : Card.Color.WILD;
    }

    /**
     * Establece el color activo después de jugar un comodín.
     *
     * @param color El color elegido por el jugador.
     */
    public void setActiveColor(Card.Color color) {
        this.activeColor = color;
    }

    /**
     * Devuelve el mazo del juego.
     *
     * @return El mazo.
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Devuelve todos los jugadores de la partida.
     *
     * @return Lista de jugadores (en orden de turno).
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Indica si el juego avanza en sentido normal (izquierda a derecha).
     *
     * @return {@code true} si va en sentido normal, {@code false} si está
     * invertido.
     */
    public boolean isClockwise() {
        return clockwise;
    }

    // ─────────────────────────────────────────────
    // Modificaciones del estado
    // ─────────────────────────────────────────────
    /**
     * Cambia la carta activa en la mesa (la que está encima del descarte). Si
     * la nueva carta tiene su propio color (no es comodín), limpia el color
     * activo especial para que no persista del turno anterior.
     *
     * @param card La nueva carta que queda en la cima.
     */
    public void setTopCard(Card card) {
        this.topCard = card;
        if (card.getColor() != Card.Color.WILD) {
            this.activeColor = null;
        }
    }

    /**
     * Avanza el turno al siguiente jugador, respetando la dirección de juego.
     * Después de llamar esto, {@link #getCurrentPlayer()} devolverá el
     * siguiente.
     */
    public void advanceTurn() {
        currentPlayerIndex = nextIndex(1);
    }

    /**
     * Salta al jugador siguiente (dos posiciones en vez de una). Esto lo usa el
     * efecto SKIP.
     */
    public void skipNextPlayer() {
        currentPlayerIndex = nextIndex(2);
    }

    /**
     * Invierte la dirección del juego. Si iba en sentido normal, pasa a ir al
     * revés, y viceversa. Esto lo usa el efecto REVERSE.
     */
    public void flipDirection() {
        clockwise = !clockwise;
    }

    public void reverseDirection() {
        flipDirection();
        advanceTurn();
    }

    /**
     * Le da una cantidad de cartas del mazo a un jugador específico. Esto lo
     * usan los efectos DRAW_TWO y WILD_DRAW_FOUR.
     *
     * @param player El jugador que debe robar cartas.
     * @param quantity Cuántas cartas debe robar.
     */
    public void giveCards(Player player, int quantity) {
        for (int i = 0; i < quantity; i++) {
            Card drawn = deck.drawCard();
            if (drawn != null) {
                player.addCard(drawn);
                // Guardar la carta para que GameModel la notifique vía evento.
                // No publicamos aquí porque GameState no tiene referencia al EventBus;
                // las cartas se exponen via getLastGivenCards() para que GameModel publique.
                lastGivenCards.add(drawn);
            }
        }
        lastGivenPlayer = player;
    }

    /**
     * Las cartas dadas en el último giveCards(), para publicar como eventos
     * privados.
     */
    private final java.util.List<Card> lastGivenCards = new java.util.ArrayList<>();
    private Player lastGivenPlayer = null;

    /**
     * Devuelve las cartas repartidas por el último giveCards() y limpia el
     * buffer. GameModel llama este método después de aplicar efectos para
     * publicar CardDrawnPrivateEvent por cada carta, notificando al Peer
     * correcto.
     */
    public java.util.List<Card> drainLastGivenCards() {
        java.util.List<Card> result = new java.util.ArrayList<>(lastGivenCards);
        lastGivenCards.clear();
        return result;
    }

    /**
     * Devuelve el jugador que recibió las cartas en el último giveCards().
     */
    public Player getLastGivenPlayer() {
        return lastGivenPlayer;
    }

    /**
     * Ajusta el índice del jugador actual para que no quede fuera de rango. Se
     * llama después de remover un jugador de la lista durante la partida.
     */
    public void clampCurrentIndex() {
        if (players.isEmpty()) {
            return;
        }
        currentPlayerIndex = currentPlayerIndex % players.size();
    }

    // ─────────────────────────────────────────────
    // Ayudas internas
    // ─────────────────────────────────────────────
    /**
     * Calcula el índice del jugador que está {@code steps} posiciones adelante,
     * respetando la dirección actual y el total de jugadores (bucle circular).
     *
     * <p>
     * Ejemplo con 4 jugadores en sentido inverso, turno actual = 0, steps = 1:
     * resultado = (0 - 1 + 4) % 4 = 3. Es decir, el jugador 3 va después.
     *
     * @param steps Cuántos pasos hacia adelante queremos contar.
     * @return El índice del jugador en esa posición.
     */
    private int nextIndex(int steps) {
        int total = players.size();
        if (clockwise) {
            return (currentPlayerIndex + steps) % total;
        } else {
            return (currentPlayerIndex - steps + total) % total;
        }
    }

}
