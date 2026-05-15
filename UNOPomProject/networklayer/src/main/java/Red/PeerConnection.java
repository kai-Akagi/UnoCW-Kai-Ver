package Red;

import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;

/**
 * Representa y gestiona la conexión de red con un peer específico.
 *
 * <p>
 * Cada par de jugadores conectados tiene un objeto {@code PeerConnection} de
 * este lado. Se encarga de dos cosas simples:
 * <ul>
 * <li><b>Enviar</b> mensajes de texto por el socket.</li>
 * <li><b>Escuchar</b> mensajes entrantes en un hilo propio y notificar cuando
 * llega uno.</li>
 * </ul>
 *
 * <p>
 * <b>¿Por qué una clase separada?</b><br>
 * Cada conexión necesita su propio hilo de escucha. Si la {@link NetworkLayer}
 * lo hiciera directamente, su código se llenaría de manejo de hilos y streams.
 * Separarlo aquí mantiene a {@link NetworkLayer} limpia y enfocada en el
 * enrutamiento de mensajes.
 *
 * <p>
 * <b>Protocolo:</b> Un mensaje por línea. El emisor termina cada mensaje con
 * {@code \n}. El receptor lee línea por línea con {@link BufferedReader}.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class PeerConnection {

    /**
     * Nombre del jugador en el otro extremo de esta conexión.
     */
    private String name;

    /**
     * El socket de red que representa el canal de comunicación.
     */
    private final Socket socket;

    /**
     * Para escribir mensajes al socket. {@code PrintWriter} con autoflush: cada
     * vez que escribimos una línea, se envía automáticamente sin necesidad de
     * llamar a flush().
     */
    private final PrintWriter writer;

    /**
     * Para leer mensajes del socket. {@code BufferedReader} lee línea a línea
     * de forma eficiente.
     */
    private final BufferedReader reader;

    /**
     * La función que se llama cuando llega un mensaje. Recibe el nombre del
     * peer y el mensaje recibido. Esto es el patrón callback: quien crea la
     * conexión decide qué hacer con los mensajes, no la conexión misma.
     */
    private final BiConsumer<String, String> onMessageReceived;

    /**
     * Indica si el hilo de escucha debe seguir corriendo.
     */
    private volatile boolean listening;

    /**
     * Crea una conexión con un peer a través de un socket existente.
     *
     * @param name El nombre del jugador en el otro extremo.
     * @param socket El socket de la conexión.
     * @param onMessageReceived Callback que se invoca cuando llega un mensaje.
     * @throws IOException Si no se pueden abrir los streams del socket.
     */
    public PeerConnection(String name, Socket socket,
            BiConsumer<String, String> onMessageReceived) {
        this.name = name;
        this.socket = socket;
        this.onMessageReceived = onMessageReceived;
        this.listening = false;

        try {
            // autoFlush = true: cada println() envía inmediatamente
            this.writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            this.reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir los streams del socket: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Inicia el hilo de escucha que espera mensajes entrantes.
     *
     * <p>
     * El hilo corre en segundo plano (daemon) y llama a
     * {@code onMessageReceived} cada vez que llega una línea completa. Si la
     * conexión se cierra o hay un error, el hilo termina solo.
     */
    public void startListening() {
        listening = true;
        Thread listenThread = new Thread(() -> {
            try {
                String line;
                // readLine() espera bloqueante hasta que llega una línea o se cierra el socket
                while (listening && (line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        onMessageReceived.accept(name, line);
                    }
                }
            } catch (IOException e) {
                if (listening) {
                    // El peer se desconectó inesperadamente
                    onMessageReceived.accept(name,
                            "{\"type\":\"PLAYER_LEFT\",\"player\":\"" + name + "\"}");
                }
            } finally {
                listening = false;
            }
        }, "listen-" + name);

        listenThread.setDaemon(true);
        listenThread.start();
    }

    /**
     * Envía un mensaje de texto al peer.
     *
     * <p>
     * El mensaje debe estar serializado (JSON en una línea). El
     * {@code PrintWriter} con autoFlush se encarga de que llegue inmediatamente
     * sin necesidad de buffering manual.
     *
     * @param message El mensaje a enviar. Debe ser una sola línea sin saltos.
     */
    public void sendMessage(String message) {
        if (!socket.isClosed() && writer != null) {
            writer.println(message); // println agrega \n automáticamente
        }
    }

    /**
     * Cierra esta conexión y libera los recursos del socket.
     */
    public void close() {
        listening = false;
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * @return El nombre del peer en el otro extremo.
     */
    public String getName() {
        return name;
    }

    /**
     * Actualiza el nombre de este peer. Se usa cuando el nombre temporal se
     * reemplaza por el nombre real del jugador.
     *
     * @param name El nombre real del jugador.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return {@code true} si la conexión está activa y escuchando.
     */
    public boolean isListening() {
        return listening;
    }
}
