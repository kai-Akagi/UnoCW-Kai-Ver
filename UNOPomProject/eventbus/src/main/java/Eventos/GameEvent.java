package Eventos;

/**
 * Clase base para todos los eventos del juego.
 *
 * <p>
 * En la arquitectura EDA, cada cosa que "pasa" se representa como un objeto que
 * extiende esta clase. Los eventos fluyen por el {@link Eventos.EventBus}, que
 * notifica a todos los suscriptores.
 *
 * <p>
 * <b>Por qué es pública y abstracta:</b><br>
 * Pública: cualquier paquete del proyecto necesita referenciarla con
 * {@code .class} para suscribirse al EventBus.<br>
 * Abstracta: nadie debe instanciar un "GameEvent genérico"; siempre se
 * instancia una subclase concreta que describe qué ocurrió.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public abstract class GameEvent {

    /**
     * El momento en que ocurrió este evento. Se registra automáticamente al
     * crear el evento.
     */
    private final long timestamp;

    /**
     * Construye un evento y registra el momento en que ocurrió.
     */
    protected GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Regresa el timestamp de creacion del evento en milisegundos.
     *
     * @return El timestamp de creación del evento en milisegundos.
     */
    public long getTimestamp() {
        return timestamp;
    }
}
