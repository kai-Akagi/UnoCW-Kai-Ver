package Eventos;

/**
 * Evento: un Peer solicitó al Host iniciar la partida antes de completar el cupo.
 *
 * <p>Solo los Peers pueden enviarlo. El LobbyController del Host decide
 * si acepta o rechaza mostrando una notificación.
 */
public class StartRequestedEvent extends GameEvent {

    /** Nombre del jugador que solicitó el inicio. */
    private final String requesterName;

    /** @param requesterName El nombre del peer que solicita iniciar. */
    public StartRequestedEvent(String requesterName) {
        super();
        this.requesterName = requesterName;
    }

    /** @return El nombre del jugador que hizo la solicitud. */
    public String getRequesterName() { return requesterName; }
}
