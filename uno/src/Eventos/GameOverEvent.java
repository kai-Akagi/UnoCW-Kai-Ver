package Eventos;

import Eventos.GameEvent;
import Dominio.Player;

/**
 * Evento: la partida terminó.
 *
 * <p>El GameModel lo publica cuando detecta que un jugador se quedó
 * sin cartas. La GUI lo escucha para navegar al scoreboard.
 */
public class GameOverEvent extends GameEvent {

    private final Player winner;

    /** @param winner El jugador que ganó. */
    public GameOverEvent(Player winner) {
        super();
        this.winner = winner;
    }

    /** @return El jugador ganador. */
    public Player getWinner() { return winner; }
}
