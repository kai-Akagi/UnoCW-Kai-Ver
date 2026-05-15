package Main;

import Controles.GameController;
import Controles.GameSession;
import Controles.LobbyController;
import Controles.RegisterController;
import Dominio.LobbyState;
import Red.NetworkLayer;
import Vistas.CrearSala;
import Vistas.GameView;
import Vistas.LobbyView;
import Vistas.RegisterView;
import Vistas.ScoreboardView;
import Vistas.SalaEspera;

import javax.swing.*;
import java.awt.*;

/**
 * La ventana principal de la aplicación.
 *
 * <p>Es el único {@link JFrame} del programa. Usa {@link CardLayout}
 * para cambiar entre pantallas sin abrir nuevas ventanas.
 *
 * <p>Incluye la pantalla de selección de tamaño de sala (CrearSala)
 * entre el registro y el lobby. El Host pasa por ella antes de llegar
 * a la SalaEspera; los Peers van directamente al lobby al unirse.
 */
public class MainWindow extends JFrame {

    public static final String SCREEN_REGISTER      = "register";
    public static final String SCREEN_ROOM_CONFIG   = "roomConfig";
    public static final String SCREEN_LOBBY         = "lobby";
    public static final String SCREEN_GAME          = "game";
    public static final String SCREEN_SCOREBOARD    = "scoreboard";

    private final CardLayout cardLayout;
    private final JPanel     screenContainer;

    private GameSession  session;
    private NetworkLayer networkLayer;
    private LobbyState   lobbyState;

    public MainWindow() {
        super("UNO");
        this.cardLayout      = new CardLayout();
        this.screenContainer = new JPanel(cardLayout);
        configureWindow();
        buildScreens();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);
        setResizable(true);
        add(screenContainer);
    }

    private void buildScreens() {
        RegisterView       registerView       = new RegisterView();
        RegisterController registerController = new RegisterController(registerView, this);
        registerView.setController(registerController);

        screenContainer.add(registerView, SCREEN_REGISTER);
        screenContainer.add(new JPanel(), SCREEN_ROOM_CONFIG);
        screenContainer.add(new JPanel(), SCREEN_LOBBY);
        screenContainer.add(new JPanel(), SCREEN_GAME);
        screenContainer.add(new JPanel(), SCREEN_SCOREBOARD);

        cardLayout.show(screenContainer, SCREEN_REGISTER);
    }

    /**
     * Navega a la pantalla de selección de tamaño de sala.
     * Solo el Host pasa por aquí, justo después de registrarse.
     *
     * @param session    La sesión recién creada en RegisterController.
     * @param lobbyState El LobbyState del Host (con código ya asignado).
     * @param network    La NetworkLayer ya iniciada como Host.
     */
    public void showRoomConfiguration(GameSession session, LobbyState lobbyState,
                                      NetworkLayer network) {
        this.session      = session;
        this.networkLayer = network;
        this.lobbyState   = lobbyState;

        CrearSala       configView  = new CrearSala();
        LobbyController lobbyCtrl  = new LobbyController(
                configView, this, session, lobbyState, network);
        configView.setController(lobbyCtrl);

        screenContainer.add(configView, SCREEN_ROOM_CONFIG);
        cardLayout.show(screenContainer, SCREEN_ROOM_CONFIG);
    }

    /**
     * Navega al lobby (SalaEspera) con la sesión, el estado de sala y la red ya creados.
     *
     * @param session    La sesión del jugador registrado.
     * @param lobbyState El estado de sala ya inicializado.
     * @param network    La capa de red ya conectada.
     */
    public void showLobby(GameSession session, LobbyState lobbyState, NetworkLayer network) {
        this.session      = session;
        this.networkLayer = network;
        this.lobbyState   = lobbyState;

        SalaEspera      lobbyView  = new SalaEspera();
        LobbyController lobbyCtrl  = new LobbyController(
                lobbyView, this, session, lobbyState, network);
        lobbyView.setController(lobbyCtrl);

        screenContainer.add(lobbyView, SCREEN_LOBBY);
        cardLayout.show(screenContainer, SCREEN_LOBBY);

        lobbyCtrl.initialize();
    }

    /** Navega a la mesa de juego. */
    public void showGame() {
        GameView       gameView   = new GameView();
        GameController gameCtrl   = new GameController(
                gameView, this, session, networkLayer, lobbyState);
        gameView.setController(gameCtrl);

        screenContainer.add(gameView, SCREEN_GAME);
        cardLayout.show(screenContainer, SCREEN_GAME);

        gameCtrl.initialize();
    }

    /** Navega al scoreboard al terminar la partida. */
    public void showScoreboard(String winnerName) {
        ScoreboardView scoreboardView = new ScoreboardView(winnerName, session, this);
        screenContainer.add(scoreboardView, SCREEN_SCOREBOARD);
        cardLayout.show(screenContainer, SCREEN_SCOREBOARD);
    }

    /**
     * Vuelve a la pantalla de registro y limpia el estado anterior.
     */
    public void showRegister() {
        if (networkLayer != null) {
            networkLayer.shutdown();
        }
        Eventos.EventBus.getInstance().clearAll();
        this.session      = null;
        this.networkLayer = null;
        this.lobbyState   = null;
        cardLayout.show(screenContainer, SCREEN_REGISTER);
    }

    public void showScreen(String screenName) {
        cardLayout.show(screenContainer, screenName);
    }

    public GameSession  getSession()      { return session; }
    public NetworkLayer getNetworkLayer() { return networkLayer; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
