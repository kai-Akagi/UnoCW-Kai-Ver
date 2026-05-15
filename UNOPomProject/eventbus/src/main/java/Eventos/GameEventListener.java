package Eventos;

import Eventos.GameEvent;

/**
 * Contrato que debe cumplir cualquier clase que quiera escuchar eventos del
 * juego.
 *
 * <p>
 * Esta es una <b>interfaz funcional</b>: tiene un solo método. Eso nos permite
 * usarla con expresiones lambda (funciones cortas en Java), lo que hace el
 * código de suscripción mucho más limpio.
 *
 * <p>
 * <b>Patrón Observer:</b> Esta interfaz define lo que es un "observador".
 * Cualquier cosa que implemente {@code GameEventListener} puede suscribirse al
 * {@link EventBus} y recibirá notificaciones automáticas.
 *
 * <p>
 * <b>Ejemplo con clase:</b>
 * <pre>
 *   public class GameScreen implements GameEventListener {
 *       {@literal @}Override
 *       public void onEvent(GameEvent event) {
 *           // Actualizar la pantalla
 *       }
 *   }
 * </pre>
 *
 * <p>
 * <b>Ejemplo con lambda (más corto y legible):</b>
 * <pre>
 *   bus.subscribe(TurnChangedEvent.class, event -> actualizarTurno(event));
 * </pre>
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
@FunctionalInterface
public interface GameEventListener {

    /**
     * Se llama automáticamente cuando ocurre un evento del tipo suscrito.
     *
     * <p>
     * Dentro de este método, puedes hacer un cast al tipo concreto del evento
     * para acceder a sus datos:
     * <pre>
     *   if (event instanceof CardPlayedEvent) {
     *       CardPlayedEvent e = (CardPlayedEvent) event;
     *       // usar e.getCard(), e.getPlayer(), etc.
     *   }
     * </pre>
     *
     * @param event El evento que ocurrió. Puede ser cualquier subclase de
     * {@link GameEvent}.
     */
    void onEvent(GameEvent event);
}
