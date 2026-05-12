package Eventos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus de eventos central del juego (patrón Observer / EDA).
 *
 * <p>Funciona como tablero de anuncios: cualquier clase puede suscribirse
 * a un tipo de evento y cualquier clase puede publicarlo. El bus notifica
 * automáticamente a todos los suscriptores.
 *
 * <p><b>Patrones:</b> Singleton (instancia única compartida) + Observer
 * (suscriptores notificados sin que el publicador los conozca).
 *
 * <p><b>Thread-safety:</b> Usa {@link ConcurrentHashMap} y
 * {@link CopyOnWriteArrayList} para soportar acceso concurrente seguro
 * desde el EDT de Swing y los hilos de red, sin {@code synchronized}.
 * FIX: la instancia se crea con doble-checked locking para evitar
 * condiciones de carrera al inicializar el Singleton.
 */
public class EventBus {

    /** Instancia única, volatile para visibilidad entre hilos. */
    private static volatile EventBus instance;

    /**
     * Mapa suscriptores: clase del evento → lista de listeners.
     * ConcurrentHashMap es thread-safe para lecturas/escrituras concurrentes.
     */
    private final Map<Class<? extends GameEvent>, List<GameEventListener>> listeners;

    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }

    /**
     * Devuelve la única instancia del bus.
     * Usa doble-checked locking para thread-safety sin sincronizar siempre.
     *
     * @return La instancia única del EventBus.
     */
    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    /**
     * Suscribe un listener a un tipo de evento específico.
     *
     * @param eventType La clase del evento (ej. {@code CardPlayedEvent.class}).
     * @param listener  Función que se ejecutará cuando ocurra el evento.
     */
    public void subscribe(Class<? extends GameEvent> eventType,
                          GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    /**
     * Elimina un listener de un tipo de evento.
     *
     * @param eventType La clase del evento del que desuscribirse.
     * @param listener  El listener a eliminar.
     */
    public void unsubscribe(Class<? extends GameEvent> eventType,
                            GameEventListener listener) {
        List<GameEventListener> subs = listeners.get(eventType);
        if (subs != null) subs.remove(listener);
    }

    /**
     * Publica un evento notificando a todos los suscriptores de su tipo.
     * Itera sobre una snapshot de la lista para evitar CME si un listener
     * se desuscribe durante la notificación.
     *
     * @param event El evento que ocurrió.
     */
    public void publish(GameEvent event) {
        List<GameEventListener> subs = listeners.get(event.getClass());
        if (subs == null || subs.isEmpty()) return;
        // CopyOnWriteArrayList itera sobre snapshot → no necesita copia manual
        subs.forEach(l -> l.onEvent(event));
    }

    /** Elimina todos los suscriptores. Se llama al reiniciar una partida. */
    public void clearAll() {
        listeners.clear();
    }
}
