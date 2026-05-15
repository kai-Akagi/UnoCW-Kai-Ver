package Eventos;

import Dominio.Card;
import Dominio.Player;

/**
 * Evento privado: la carta que robó un jugador.
 *
 * <p>
 * Este evento solo llega al jugador que robó la carta. Contiene la carta real
 * para que su GameModel local la agregue a su mano y su GUI la muestre.
 *
 * <p>
 * La audiencia de este evento es {@code PRIVATE}, así que la
 * {@code NetworkLayer} del Host lo envía únicamente al peer destino, no a
 * todos.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class CardDrawnPrivateEvent extends GameEvent {

    /**
     * El jugador que robó.
     */
    private final Player player;

    /**
     * La carta que fue robada.
     */
    private final Card card;

    /**
     * Constructor de CardDrawnPrivateEvent
     *
     * @param player El jugador que robó.
     * @param card La carta que tomó del mazo.
     */
    public CardDrawnPrivateEvent(Player player, Card card) {
        super();
        this.player = player;
        this.card = card;
    }

    /**
     * Regresa el jugador que robó.
     *
     * @return El jugador que robó.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Regresa la carta robada.
     *
     * @return La carta robada.
     */
    public Card getCard() {
        return card;
    }
}
