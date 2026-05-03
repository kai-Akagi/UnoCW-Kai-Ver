package uno.events;

import uno.model.Card;
import uno.model.Player;

/**
 * Evento: un jugador gritó "¡UNO!".
 *
 * <p>Se publica cuando el jugador presiona el botón UNO al quedarse
 * con una carta. Si no lo presiona a tiempo, el GameModel le aplica
 * la penalización de 2 cartas.
 */
public class UnoCalledEvent extends GameEvent {

    private final Player player;

    /** @param player El jugador que gritó UNO. */
    public UnoCalledEvent(Player player) {
        super();
        this.player = player;
    }

    /** @return El jugador que gritó UNO. */
    public Player getPlayer() { return player; }
}
