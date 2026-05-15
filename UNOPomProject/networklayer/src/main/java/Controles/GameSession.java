package Controles;

import Dominio.Player;

/**
 * Guarda la información de la sesión del jugador local: su nombre, avatar, rol
 * (Host o Peer) y el código de sala al que pertenece. Es el punto de referencia
 * para que cualquier componente sepa quién es el jugador local sin necesitar
 * parámetros extra.
 * 
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameSession {

    /**
     * El jugador que representa a este usuario en la partida actual. Sus datos
     * (nombre, avatar, mano) son los de la persona frente a la pantalla.
     */
    private final Player localPlayer;

    /**
     * Indica si esta instancia del programa actúa como Host. true → abrimos un
     * ServerSocket y esperamos conexiones. false → nos conectamos al
     * ServerSocket del Host.
     */
    private final boolean host;

    /**
     * El código de la sala a la que pertenece esta sesión. El Host lo genera y
     * lo comparte. Los Peers lo usan para conectarse.
     */
    private String roomCode;

    private int maxJugadores;

    /**
     * Crea una nueva sesión para el jugador local.
     *
     * @param localPlayer El jugador registrado en esta instancia.
     * @param host {@code true} si este jugador creó la sala (es el Host).
     */
    public GameSession(Player localPlayer, boolean host) {
        this.localPlayer = localPlayer;
        this.host = host;
        this.roomCode = null; // se asigna cuando el lobby está listo
    }

    // ─────────────────────────────────────────────
    // Consultas de identidad
    // ─────────────────────────────────────────────
    /**
     * Devuelve el jugador local de esta sesión.
     *
     * @return El jugador que representa a este usuario.
     */
    public Player getLocalPlayer() {
        return localPlayer;
    }

    /**
     * Indica si esta instancia es el Host de la partida.
     *
     * @return {@code true} si es Host, {@code false} si es Peer.
     */
    public boolean isHost() {
        return host;
    }

    /**
     * Devuelve el código de la sala actual.
     *
     * @return El código de sala, o {@code null} si aún no está asignado.
     */
    public String getRoomCode() {
        return roomCode;
    }

    /**
     * Asigna el código de sala a esta sesión. El Host lo llama después de crear
     * el lobby. El Peer lo llama cuando se une exitosamente.
     *
     * @param roomCode El código de la sala.
     */
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    /**
     * Verifica si el jugador dado es el mismo que el jugador local de esta
     * sesión. Útil para que la GUI sepa si debe resaltar "tu turno" o mostrar
     * controles.
     *
     * @param playerName El nombre a comparar.
     * @return {@code true} si es el jugador local.
     */
    public boolean isLocalPlayer(String playerName) {
        return localPlayer.getName().equals(playerName);
    }

    public int getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(int maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

}
