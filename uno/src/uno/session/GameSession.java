package uno.session;

import uno.model.Player;

/**
 * Contexto de la sesión actual del jugador en esta instancia del programa.
 *
 * <p>Responde a las preguntas más básicas de cualquier componente:
 * <ul>
 *   <li>¿Quién soy yo en esta partida?</li>
 *   <li>¿Soy el Host o un Peer?</li>
 *   <li>¿A qué sala pertenezco?</li>
 * </ul>
 *
 * <p><b>¿Por qué existe esta clase?</b><br>
 * Sin ella, cada componente (Controller, NetworkLayer, GameModel) tendría que
 * recibir el jugador local como parámetro en cada método, o peor, guardarlo
 * como atributo propio. Con {@code GameSession} hay un solo lugar donde
 * vive esa información y todos la consultan ahí.
 *
 * <p><b>Ciclo de vida:</b> Se crea cuando el jugador completa el registro
 * (nombre + avatar) y vive hasta que cierra la aplicación o vuelve al menú
 * principal para registrarse de nuevo.
 *
 * <p><b>¿Es un Singleton?</b><br>
 * No formalmente, pero en la práctica solo existe una instancia por ejecución.
 * Se crea en el punto de entrada (la pantalla de registro) y se pasa por
 * referencia a quien la necesite. Evitamos el Singleton aquí porque la sesión
 * sí puede "reiniciarse" si el jugador vuelve al menú principal, y un Singleton
 * complicaría ese reset.
 */
public class GameSession {

    /**
     * El jugador que representa a este usuario en la partida actual.
     * Sus datos (nombre, avatar, mano) son los de la persona frente a la pantalla.
     */
    private final Player localPlayer;

    /**
     * Indica si esta instancia del programa actúa como Host.
     * true  → abrimos un ServerSocket y esperamos conexiones.
     * false → nos conectamos al ServerSocket del Host.
     */
    private final boolean host;

    /**
     * El código de la sala a la que pertenece esta sesión.
     * El Host lo genera y lo comparte. Los Peers lo usan para conectarse.
     */
    private String roomCode;

    /**
     * Crea una nueva sesión para el jugador local.
     *
     * @param localPlayer El jugador registrado en esta instancia.
     * @param host        {@code true} si este jugador creó la sala (es el Host).
     */
    public GameSession(Player localPlayer, boolean host) {
        this.localPlayer = localPlayer;
        this.host        = host;
        this.roomCode    = null; // se asigna cuando el lobby está listo
    }

    // ─────────────────────────────────────────────
    // Consultas de identidad
    // ─────────────────────────────────────────────

    /**
     * Devuelve el jugador local de esta sesión.
     *
     * @return El jugador que representa a este usuario.
     */
    public Player getLocalPlayer() { return localPlayer; }

    /**
     * Indica si esta instancia es el Host de la partida.
     *
     * @return {@code true} si es Host, {@code false} si es Peer.
     */
    public boolean isHost() { return host; }

    /**
     * Devuelve el código de la sala actual.
     *
     * @return El código de sala, o {@code null} si aún no está asignado.
     */
    public String getRoomCode() { return roomCode; }

    /**
     * Asigna el código de sala a esta sesión.
     * El Host lo llama después de crear el lobby.
     * El Peer lo llama cuando se une exitosamente.
     *
     * @param roomCode El código de la sala.
     */
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    /**
     * Verifica si el jugador dado es el mismo que el jugador local de esta sesión.
     * Útil para que la GUI sepa si debe resaltar "tu turno" o mostrar controles.
     *
     * @param playerName El nombre a comparar.
     * @return {@code true} si es el jugador local.
     */
    public boolean isLocalPlayer(String playerName) {
        return localPlayer.getName().equals(playerName);
    }
}
