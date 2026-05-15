package Eventos;

import Eventos.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El bus de eventos del juego. Núcleo de la arquitectura EDA.
 *
 * <p>
 * Funciona como un tablero de anuncios central:
 * <ul>
 * <li>Cualquier clase puede <b>suscribirse</b> a un tipo de evento.</li>
 * <li>Cualquier clase puede <b>publicar</b> un evento.</li>
 * <li>El bus notifica automáticamente a todos los suscriptores.</li>
 * </ul>
 *
 * <p>
 * <b>Patrones aplicados:</b>
 * <ul>
 * <li><b>Observer:</b> los suscriptores son notificados sin que el publicador
 * los conozca directamente.</li>
 * <li><b>Singleton:</b> toda la aplicación comparte el mismo bus. Si hubiera
 * dos instancias, los suscriptores de una no recibirían los eventos publicados
 * en la otra.</li>
 * </ul>
 *
 * <p>
 * <b>Cómo suscribirse:</b>
 * <pre>
 *   EventBus.getInstance().subscribe(CardPlayedEvent.class, event -> {
 *       CardPlayedEvent e = (CardPlayedEvent) event;
 *       // hacer algo con e.getCard()
 *   });
 * </pre>
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class EventBus {

    /**
     * La única instancia del bus en toda la aplicación.
     */
    private static EventBus instance;

    /**
     * Mapa de suscriptores: clase del evento → lista de listeners. Usamos la
     * clase como clave porque es inmutable y única por tipo.
     */
    private final Map<Class<? extends GameEvent>, List<GameEventListener>> listeners;

    /**
     * Constructor privado: solo {@link #getInstance()} puede crear el bus.
     */
    private EventBus() {
        this.listeners = new HashMap<>();
    }

    /**
     * Devuelve la única instancia del bus. La crea si aún no existe.
     *
     * @return La instancia única del EventBus.
     */
    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Suscribe un listener a un tipo de evento específico.
     *
     * <p>
     * A partir de este momento, cada vez que se publique un evento de ese tipo,
     * el listener será llamado automáticamente.
     *
     * @param eventType La clase del evento al que suscribirse (ej.
     * {@code CardPlayedEvent.class}).
     * @param listener La función que se ejecutará cuando ocurra el evento.
     */
    public void subscribe(Class<? extends GameEvent> eventType,
            GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(listener);
    }

    /**
     * Elimina un listener de un tipo de evento. Útil cuando una pantalla se
     * cierra y ya no necesita recibir eventos.
     *
     * @param eventType La clase del evento del que desuscribirse.
     * @param listener El listener a eliminar.
     */
    public void unsubscribe(Class<? extends GameEvent> eventType,
            GameEventListener listener) {
        List<GameEventListener> subs = listeners.get(eventType);
        if (subs != null) {
            subs.remove(listener);
        }
    }

    /**
     * Publica un evento en el bus.
     *
     * <p>
     * Todos los listeners suscritos al tipo de este evento son notificados
     * inmediatamente. Se itera sobre una copia de la lista para evitar
     * problemas si un listener se desuscribe durante la notificación.
     *
     * @param event El evento que ocurrió.
     */
    public void publish(GameEvent event) {
        List<GameEventListener> subs = listeners.get(event.getClass());
        if (subs == null || subs.isEmpty()) {
            return;
        }

        // Copia para evitar ConcurrentModificationException
        new ArrayList<>(subs).forEach(l -> l.onEvent(event));
    }

    /**
     * Elimina todos los suscriptores. Se llama al reiniciar una partida para
     * limpiar el estado anterior.
     */
    public void clearAll() {
        listeners.clear();
    }
}
