package Eventos;

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

    /** Dirección del juego recibida del Host. */
    private final boolean clockwise;

    /**
     * Conteo exacto de cartas por jugador enviado por el Host.
     * Es null cuando el mensaje no incluye esta información (ej. CARD_REJECTED).
     */
    private final java.util.Map<String, Integer> handSizes;

    /**
     * Color activo elegido tras un comodín. Null si la carta activa tiene su propio color.
     * Se usa para mostrar el color correcto en la mesa cuando el topCard es WILD.
     */
    private final Dominio.Card.Color activeColor;

    public NetworkTurnChangedEvent(String currentPlayerName, String topCardText,
                                   boolean clockwise, java.util.Map<String, Integer> handSizes,
                                   Dominio.Card.Color activeColor) {
        super();
        this.currentPlayerName = currentPlayerName;
        this.topCardText       = topCardText;
        this.clockwise         = clockwise;
        this.handSizes         = handSizes;
        this.activeColor       = activeColor;
    }

    public String getCurrentPlayerName() { return currentPlayerName; }
    public String getTopCardText()       { return topCardText; }
    public boolean isClockwise()         { return clockwise; }
    public java.util.Map<String, Integer> getHandSizes() { return handSizes; }
    public Dominio.Card.Color getActiveColor()         { return activeColor; }
}
