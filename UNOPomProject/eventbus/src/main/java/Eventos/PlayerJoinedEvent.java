package Eventos;

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
     * Constructor para PlayerJoinedEvent.
     *
     * @param player El jugador que se unió.
     */
    public PlayerJoinedEvent(Player player) {
        super();
        this.player = player;
    }

    /**
     * Regresa el jugador que se unió.
     *
     * @return El jugador que se unió.
     */
    public Player getPlayer() {
        return player;
    }
}
