package uno.events;

import uno.model.Player;

/**
 * Evento público: un jugador robó una carta del mazo.
 *
 * <p>Este evento llega a <b>todos</b> los jugadores. Solo informa
 * quién robó y cuántas cartas tiene ahora. La carta real no se incluye
 * porque los demás jugadores no deben saber qué carta fue.
 *
 * <p>El jugador que robó recibe adicionalmente un {@link CardDrawnPrivateEvent}
 * con la carta real.
 */
public class CardDrawnPublicEvent extends GameEvent {

    /** El jugador que robó. */
    private final Player player;

    /**
     * @param player El jugador que robó una carta del mazo.
     */
    public CardDrawnPublicEvent(Player player) {
        super();
        this.player = player;
    }

    /** @return El jugador que robó. */
    public Player getPlayer() { return player; }
}
