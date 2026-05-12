package Eventos;

import Eventos.GameEvent;

/**
 * Evento: un jugador cambió su estado de "Listo" en el lobby.
 *
 * <p>Se publica cuando el jugador presiona "Estoy listo" o "Cancelar listo".
 * El Host lo escucha para saber si puede habilitar "Iniciar partida".
 * Todos los peers lo ven para actualizar el indicador visual en la lista.
 */
public class PlayerReadyEvent extends GameEvent {

    /** Nombre del jugador que cambió su estado. */
    private final String playerName;

    /** true = listo, false = no listo. */
    private final boolean ready;

    /**
     * @param playerName El nombre del jugador.
     * @param ready      {@code true} si se marcó como listo.
     */
    public PlayerReadyEvent(String playerName, boolean ready) {
        super();
        this.playerName = playerName;
        this.ready      = ready;
    }

    /** @return El nombre del jugador que cambió su estado. */
    public String getPlayerName() { return playerName; }

    /** @return {@code true} si el jugador está listo. */
    public boolean isReady() { return ready; }
}
