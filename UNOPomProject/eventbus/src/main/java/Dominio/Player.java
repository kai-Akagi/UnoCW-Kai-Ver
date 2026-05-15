package Dominio;

import Dominio.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a un jugador en la partida de UNO.
 *
 * <p>
 * Un jugador tiene:
 * <ul>
 * <li>Un <b>nombre</b> único que lo identifica en la partida.</li>
 * <li>Un <b>avatar</b> (identificador de imagen para la GUI).</li>
 * <li>Una <b>mano</b>: la lista de cartas que tiene actualmente.</li>
 * <li>Una bandera que indica si es el <b>Host</b> de la partida.</li>
 * <li>Su <b>estado</b>: si está listo para empezar o no (en el lobby).</li>
 * </ul>
 *
 * <p>
 * <b>Principio de responsabilidad única:</b> Esta clase solo sabe sobre el
 * jugador y sus cartas. No sabe nada de la red, de la GUI, ni de las reglas. Si
 * necesitamos cambiar cómo se muestra un jugador en pantalla, no tocamos aquí.
 * Si cambia el protocolo de red, tampoco tocamos aquí. Cada cosa en su lugar.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class Player {

    /**
     * El nombre único del jugador.
     */
    private final String name;

    /**
     * Identificador del avatar elegido. Corresponde al nombre del archivo de
     * imagen en la carpeta de recursos. Ejemplo: "avatar_1", "avatar_2", etc.
     */
    private final String avatarId;

    /**
     * Indica si este jugador es el Host (anfitrión) de la partida. El Host
     * tiene privilegios adicionales (iniciar partida, cerrar sala).
     */
    private final boolean isHost;

    /**
     * La mano del jugador: las cartas que tiene en su poder en este momento.
     * Empieza vacía y se llena al repartir cartas al inicio del juego.
     */
    private final List<Card> hand;

    /**
     * Indica si el jugador marcó que está listo en el lobby de espera. La
     * partida no puede comenzar hasta que todos estén listos.
     */
    private boolean ready;

    /**
     * Construye un jugador nuevo.
     *
     * @param name El nombre único del jugador.
     * @param avatarId El identificador del avatar seleccionado.
     * @param isHost {@code true} si este jugador es el Host de la partida.
     */
    public Player(String name, String avatarId, boolean isHost) {
        this.name = name;
        this.avatarId = avatarId;
        this.isHost = isHost;
        this.hand = new ArrayList<>();
        this.ready = false;
    }

    // ─────────────────────────────────────────────
    // Información del jugador
    // ─────────────────────────────────────────────
    /**
     * @return El nombre del jugador.
     */
    public String getName() {
        return name;
    }

    /**
     * @return El identificador del avatar.
     */
    public String getAvatarId() {
        return avatarId;
    }

    /**
     * @return {@code true} si este jugador es el Host.
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * @return {@code true} si el jugador marcó "Listo" en el lobby.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Cambia el estado de "Listo" del jugador.
     *
     * @param ready {@code true} para marcar como listo, {@code false} para
     * desmarcar.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    // ─────────────────────────────────────────────
    // Manejo de cartas en mano
    // ─────────────────────────────────────────────
    /**
     * Agrega una carta a la mano del jugador. Se usa cuando el jugador roba una
     * carta del mazo.
     *
     * @param card La carta que se agrega a la mano.
     */
    public void addCard(Card card) {
        hand.add(card);
    }

    /**
     * Quita una carta específica de la mano del jugador y la devuelve. Se usa
     * cuando el jugador decide jugar una carta.
     *
     * @param card La carta que el jugador quiere jugar.
     * @return {@code true} si la carta estaba en la mano y fue quitada,
     * {@code false} si la carta no estaba en la mano.
     */
    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    /**
     * Devuelve una vista de solo lectura de la mano del jugador. Usamos una
     * copia para que nadie pueda modificar la mano desde afuera.
     *
     * <p>
     * <b>Buena práctica:</b> Devolver una copia inmutable protege el estado
     * interno del objeto. Esto se llama "encapsulación" y evita errores
     * difíciles de rastrear donde alguien modifica la lista desde otro lugar.
     *
     * @return Lista de cartas en la mano (copia, no modificable directamente).
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Dice cuántas cartas tiene este jugador en la mano.
     *
     * @return El número de cartas en mano.
     */
    public int getHandSize() {
        return hand.size();
    }

    /**
     * Verifica si el jugador puede jugar alguna carta de su mano sobre la carta
     * que está actualmente en la mesa.
     *
     * @param topCard La carta activa en la mesa.
     * @return {@code true} si al menos una carta de la mano se puede jugar.
     */
    public boolean hasPlayableCard(Card topCard) {
        for (Card card : hand) {
            if (card.canBePlacedOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si este jugador ha ganado la partida. En UNO, un jugador gana
     * cuando se queda sin cartas.
     *
     * @return {@code true} si la mano está vacía (el jugador ganó).
     */
    public boolean hasWon() {
        return hand.isEmpty();
    }

    /**
     * Verifica si el jugador tiene exactamente una carta en la mano. En ese
     * caso, debería gritar "UNO".
     *
     * @return {@code true} si solo le queda una carta.
     */
    public boolean hasUno() {
        return hand.size() == 1;
    }

    /**
     * Vacía la mano del jugador. Se usa al reiniciar una partida.
     */
    public void clearHand() {
        hand.clear();
        ready = false;
    }

    /**
     * Devuelve al jugador como texto legible, útil para depuración.
     *
     * @return Texto con nombre, rol y cantidad de cartas.
     */
    @Override
    public String toString() {
        String role = isHost ? "[HOST]" : "[jugador]";
        return role + " " + name + " | Cartas en mano: " + hand.size();
    }
}
