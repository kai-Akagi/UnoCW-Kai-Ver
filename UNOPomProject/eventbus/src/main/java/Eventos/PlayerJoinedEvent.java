package Eventos;

import Eventos.GameEvent;
import Dominio.Player;

/**
 * Evento: un jugador se unió al lobby.
 *
 * <p>
 * La red lo genera cuando recibe la conexión de un nuevo peer. El
 * LobbyController lo escucha para agregar al jugador a la lista visual.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class PlayerJoinedEvent extends GameEvent {

    private final Player player;

    /**
     * @param player El jugador que se unió.
     */
    public PlayerJoinedEvent(Player player) {
        super();
        this.player = player;
    }

    /**
     * @return El jugador que se unió.
     */
    public Player getPlayer() {
        return player;
    }
}
