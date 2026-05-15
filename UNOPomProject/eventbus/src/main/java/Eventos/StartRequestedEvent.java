package Eventos;

/**
 * Evento: un Peer solicitó al Host iniciar la partida antes de completar el
 * cupo.
 *
 * <p>
 * Solo los Peers pueden enviarlo. El LobbyController del Host decide si acepta
 * o rechaza mostrando una notificación.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class StartRequestedEvent extends GameEvent {

    /**
     * Nombre del jugador que solicitó el inicio.
     */
    private final String requesterName;

    /**
     * Constructor para StartRequestedEvent.
     * 
     * @param requesterName El nombre del peer que solicita iniciar.
     */
    public StartRequestedEvent(String requesterName) {
        super();
        this.requesterName = requesterName;
    }

    /**
     * Regresa el nombre del jugador que hizo la solicitud.
     * 
     * @return El nombre del jugador que hizo la solicitud.
     */
    public String getRequesterName() {
        return requesterName;
    }
}
