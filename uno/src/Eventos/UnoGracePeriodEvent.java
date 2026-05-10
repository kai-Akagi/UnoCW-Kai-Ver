/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

import Eventos.GameEvent;
import Dominio.Player;

/**
 * Evento publicado cuando un jugador juega su penúltima carta y le queda solo 1.
 *
 * <p>Al recibirlo, la GUI debe:
 * <ol>
 *   <li>Mostrar el botón "¡UNO!" habilitado en modo urgente.</li>
 *   <li>Iniciar un temporizador de 5 segundos.</li>
 *   <li>Si el jugador presiona UNO antes de que expire el timer → turno termina sin penalización.</li>
 *   <li>Si el timer expira sin presionar UNO → se aplican 2 cartas de penalización.</li>
 * </ol>
 *
 * <p>El turno NO avanza hasta que el jugador presione UNO o el timer expire.
 *
 * <p>Extiende {@link GameEvent} (clase abstracta) para heredar el timestamp
 * automático que registra cuándo ocurrió el evento.
 */
public class UnoGracePeriodEvent extends GameEvent {

    /** El jugador que quedó con 1 carta y debe declarar UNO. */
    private final Player player;

    /**
     * @param player El jugador que debe declarar UNO en los próximos 5 segundos.
     */
    public UnoGracePeriodEvent(Player player) {
        super(); // registra el timestamp automáticamente
        this.player = player;
    }

    /** @return El jugador que debe declarar UNO. */
    public Player getPlayer() {
        return player;
    }
}