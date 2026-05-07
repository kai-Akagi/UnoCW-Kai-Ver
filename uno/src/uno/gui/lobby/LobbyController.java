package uno.gui.lobby;

import uno.events.*;
import uno.events.bus.EventBus;
import uno.gui.MainWindow;
import uno.model.LobbyState;
import uno.model.Player;
import uno.network.NetworkLayer;
import uno.session.GameSession;
 
import javax.swing.*;
 
/**
 * Controller de la pantalla del lobby (sala de espera).
 *
 * <p><b>Correcciones respecto a la versión anterior:</b>
 * <ul>
 *   <li>Ya no crea su propio {@link LobbyState}: lo recibe del
 *       {@link uno.gui.register.RegisterController}, que es quien
 *       lo inicializó con el código de sala correcto.</li>
 *   <li>Recibe la {@link NetworkLayer} ya conectada en vez de crear una nueva.</li>
 *   <li>El botón "Salir" ahora navega de vuelta al registro correctamente
 *       llamando a {@link MainWindow#showRegister()}.</li>
 * </ul>
 */
public class LobbyController {
 
    private final LobbyView    view;
    private final MainWindow   mainWindow;
    private final GameSession  session;
    private final EventBus     eventBus;
    private final LobbyState   lobbyState;
    private final NetworkLayer networkLayer;
 
    /** Rastrea si el jugador local ya presionó "Listo". */
    private boolean localPlayerReady;
 
    /**
     * Construye el Controller del lobby con todos los objetos ya inicializados.
     *
     * @param view         La View del lobby.
     * @param mainWindow   La ventana principal para navegación.
     * @param session      La sesión del jugador local.
     * @param lobbyState   El estado de sala creado en RegisterController.
     * @param networkLayer La capa de red ya conectada.
     */
    public LobbyController(LobbyView view, MainWindow mainWindow,
                           GameSession session, LobbyState lobbyState,
                           NetworkLayer networkLayer) {
        this.view         = view;
        this.mainWindow   = mainWindow;
        this.session      = session;
        this.lobbyState   = lobbyState;
        this.networkLayer = networkLayer;
        this.eventBus     = EventBus.getInstance();
        this.localPlayerReady = session.isHost(); // Host siempre listo
    }
 
    /**
     * Inicializa el lobby: configura la View según el rol y se suscribe
     * a los eventos del bus.
     */
    public void initialize() {
        view.configureForRole(session.isHost());
        view.setRoomCode(session.getRoomCode());
        view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
        view.updateReadyButton(localPlayerReady);
 
        refreshPlayerList();
        registerEventListeners();
    }
 
    // ─────────────────────────────────────────────
    // Acciones del usuario → vienen de la View
    // ─────────────────────────────────────────────
 
    /**
     * Alterna el estado "Listo" del jugador local y lo publica en el bus.
     */
    public void onReadyClicked() {
        localPlayerReady = !localPlayerReady;
 
        // Actualizar el objeto Player en la sesión
        session.getLocalPlayer().setReady(localPlayerReady);
 
        // Actualizar también la referencia dentro del LobbyState,
        // que puede ser un objeto distinto aunque represente al mismo jugador.
        // Sin esto, refreshPlayerList() leería el estado antiguo del LobbyState.
        lobbyState.getConnectedPlayers().stream()
                .filter(p -> p.getName().equals(session.getLocalPlayer().getName()))
                .findFirst()
                .ifPresent(p -> p.setReady(localPlayerReady));
 
        view.updateReadyButton(localPlayerReady);
        eventBus.publish(GameEventFactory.playerReady(
                session.getLocalPlayer().getName(), localPlayerReady));

        // Habilitar botón de solicitud solo cuando el Peer esté listo
        if (!session.isHost()) {
            view.setRequestStartEnabled(localPlayerReady);
        }

        refreshPlayerList();
        checkStartCondition();
    }

    /**
     * El Peer solicita al Host iniciar la partida antes de completar el cupo.
     * Solo disponible cuando el Peer ya marcó "Listo".
     */
    public void onRequestStartClicked() {
        if (session.isHost() || !localPlayerReady) return;
        eventBus.publish(GameEventFactory.startRequested(
                session.getLocalPlayer().getName()));
    }

    /**
     * Inicia la partida. Solo el Host puede ejecutar esto.
     */
    public void onStartClicked() {
        if (!session.isHost() || !lobbyState.canStart()) return;
 
        lobbyState.markGameStarted();
        // Publicamos GameStartedEvent. NetworkLayer lo hará broadcast a los Peers.
        // El listener de GameStartedEvent más abajo maneja la navegación
        // tanto para el Host como para los Peers, evitando llamar showGame() dos veces.
        eventBus.publish(GameEventFactory.gameStarted());
    }
 
    /**
     * Sale del lobby con confirmación.
     *
     * <p><b>Corrección del bug 2:</b> Antes solo mostraba un mensaje
     * sin cambiar de pantalla. Ahora llama a {@link MainWindow#showRegister()}
     * que cierra la red y navega al registro correctamente.
     */
    public void onLeaveClicked() {
        int confirm = JOptionPane.showConfirmDialog(
                mainWindow,
                "¿Estás seguro de que deseas salir de la sala?",
                "Salir",
                JOptionPane.YES_NO_OPTION
        );
 
        if (confirm == JOptionPane.YES_OPTION) {
            // Notificar a los demás que este jugador se va
            eventBus.publish(GameEventFactory.playerDisconnected(
                    session.getLocalPlayer().getName()));
 
            // Navegar al registro. MainWindow cierra la red internamente.
            SwingUtilities.invokeLater(mainWindow::showRegister);
        }
    }
 
    /**
     * Cambia la capacidad de la sala. Solo el Host puede hacerlo.
     *
     * @param newCapacity La nueva capacidad (2, 3 o 4).
     */
    public void onCapacityChanged(int newCapacity) {
        if (!session.isHost()) return;
        lobbyState.setCapacity(newCapacity);
        view.setPlayerCount(lobbyState.getPlayerCount(), newCapacity);
        checkStartCondition();
    }
 
    // ─────────────────────────────────────────────
    // Eventos del bus → actualizan la View
    // ─────────────────────────────────────────────
 
    /**
     * Suscribe el Controller a los eventos de lobby relevantes.
     */
    private void registerEventListeners() {
 
        // Un nuevo jugador se unió.
        // NetworkLayer ya actualizó el LobbyState antes de publicar este evento,
        // así que aquí solo refrescamos la GUI sin volver a agregar al jugador.
        eventBus.subscribe(PlayerJoinedEvent.class, event -> {
            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                checkStartCondition();
            });
        });
 
        // Un jugador cambió su estado "Listo"
        eventBus.subscribe(PlayerReadyEvent.class, event -> {
            PlayerReadyEvent e = (PlayerReadyEvent) event;
            lobbyState.getConnectedPlayers().stream()
                    .filter(p -> p.getName().equals(e.getPlayerName()))
                    .findFirst()
                    .ifPresent(p -> p.setReady(e.isReady()));
 
            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                checkStartCondition();
            });
        });
 
        // Un jugador se desconectó
        eventBus.subscribe(PlayerDisconnectedEvent.class, event -> {
            PlayerDisconnectedEvent e = (PlayerDisconnectedEvent) event;
            lobbyState.removePlayer(e.getPlayerName());
 
            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
 
                // Si el Host se fue, cerrar la sala y volver al registro
                if (isHostName(e.getPlayerName()) && !session.isHost()) {
                    JOptionPane.showMessageDialog(mainWindow,
                            "El Host abandonó. La sala fue cerrada.",
                            "Sala cerrada", JOptionPane.WARNING_MESSAGE);
                    mainWindow.showRegister();
                }
            });
        });
 
        // Un Peer solicitó iniciar antes de completar el cupo.
        // Solo el Host muestra el diálogo y decide si acepta.
        eventBus.subscribe(StartRequestedEvent.class, event -> {
            if (!session.isHost()) return;
            StartRequestedEvent e = (StartRequestedEvent) event;
            SwingUtilities.invokeLater(() -> {
                if (view.showStartRequestDialog(e.getRequesterName())) {
                    onStartClicked();
                }
            });
        });

        // La partida inició: navegar a la pantalla del juego.
        // NO llamamos clearAll() porque eso eliminaría los listeners de
        // NetworkLayer que son necesarios para recibir eventos del juego.
        // GameController registrará sus propios listeners al inicializarse.
        eventBus.subscribe(GameStartedEvent.class, event ->
                SwingUtilities.invokeLater(mainWindow::showGame));
    }
 
    // ─────────────────────────────────────────────
    // Ayudas internas
    // ─────────────────────────────────────────────
 
    /** Redibuja la lista de jugadores en la View. */
    private void refreshPlayerList() {
        view.updatePlayerList(
                lobbyState.getConnectedPlayers(),
                session.getLocalPlayer().getName(),
                session.isHost()
        );
    }
 
    /** Habilita el botón Iniciar si se cumplen las condiciones. */
    private void checkStartCondition() {
        if (session.isHost()) {
            view.setStartEnabled(lobbyState.canStart());
        }
    }
 
    /** Verifica si el nombre dado corresponde al Host de la sala. */
    private boolean isHostName(String name) {
        return lobbyState.getConnectedPlayers().stream()
                .filter(Player::isHost)
                .map(Player::getName)
                .anyMatch(n -> n.equals(name));
    }
}
