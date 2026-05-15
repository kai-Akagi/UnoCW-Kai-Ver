package Eventos;

/**
 * Evento: un jugador se desconectó.
 *
 * <p>La NetworkLayer lo genera cuando detecta que un socket se cerró.
 * El GameModel lo escucha para decidir si la partida puede continuar.
 * Si el Host se desconecta, la partida termina para todos.
 */
public class PlayerDisconnectedEvent extends GameEvent {

    /** El nombre del jugador que se desconectó. */
    private final String playerName;

    /**
     * @param playerName El nombre del jugador desconectado.
     */
    public PlayerDisconnectedEvent(String playerName) {
        super();
        this.playerName = playerName;
    }

    /** @return El nombre del jugador que se desconectó. */
    public String getPlayerName() { return playerName; }
}
