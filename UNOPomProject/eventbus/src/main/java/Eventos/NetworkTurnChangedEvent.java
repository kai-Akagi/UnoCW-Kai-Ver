package Eventos;

/**
 * Evento de red: cambio de turno recibido desde el Host.
 *
 * <p>
 * Versión "liviana" de {@link TurnChangedEvent} que viaja por la red. En vez de
 * llevar objetos {@code Player} y {@code Card} completos (que requieren
 * serialización compleja), lleva solo los datos mínimos como texto. El
 * Controller los usa para actualizar la vista.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class NetworkTurnChangedEvent extends GameEvent {

    /**
     * Nombre del jugador que ahora tiene el turno.
     */
    private final String currentPlayerName;

    /**
     * Representación en texto de la carta activa (ej. "RED-7", "SKIP").
     */
    private final String topCardText;

    /**
     * Dirección del juego recibida del Host.
     */
    private final boolean clockwise;

    /**
     * Conteo exacto de cartas por jugador enviado por el Host. Es null cuando
     * el mensaje no incluye esta información (ej. CARD_REJECTED).
     */
    private final java.util.Map<String, Integer> handSizes;

    /**
     * Color activo elegido tras un comodín. Null si la carta activa tiene su
     * propio color. Se usa para mostrar el color correcto en la mesa cuando el
     * topCard es WILD.
     */
    private final Dominio.Card.Color activeColor;

    /**
     * Constructor para NetworkTurnChangedEvent.
     *
     * @param currentPlayerName Nombre del jugador que tiene el turno actual.
     * @param topCardText Carta activa representada como texto.
     * @param clockwise Indica si la dirección del juego es horaria.
     * @param handSizes Cantidad de cartas de cada jugador.
     * @param activeColor Color activo actual del juego.
     */
    public NetworkTurnChangedEvent(String currentPlayerName, String topCardText,
            boolean clockwise, java.util.Map<String, Integer> handSizes,
            Dominio.Card.Color activeColor) {
        super();
        this.currentPlayerName = currentPlayerName;
        this.topCardText = topCardText;
        this.clockwise = clockwise;
        this.handSizes = handSizes;
        this.activeColor = activeColor;
    }

    /**
     * Regresa el nombre del jugador actual.
     *
     * @return Nombre del jugador que tiene el turno.
     */
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    /**
     * Regresa la carta activa como texto.
     *
     * @return Representación textual de la carta activa.
     */
    public String getTopCardText() {
        return topCardText;
    }

    /**
     * Indica si la dirección del juego es horaria.
     *
     * @return true si el juego avanza en sentido horario.
     */
    public boolean isClockwise() {
        return clockwise;
    }

    /**
     * Regresa la cantidad de cartas de cada jugador.
     *
     * @return Mapa con los nombres de jugadores y sus cantidades de cartas.
     */
    public java.util.Map<String, Integer> getHandSizes() {
        return handSizes;
    }

    /**
     * Regresa el color activo actual.
     *
     * @return Color activo del juego.
     */
    public Dominio.Card.Color getActiveColor() {
        return activeColor;
    }
}
