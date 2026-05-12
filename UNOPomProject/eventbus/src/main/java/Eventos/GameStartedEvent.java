package Eventos;

import Eventos.GameEvent;

/**
 * Evento: la partida comenzó oficialmente.
 *
 * <p>El Host lo publica cuando presiona "Iniciar Partida".
 * La NetworkLayer lo hace broadcast a todos los peers para que
 * transicionen del lobby a la mesa de juego.
 */
public class GameStartedEvent extends GameEvent {
    /** No necesita datos: el hecho de ocurrir es suficiente. */
    public GameStartedEvent() { super(); }
}
