package Controles;

import Eventos.StartRequestedEvent;
import Eventos.PlayerReadyEvent;
import Eventos.PlayerDisconnectedEvent;
import Eventos.PlayerJoinedEvent;
import Eventos.GameEventFactory;
import Eventos.GameStartedEvent;
import Eventos.EventBus;
import Main.MainWindow;
import Dominio.LobbyState;
import Dominio.Player;
import Red.NetworkLayer;
import Vistas.CrearSala;
import Vistas.LobbyView;
import Vistas.SalaEspera;

import javax.swing.*;

public class LobbyController {

    private final LobbyView  view;
    private final SalaEspera lobbyView;
    private final CrearSala  configurationView;
    private final MainWindow mainWindow;
    private final GameSession session;
    private final EventBus   eventBus;
    private final LobbyState lobbyState;
    private final NetworkLayer networkLayer;

    private boolean localPlayerReady;

    public LobbyController(LobbyView view, MainWindow mainWindow,
                           GameSession session, LobbyState lobbyState,
                           NetworkLayer networkLayer) {
        this.view = view; this.lobbyView = null; this.configurationView = null;
        this.mainWindow = mainWindow; this.session = session;
        this.lobbyState = lobbyState; this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost();
    }

    public LobbyController(CrearSala view, MainWindow mainWindow,
                           GameSession session, LobbyState lobbyState,
                           NetworkLayer networkLayer) {
        this.configurationView = view; this.view = null; this.lobbyView = null;
        this.mainWindow = mainWindow; this.session = session;
        this.lobbyState = lobbyState; this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost();
    }

    public LobbyController(SalaEspera lobbyView, MainWindow mainWindow,
                           GameSession session, LobbyState lobbyState,
                           NetworkLayer networkLayer) {
        this.lobbyView = lobbyView; this.view = null; this.configurationView = null;
        this.mainWindow = mainWindow; this.session = session;
        this.lobbyState = lobbyState; this.networkLayer = networkLayer;
        this.eventBus = EventBus.getInstance();
        this.localPlayerReady = session.isHost();
    }

    public void initialize() {
        session.getLocalPlayer().setReady(localPlayerReady);
        lobbyState.getConnectedPlayers().stream()
            .filter(p -> p.getName().equals(session.getLocalPlayer().getName()))
            .findFirst().ifPresent(p -> p.setReady(localPlayerReady));

        lobbyView.configureForRole(session.isHost());
        lobbyView.setRoomCode(session.getRoomCode());
        lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
        lobbyView.updateReadyButton(localPlayerReady);
        refreshPlayerList();
        registerEventListeners();
    }

    /**
     * Llamado cuando el usuario hace clic en una carta de tamaño en CrearSala.
     * Configura la capacidad del lobby Y navega directamente al lobby sin
     * necesidad de un boton "Siguiente" separado.
     */
    public void onCardSizeClicked(int size) {
        lobbyState.setCapacity(size);
        SwingUtilities.invokeLater(() ->
            mainWindow.showLobby(session, lobbyState, networkLayer));
    }

    public void onReadyClicked() {
        localPlayerReady = !localPlayerReady;
        session.getLocalPlayer().setReady(localPlayerReady);
        lobbyState.getConnectedPlayers().stream()
                .filter(p -> p.getName().equals(session.getLocalPlayer().getName()))
                .findFirst().ifPresent(p -> p.setReady(localPlayerReady));
        if (lobbyView != null) lobbyView.updateReadyButton(localPlayerReady);
        eventBus.publish(GameEventFactory.playerReady(
                session.getLocalPlayer().getName(), localPlayerReady));
        if (!session.isHost() && lobbyView != null)
            lobbyView.setRequestStartEnabled(localPlayerReady);
        refreshPlayerList();
        checkStartCondition();
    }

    public void onRequestStartClicked() {
        if (session.isHost() || !localPlayerReady) return;
        eventBus.publish(GameEventFactory.startRequested(
                session.getLocalPlayer().getName()));
    }

    public void onStartClicked() {
        if (!session.isHost() || !lobbyState.canStart()) return;
        lobbyState.markGameStarted();
        eventBus.publish(GameEventFactory.gameStarted());
    }

    public void onLeaveClicked() {
        int confirm = JOptionPane.showConfirmDialog(mainWindow,
                "¿Estás seguro de que deseas salir de la sala?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            eventBus.publish(GameEventFactory.playerDisconnected(
                    session.getLocalPlayer().getName()));
            SwingUtilities.invokeLater(mainWindow::showRegister);
        }
    }

    public void onCapacityChanged(int newCapacity) {
        if (!session.isHost()) return;
        lobbyState.setCapacity(newCapacity);
        if (lobbyView != null) lobbyView.setPlayerCount(lobbyState.getPlayerCount(), newCapacity);
        if (view != null)      view.setPlayerCount(lobbyState.getPlayerCount(), newCapacity);
        networkLayer.broadcast("{\"type\":\"LOBBY_STATE\",\"capacity\":\"" + newCapacity + "\"}\n");
        checkStartCondition();
    }

    private void registerEventListeners() {
        eventBus.subscribe(PlayerJoinedEvent.class, event ->
            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                if (lobbyView != null) lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                if (view != null)      view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                checkStartCondition();
            }));

        eventBus.subscribe(PlayerReadyEvent.class, event -> {
            PlayerReadyEvent e = (PlayerReadyEvent) event;
            lobbyState.getConnectedPlayers().stream()
                    .filter(p -> p.getName().equals(e.getPlayerName()))
                    .findFirst().ifPresent(p -> p.setReady(e.isReady()));
            SwingUtilities.invokeLater(() -> {
                if (e.getPlayerName().equals(session.getLocalPlayer().getName())) {
                    localPlayerReady = e.isReady();
                    if (lobbyView != null) lobbyView.updateReadyButton(localPlayerReady);
                }
                refreshPlayerList();
                if (lobbyView != null) lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                if (view != null)      view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                checkStartCondition();
            });
        });

        eventBus.subscribe(PlayerDisconnectedEvent.class, event -> {
            PlayerDisconnectedEvent e = (PlayerDisconnectedEvent) event;
            final boolean hostLeft = isHostName(e.getPlayerName());
            lobbyState.removePlayer(e.getPlayerName());
            SwingUtilities.invokeLater(() -> {
                refreshPlayerList();
                if (lobbyView != null) lobbyView.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                if (view != null)      view.setPlayerCount(lobbyState.getPlayerCount(), lobbyState.getCapacity());
                if (hostLeft && !session.isHost()) {
                    JOptionPane.showMessageDialog(mainWindow,
                            "El Host abandonó. La sala fue cerrada.",
                            "Sala cerrada", JOptionPane.WARNING_MESSAGE);
                    mainWindow.showRegister();
                }
            });
        });

        eventBus.subscribe(StartRequestedEvent.class, event -> {
            if (!session.isHost()) return;
            StartRequestedEvent e = (StartRequestedEvent) event;
            SwingUtilities.invokeLater(() -> {
                boolean accept = (lobbyView != null && lobbyView.showStartRequestDialog(e.getRequesterName()))
                              || (view != null && view.showStartRequestDialog(e.getRequesterName()));
                if (accept) onStartClicked();
            });
        });

        eventBus.subscribe(GameStartedEvent.class, event ->
                SwingUtilities.invokeLater(mainWindow::showGame));
    }

    private void refreshPlayerList() {
        String localName = session.getLocalPlayer().getName();
        boolean isHost   = session.isHost();
        if (lobbyView != null) lobbyView.updatePlayerList(lobbyState.getConnectedPlayers(), localName, isHost);
        if (view != null)      view.updatePlayerList(lobbyState.getConnectedPlayers(), localName, isHost);
    }

    private void checkStartCondition() {
        boolean canStart = lobbyState.canStart();
        if (session.isHost()) {
            if (lobbyView != null) lobbyView.setStartEnabled(canStart);
            if (view != null)      view.setStartEnabled(canStart);
        }
        String status = canStart ? "Todos los jugadores están listos."
            : (lobbyState.allPlayersReady() ? "Esperando más jugadores..." : "Esperando jugadores...");
        if (lobbyView != null) lobbyView.setWaitingStatus(status);
    }

    private boolean isHostName(String name) {
        return lobbyState.getConnectedPlayers().stream()
                .filter(Player::isHost).map(Player::getName).anyMatch(n -> n.equals(name));
    }
}
