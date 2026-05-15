package Eventos;

/**
 * Evento de red: alguien gritó UNO (versión recibida por la red).
 *
 * <p>
 * Solo contiene el nombre del jugador porque los objetos {@link Dominio.Player}
 * completos no viajan por la red. El Controller usa el nombre para mostrar el
 * feedback visual adecuado.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class NetworkUnoCalledEvent extends GameEvent {

    /**
     * Nombre del jugador que gritó UNO.
     */
    private final String playerName;

    /**
     * Constructor para NetworkUnoCalledEvent.
     *
     * @param playerName El nombre del jugador que gritó UNO.
     */
    public NetworkUnoCalledEvent(String playerName) {
        super();
        this.playerName = playerName;
    }

    /**
     * Regresa el nombre del jugador que gritó UNO.
     *
     * @return El nombre del jugador que gritó UNO.
     */
    public String getPlayerName() {
        return playerName;
    }
}
