package Eventos;

/**
 * Evento: el lobby fue cerrado por el Host.
 *
 * <p>Se publica cuando el Host abandona antes de iniciar la partida.
 * Todos los peers conectados lo reciben y son redirigidos al menú principal.
 */
public class LobbyClosedEvent extends GameEvent {

    /** La razón del cierre (ej. "El Host abandonó"). */
    private final String reason;

    /** @param reason La razón del cierre. */
    public LobbyClosedEvent(String reason) {
        super();
        this.reason = reason;
    }

    /** @return La razón del cierre. */
    public String getReason() { return reason; }
}
