/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

import Dominio.Player;

/**
 * Evento: un jugador solicitó robar una carta del mazo.
 *
 * <p>
 * Este evento representa la <b>intención</b> del jugador, no el resultado. Lo
 * publica el Peer en su EventBus local cuando presiona el botón "Robar". La
 * NetworkLayer lo lleva al Host, quien ejecuta el robo real en su GameModel y
 * devuelve el resultado en dos eventos:
 * <ul>
 * <li>{@link CardDrawnPrivateEvent}: solo al jugador que robó (con la carta
 * real).</li>
 * <li>{@link CardDrawnPublicEvent}: a todos los demás (sin revelar la
 * carta).</li>
 * </ul>
 *
 * <p>
 * <b>¿Por qué un evento de intención separado?</b><br>
 * Porque en la arquitectura P2P el Peer no puede decidir qué carta saca del
 * mazo: esa decisión pertenece al Host (fuente de verdad). El Peer solo
 * comunica que quiere robar; el Host actúa y le informa el resultado.
 *
 * <p>
 * Sigue el mismo patrón que {@link StartRequestedEvent}: un jugador expresa una
 * intención y el Host decide la respuesta.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class DrawCardRequestEvent extends GameEvent {

    /**
     * El jugador que quiere robar una carta.
     */
    private final Player player;

    /**
     * @param player El jugador que presionó el botón de robar.
     */
    public DrawCardRequestEvent(Player player) {
        super();
        this.player = player;
    }

    /**
     * Devuelve el jugador que solicitó robar.
     *
     * @return El jugador solicitante.
     */
    public Player getPlayer() {
        return player;
    }
}
