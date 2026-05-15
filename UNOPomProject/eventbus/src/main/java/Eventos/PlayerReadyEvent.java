package Eventos;

/**
 * Evento: un jugador cambió su estado de "Listo" en el lobby.
 *
 * <p>
 * Se publica cuando el jugador presiona "Estoy listo" o "Cancelar listo". El
 * Host lo escucha para saber si puede habilitar "Iniciar partida". Todos los
 * peers lo ven para actualizar el indicador visual en la lista.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class PlayerReadyEvent extends GameEvent {

    /**
     * Nombre del jugador que cambió su estado.
     */
    private final String playerName;

    /**
     * true = listo, false = no listo.
     */
    private final boolean ready;

    /**
     * Constructor para PlayerReadyEvent.
     *
     * @param playerName El nombre del jugador.
     * @param ready {@code true} si se marcó como listo.
     */
    public PlayerReadyEvent(String playerName, boolean ready) {
        super();
        this.playerName = playerName;
        this.ready = ready;
    }

    /**
     * Regresa el nombre del jugador que cambió su estado.
     *
     * @return El nombre del jugador que cambió su estado.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Regresa verdadero si el jugador está listo.
     *
     * @return {@code true} si el jugador está listo.
     */
    public boolean isReady() {
        return ready;
    }
}
