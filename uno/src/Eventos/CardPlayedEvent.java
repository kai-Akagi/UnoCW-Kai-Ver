package Eventos;

import Eventos.GameEvent;
import Dominio.Card;
import Dominio.Player;

/**
 * Evento: un jugador jugó una carta.
 *
 * <p>Es el evento más importante del juego. Lo escuchan:
 * <ul>
 *   <li>El {@code GameModel} del Host: para validar y actualizar el estado.</li>
 *   <li>La {@code NetworkLayer} del Host: para hacer broadcast a los peers.</li>
 *   <li>La GUI de cada jugador: para actualizar la pila de descarte.</li>
 * </ul>
 */
public class CardPlayedEvent extends GameEvent {

    /** El jugador que jugó la carta. */
    private final Player player;

    /** La carta que fue jugada. */
    private final Card card;

    /**
     * @param player El jugador que realizó la jugada.
     * @param card   La carta que jugó.
     */
    public CardPlayedEvent(Player player, Card card) {
        super();
        this.player = player;
        this.card   = card;
    }

    /** @return El jugador que jugó la carta. */
    public Player getPlayer() { return player; }

    /** @return La carta que fue jugada. */
    public Card getCard() { return card; }
}
