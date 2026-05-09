package uno.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Guarda el estado de la sala de espera: jugadores conectados,
 * capacidad máxima, código de sala y si la partida ya inició.
 * Se crea antes del juego y se descarta cuando la partida comienza.
 */
public class LobbyState {

    /**
     * Código único de la sala. El Host lo comparte con los demás jugadores
     * para que puedan unirse. Se genera automáticamente al crear la sala.
     * Formato: "XXXX-XXXX" con letras y números mayúsculas.
     */
    private final String roomCode;

    /**
     * Número máximo de jugadores que puede tener esta sala.
     * Solo el Host puede definirlo (2, 3 o 4).
     */
    private int capacity;

    /**
     * Lista de jugadores actualmente conectados en el lobby.
     * El primer jugador siempre es el Host.
     */
    private final List<Player> connectedPlayers;

    /**
     * Indica si la partida ya fue iniciada.
     * Una vez que es true, no se permiten más conexiones a la sala.
     */
    private boolean gameStarted;

    /**
     * Crea un nuevo estado de lobby con capacidad inicial de 4 jugadores.
     * El Host puede cambiar la capacidad antes de que otros se unan.
     */
    public LobbyState() {
        this.roomCode         = generateRoomCode();
        this.capacity         = 4;
        this.connectedPlayers = new ArrayList<>();
        this.gameStarted      = false;
    }
    
    //contructor con el parametro del num max de jugadores
    public LobbyState(int capacity) {
        this.roomCode         = generateRoomCode();
        this.capacity         = capacity;
        this.connectedPlayers = new ArrayList<>();
        this.gameStarted      = false;
    }

    // ─────────────────────────────────────────────
    // Gestión de jugadores
    // ─────────────────────────────────────────────

    /**
     * Agrega un jugador a la sala si hay espacio y la partida no ha iniciado.
     *
     * @param player El jugador que quiere unirse.
     * @return {@code true} si se unió exitosamente, {@code false} si la sala
     *         está llena o la partida ya comenzó.
     */
    public boolean addPlayer(Player player) {
        if (gameStarted || connectedPlayers.size() >= capacity) {
            return false;
        }
        connectedPlayers.add(player);
        return true;
    }

    /**
     * Elimina un jugador de la sala.
     * Se usa cuando un jugador abandona o se desconecta.
     *
     * @param playerName El nombre del jugador que se va.
     */
    public void removePlayer(String playerName) {
        connectedPlayers.removeIf(p -> p.getName().equals(playerName));
    }

    // ─────────────────────────────────────────────
    // Validaciones de unicidad
    // ─────────────────────────────────────────────

    /**
     * Verifica si un nombre ya está siendo usado por algún jugador conectado.
     *
     * @param name El nombre a verificar.
     * @return {@code true} si el nombre ya está en uso.
     */
    public boolean isNameTaken(String name) {
        return connectedPlayers.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }

    /**
     * Verifica si un avatar ya está siendo usado por algún jugador conectado.
     *
     * @param avatarId El identificador del avatar a verificar.
     * @return {@code true} si el avatar ya está en uso.
     */
    public boolean isAvatarTaken(String avatarId) {
        return connectedPlayers.stream()
                .anyMatch(p -> p.getAvatarId().equals(avatarId));
    }

    // ─────────────────────────────────────────────
    // Condiciones para iniciar la partida
    // ─────────────────────────────────────────────

    /**
     * Verifica si todos los jugadores conectados marcaron "Listo".
     *
     * @return {@code true} si todos están listos.
     */
    public boolean allPlayersReady() {
        if (connectedPlayers.size() < 2) return false;
        return connectedPlayers.stream().allMatch(Player::isReady);
    }

    /**
     * Verifica si la sala tiene el mínimo de jugadores para iniciar.
     * En UNO se necesitan al menos 2 jugadores.
     *
     * @return {@code true} si hay 2 o más jugadores conectados.
     */
    public boolean hasMinimumPlayers() {
        return connectedPlayers.size() >= 2;
    }

    /**
     * Verifica si se puede iniciar la partida.
     * Condiciones: mínimo 2 jugadores y todos en estado "Listo".
     *
     * @return {@code true} si se puede iniciar.
     */
    public boolean canStart() {
        return hasMinimumPlayers() && allPlayersReady();
    }

    /**
     * Verifica si la sala aún tiene espacio para más jugadores.
     *
     * @return {@code true} si hay espacio disponible.
     */
    public boolean hasSpace() {
        return !gameStarted && connectedPlayers.size() < capacity;
    }

    // ─────────────────────────────────────────────
    // Getters y setters
    // ─────────────────────────────────────────────

    /** @return El código único de esta sala. */
    public String getRoomCode() { return roomCode; }

    /** @return La capacidad máxima de jugadores. */
    public int getCapacity() { return capacity; }

    /**
     * Cambia la capacidad de la sala.
     * Solo el Host puede hacer esto, y solo antes de que inicien.
     *
     * @param capacity El nuevo número máximo (2, 3 o 4).
     */
    public void setCapacity(int capacity) {
        if (capacity >= 2 && capacity <= 4) {
            this.capacity = capacity;
        }
    }

    /** @return Lista de jugadores conectados (solo lectura). */
    public List<Player> getConnectedPlayers() {
        return Collections.unmodifiableList(connectedPlayers);
    }

    /** @return Cuántos jugadores están conectados actualmente. */
    public int getPlayerCount() { return connectedPlayers.size(); }

    /** @return {@code true} si la partida ya inició. */
    public boolean isGameStarted() { return gameStarted; }

    /** Marca la partida como iniciada. No se pueden unir más jugadores. */
    public void markGameStarted() { this.gameStarted = true; }

    // ─────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────

    /**
     * Genera un código de sala único y legible.
     * Formato: "A3F2-9KX1" — dos grupos de 4 caracteres separados por guión.
     *
     * <p>Usamos UUID como base para garantizar unicidad, luego tomamos
     * solo los primeros 8 caracteres en mayúsculas para que sea fácil
     * de escribir y compartir entre jugadores.
     *
     * @return El código generado.
     */
    private String generateRoomCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, 4) + "-" + uuid.substring(4, 8);
    }
}
