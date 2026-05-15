/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

import Dominio.Player;

/**
 * Evento publicado por el Peer cuando el timer del periodo de gracia UNO
 * expira sin que el jugador haya declarado UNO.
 *
 * <p>El Peer lo envía al Host vía red. El Host aplica la penalización
 * de 2 cartas llamando a {@code GameModel.onUnoTimerExpired()}.
 *
 * <p>Extiende {@link GameEvent} (clase abstracta) para heredar el timestamp
 * automático que registra cuándo ocurrió el evento.
 */
public class UnoPenaltyEvent extends GameEvent {

    /** El jugador que no declaró UNO a tiempo. */
    private final Player player;

    /**
     * @param player El jugador que recibirá la penalización de 2 cartas.
     */
    public UnoPenaltyEvent(Player player) {
        super(); // registra el timestamp automáticamente
        this.player = player;
    }

    /** @return El jugador penalizado. */
    public Player getPlayer() {
        return player;
    }
}
