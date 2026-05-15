package Eventos;

import Eventos.GameEvent;

/**
 * Evento de red: carta robada recibida de forma privada.
 *
 * <p>
 * El Host envía esto solo al peer que robó la carta. Contiene la carta real
 * para que el peer actualice su mano local. Los demás peers reciben
 * {@link CardDrawnPublicEvent} sin la carta.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class NetworkCardDrawnPrivateEvent extends GameEvent {

    /**
     * Nombre del jugador que robó.
     */
    private final String playerName;

    /**
     * Representación en texto de la carta robada (ej. "BLUE-3").
     */
    private final String cardText;

    /**
     * @param playerName El nombre del jugador que robó.
     * @param cardText La carta robada como texto.
     */
    public NetworkCardDrawnPrivateEvent(String playerName, String cardText) {
        super();
        this.playerName = playerName;
        this.cardText = cardText;
    }

    /**
     * @return El nombre del jugador que robó.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return La carta robada como texto.
     */
    public String getCardText() {
        return cardText;
    }
}
