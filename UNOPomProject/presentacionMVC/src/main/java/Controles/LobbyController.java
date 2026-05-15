package Controles;

import Eventos.StartRequestedEvent;
import Eventos.PlayerReadyEvent;
import Eventos.PlayerDisconnectedEvent;
import Eventos.PlayerJoinedEvent;
import Eventos.GameEventFactory;
import Eventos.GameStartedEvent;
import Vistas.CrearSala;
import Vistas.ModoJuego;
import Vistas.SalaEspera;
import Eventos.EventBus;
import Main.MainWindow;
import Dominio.LobbyState;
import Dominio.Player;
import Red.NetworkLayer;
import Controles.GameSession;

import javax.swing.*;
import Vistas.LobbyView;

/**
 * Controlador de la sala de espera. Maneja las acciones del jugador durante el
 * lobby: marcar listo, configurar el tamaño de sala, iniciar la partida y
 * salir. Reacciona a eventos del bus para mantener la interfaz actualizada
 * cuando otros jugadores se unen o se van.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class LobbyController {

    private final LobbyView view;
    private final SalaEspera lobbyView;
    private final CrearSala configurationView;
    private final ModoJuego gameModeView;
    private final MainWindow mainWindow;
    private final GameSession session;
    private final EventBus eventBus;
    private final LobbyState lobbyState;
    private final NetworkLayer networkLayer;

    /**
     * Rastrea si el jugador local ya presionó "Listo".
     */
    private boolean localPlayerReady;

    /**
     * Construye el Controller del lobby con todos los objetos ya inicializados.
     *
     * @param view La View del lobby.
     * @param mainWindow La ventana principal para navegación.
     * @param session La sesión del jugador local.
     * @param lobbyState El estado de sala creado en RegisterController.
     * @param networkLayer La capa de red ya conectada.
     */
    public LobbyController(LobbyView view, MainWindow mainWindow,
            GameSession session, LobbyState lobbyState,
            NetworkLayer networkLayer) {
        this.view = view;
        this.mainWindow = mainWindow;
        this.session = session;
        this.lobbyState = lobbyState;
        this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost(); // Host siempre listo
        this.configurationView = null;
        this.gameModeView = null;
        this.lobbyView = null;
    }

    //constructor con el Crear sala (PRUEBAAAS)
    public LobbyController(CrearSala view, MainWindow mainWindow, GameSession session, LobbyState lobbyState, NetworkLayer networkLayer) {
        this.configurationView = view;
        this.mainWindow = mainWindow;
        this.session = session;
        this.lobbyState = lobbyState;
        this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost(); // Host siempre listo
        this.view = null;
        this.gameModeView = null;
        this.lobbyView = null;
    }

    //constructor con el ModoJuego (PRUEBAAAS)
    public LobbyController(ModoJuego gameModeView, MainWindow mainWindow, GameSession session, LobbyState lobbyState, NetworkLayer networkLayer) {
        this.gameModeView = gameModeView;
        this.mainWindow = mainWindow;
        this.session = session;
        this.lobbyState = lobbyState;
        this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost(); // Host siempre listo
        this.view = null;
        this.configurationView = null;
        this.lobbyView = null;
    }

    public LobbyController(SalaEspera lobbyview, MainWindow mainWindow, GameSession session, LobbyState lobbyState, NetworkLayer networkLayer) {
        this.gameModeView = null;
        this.mainWindow = mainWindow;
        this.session = session;
        this.lobbyState = lobbyState;
        this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost(); // Host siempre listo
        this.view = null;
        this.configurationView = null;
        this.lobbyView = lobbyview;
    }

    /**
     * Inicializa el lobby: configura la View según el rol y se suscribe a los
     * eventos del bus.
     */
    public void initialize() {
        // Sincronizar el objeto Player con el estado inicial del controller.
        // El constructor de Player tiene ready=false por defecto, pero el Host
        // comienza como listo — sin esto canStart() nunca retorna true.
        session.getLocalPlayer().setReady(localPlayerReady);
        lobbyState.getConnectedPlayers().stream()
                .filter(p -> p.getName().equals(session.getLocalPlayer().getName()))
                .findFirst()
                .ifPresent(p -> p.setReady(localPlayerReady));

        lobbyView.configureForRole(session.isHost());
        lobbyView.setRoomCode(session.getRoomCode());
        lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
        lobbyView.updateReadyButton(localPlayerReady);

        refreshPlayerList();
        registerEventListeners();
    }

    // ─────────────────────────────────────────────
    // Acciones del usuario → vienen de la View
    // ─────────────────────────────────────────────
    //metodo para continuar desde la pantalla CrearSala
    public void onContinueClicked() {
        if (configurationView.getTamanhoSala() > 0) {
            lobbyState.setCapacity(configurationView.getTamanhoSala());
            System.out.println(configurationView.getTamanhoSala());
            SwingUtilities.invokeLater(()
                    -> // aqui deberiamos de mostrar el configurar sala dejate mi intento abajo
                    mainWindow.showLobby(session, lobbyState, networkLayer));
        } else {
            configurationView.showError("Debes seleccionar un tamaño válido.");
        }

    }

    //metodo para ir a Crear sala
    public void onConitueClickedModoJuego() {
        SwingUtilities.invokeLater(()
                -> // aqui deberiamos de mostrar el configurar sala dejate mi intento abajo
                mainWindow.showLobby(session, lobbyState, networkLayer));
    }

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

        lobbyView.updateReadyButton(localPlayerReady);
        eventBus.publish(GameEventFactory.playerReady(
                session.getLocalPlayer().getName(), localPlayerReady));

        // Habilitar botón de solicitud solo cuando el Peer esté listo
        if (!session.isHost()) {
            lobbyView.setRequestStartEnabled(localPlayerReady);
        }

        refreshPlayerList();
        checkStartCondition();
    }

    /**
     * El Peer solicita al Host iniciar la partida antes de completar el cupo.
     * Solo disponible cuando el Peer ya marcó "Listo".
     */
    public void onRequestStartClicked() {
        if (session.isHost() || !localPlayerReady) {
            return;
        }
        eventBus.publish(GameEventFactory.startRequested(
                session.getLocalPlayer().getName()));
    }

    /**
     * Inicia la partida. Solo el Host puede ejecutar esto.
     */
    public void onStartClicked() {
        if (!session.isHost() || !lobbyState.canStart()) {
            return;
        }

        lobbyState.markGameStarted();
        // Publicamos GameStartedEvent. NetworkLayer lo hará broadcast a los Peers.
        // El listener de GameStartedEvent más abajo maneja la navegación
        // tanto para el Host como para los Peers, evitando llamar showGame() dos veces.
        eventBus.publish(GameEventFactory.gameStarted());
    }

    /**
     * Sale del lobby tras confirmar con el jugador.
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
        if (!session.isHost()) {
            return;
        }
        lobbyState.setCapacity(newCapacity);
        // FIX: null guard para evitar NPE si este método se llama desde un constructor
        // que no usa lobbyView (ej. CrearSala o ModoJuego constructors).
        if (lobbyView != null) {
            lobbyView.setPlayerCount(lobbyState.getPlayerCount(), newCapacity);
        }
        // Notificar a los Peers del nuevo tamaño de sala
        networkLayer.broadcast("{\"type\":\"LOBBY_STATE\",\"capacity\":\"" + newCapacity + "\"}\n");

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
//                view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity()); pantallas anteriores
//                view.setCapacityDisplay(lobbyState.getCapacity());
                lobbyView.labelTamanhoSala.setText(lobbyState.getCapacity() + "");
                lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
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
                if (e.getPlayerName().equals(session.getLocalPlayer().getName())) {
                    localPlayerReady = e.isReady();
                    lobbyView.updateReadyButton(localPlayerReady);
                }

                refreshPlayerList();
                lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                checkStartCondition();
            });
        });

        // Un jugador se desconectó
        eventBus.subscribe(PlayerDisconnectedEvent.class, event -> {
            PlayerDisconnectedEvent e = (PlayerDisconnectedEvent) event;
            final boolean hostLeft = isHostName(e.getPlayerName());
            lobbyState.removePlayer(e.getPlayerName());

            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());

                if (hostLeft && !session.isHost()) {
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
            if (!session.isHost()) {
                return;
            }
            StartRequestedEvent e = (StartRequestedEvent) event;
            SwingUtilities.invokeLater(() -> {
                if (lobbyView.showStartRequestDialog(e.getRequesterName())) {
                    onStartClicked();
                }
            });
        });

        // La partida inició: navegar a la pantalla del juego.
        // NO llamamos clearAll() porque eso eliminaría los listeners de
        // NetworkLayer que son necesarios para recibir eventos del juego.
        // GameController registrará sus propios listeners al inicializarse.
        eventBus.subscribe(GameStartedEvent.class, event
                -> SwingUtilities.invokeLater(mainWindow::showGame));
    }

    // ─────────────────────────────────────────────
    // Ayudas internas
    // ─────────────────────────────────────────────
    /**
     * Redibuja la lista de jugadores en la View.
     */
    private void refreshPlayerList() {
        lobbyView.updatePlayerList(
                lobbyState.getConnectedPlayers(),
                session.getLocalPlayer().getName(),
                session.isHost()
        );
    }

    /**
     * Habilita el botón Iniciar si se cumplen las condiciones.
     */
    private void checkStartCondition() {
        boolean canStart = lobbyState.canStart();
        // FIX: eliminada llamada duplicada a setStartEnabled que existía en el código original
        if (session.isHost()) {
            if (lobbyView != null) {
                lobbyView.setStartEnabled(canStart);
            }
        }
        if (lobbyView != null) {
            if (canStart) {
                lobbyView.setWaitingStatus("Todos los jugadores están listos.");
            } else if (lobbyState.allPlayersReady()) {
                lobbyView.setWaitingStatus("Esperando que se unan más jugadores...");
            } else {
                lobbyView.setWaitingStatus("Esperando jugadores...");
            }
        }
    }

    /**
     * Verifica si el nombre dado corresponde al Host de la sala.
     */
    private boolean isHostName(String name) {
        return lobbyState.getConnectedPlayers().stream()
                .filter(Player::isHost)
                .map(Player::getName)
                .anyMatch(n -> n.equals(name));
    }
}
