package uno.events;

/**
 * Evento de red: cambio de turno recibido desde el Host.
 *
 * <p>Versión "liviana" de {@link TurnChangedEvent} que viaja por la red.
 * En vez de llevar objetos {@code Player} y {@code Card} completos
 * (que requieren serialización compleja), lleva solo los datos
 * mínimos como texto. El Controller los usa para actualizar la vista.
 */
public class NetworkTurnChangedEvent extends GameEvent {

    /** Nombre del jugador que ahora tiene el turno. */
    private final String currentPlayerName;

    /** Representación en texto de la carta activa (ej. "RED-7", "SKIP"). */
    private final String topCardText;

    /**
     * @param currentPlayerName El nombre del jugador activo.
     * @param topCardText       La carta activa como texto.
     */
    public NetworkTurnChangedEvent(String currentPlayerName, String topCardText) {
        super();
        this.currentPlayerName = currentPlayerName;
        this.topCardText       = topCardText;
    }

    /** @return El nombre del jugador activo. */
    public String getCurrentPlayerName() { return currentPlayerName; }

    /** @return La carta activa como texto. */
    public String getTopCardText() { return topCardText; }
}
