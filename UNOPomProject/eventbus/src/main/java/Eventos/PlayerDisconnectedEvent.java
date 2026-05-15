package Eventos;

/**
 * Evento: un jugador se desconectó.
 *
 * <p>
 * La NetworkLayer lo genera cuando detecta que un socket se cerró. El GameModel
 * lo escucha para decidir si la partida puede continuar. Si el Host se
 * desconecta, la partida termina para todos.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class PlayerDisconnectedEvent extends GameEvent {

    /**
     * El nombre del jugador que se desconectó.
     */
    private final String playerName;

    /**
     * Constructor para PlayerDisconnectedEvent.
     *
     * @param playerName El nombre del jugador desconectado.
     */
    public PlayerDisconnectedEvent(String playerName) {
        super();
        this.playerName = playerName;
    }

    /**
     * Regresa el nombre del jugador que se desconectó.
     *
     * @return El nombre del jugador que se desconectó.
     */
    public String getPlayerName() {
        return playerName;
    }
}
