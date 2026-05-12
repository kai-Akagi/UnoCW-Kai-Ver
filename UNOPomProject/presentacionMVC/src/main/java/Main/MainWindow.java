package Main;

import Vistas.CrearSala;
import Controles.GameModeController;
import Vistas.MenuPrincipal;
import Vistas.ModoJuego;
import Vistas.SalaEspera;
import Controles.GameController;
import Vistas.GameView;
import Controles.LobbyController;
import Controles.RegisterController;
import Vistas.RegisterView;
import Vistas.ScoreboardView;
import Dominio.LobbyState;
import Red.NetworkLayer;
import Controles.GameSession;
 
import javax.swing.*;
import java.awt.*;
 
/**
 * La ventana principal de la aplicación.
 *
 * <p>Es el único {@link JFrame} del programa. Usa {@link CardLayout}
 * para cambiar entre pantallas sin abrir nuevas ventanas.
 *
 * <p><b>Corrección:</b> Ahora recibe y propaga el {@link LobbyState}
 * y la {@link NetworkLayer} creados en {@link RegisterController},
 * evitando que {@link LobbyController} cree instancias nuevas que
 * rompían la sincronización entre Host y Peer.
 */
public class MainWindow extends JFrame {
 
    public static final String SCREEN_REGISTER   = "register";
    public static final String SCREEN_LOBBY      = "lobby";
    public static final String SCREEN_GAME       = "game";
    public static final String SCREEN_CONFIGURATION      = "configuracionLobby";
    public static final String SCREEN_SELECTIONGAMEMODE      = "GameMode";
    public static final String SCREEN_SCOREBOARD = "scoreboard";
 
    private final CardLayout cardLayout; //java.awt
    private final JPanel     screenContainer;
 
    /** Sesión del jugador local. Se asigna al completar el registro. */
    private GameSession  session;
 
    /**
     * NetworkLayer creada en RegisterController y pasada al lobby.
     * Se guarda aquí para que GameController también pueda acceder a ella.
     */
    private NetworkLayer networkLayer;
    private LobbyState  lobbyState;
 
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
//      RegisterController registerController = new RegisterController(registerView, this); pantallas de este repo
//      registerView.setController(registerController); pantallas de este repo
        
        MenuPrincipal MenuView = new MenuPrincipal();
        RegisterController registerController = new RegisterController(MenuView, this);

        MenuView.setController(registerController); // pantallas del primer repo
        
//        screenContainer.add(registerView, SCREEN_REGISTER); pantallas de este repo

        screenContainer.add(MenuView, SCREEN_REGISTER);
 
        screenContainer.add(new JPanel(), SCREEN_LOBBY);
        screenContainer.add(new JPanel(), SCREEN_GAME);
        screenContainer.add(new JPanel(), SCREEN_SCOREBOARD);
 
        cardLayout.show(screenContainer, SCREEN_REGISTER);
    }
    //pantalla donde selecciona el tamanho de sala
    public void showRoomConfiguration(GameSession session, LobbyState lobbyState, NetworkLayer network){
        this.session      = session;
        this.networkLayer = network;
        this.lobbyState   = lobbyState;
        
        CrearSala configurationView = new CrearSala();
        LobbyController lobbyController = new LobbyController(
                configurationView, this, session, lobbyState, network);
        configurationView.setController(lobbyController);
        
        screenContainer.add(configurationView, SCREEN_CONFIGURATION);
        cardLayout.show(screenContainer, SCREEN_CONFIGURATION);
 
//        lobbyController.initialize();
    }
    //quite los parametros para ver que pasa GameSession session, LobbyState lobbyState, NetworkLayer network
    public void showSelectGameMode(String nombre, String avatar){
//        this.session      = session;
//        this.networkLayer = network;
//        this.lobbyState   = lobbyState;
        
        ModoJuego gameMode = new ModoJuego();
        GameModeController gameModeController = new GameModeController(gameMode, this, nombre, avatar);
        gameMode.setController(gameModeController);
        screenContainer.add(gameMode,SCREEN_SELECTIONGAMEMODE);
        cardLayout.show(screenContainer, SCREEN_SELECTIONGAMEMODE);
        
    }
                 
 
    /**
     * Navega al lobby con la sesión, el estado de sala y la red ya creados.
     *
     * <p><b>Por qué recibe LobbyState y NetworkLayer:</b><br>
     * Ambos se crean en {@link RegisterController} donde se conoce el rol
     * (Host o Peer) y se establece la conexión. Si LobbyController los
     * creara de nuevo, el Peer tendría un LobbyState con código distinto
     * al del Host, y la NetworkLayer no estaría conectada.
     *
     * @param session    La sesión del jugador registrado.
     * @param lobbyState El estado de sala ya inicializado.
     * @param network    La capa de red ya iniciada.
     */
    public void showLobby(GameSession session, LobbyState lobbyState, NetworkLayer network) {
        this.session      = session;
        this.networkLayer = network;
        this.lobbyState   = lobbyState;
        SalaEspera lobbyView = new SalaEspera();
        
//        LobbyView       lobbyView       = new LobbyView();
        LobbyController lobbyController = new LobbyController(
                lobbyView, this, session, lobbyState, network);
        lobbyView.setController(lobbyController);
 
        screenContainer.add(lobbyView, SCREEN_LOBBY);
        cardLayout.show(screenContainer, SCREEN_LOBBY);
 
        lobbyController.initialize();
    }
 
    /**
     * Navega a la mesa de juego.
     */
    public void showGame() {
        GameView       gameView       = new GameView();
        GameController gameController = new GameController(
                gameView, this, session, networkLayer, lobbyState);
        gameView.setController(gameController);
 
        screenContainer.add(gameView, SCREEN_GAME);
        cardLayout.show(screenContainer, SCREEN_GAME);
 
        gameController.initialize();
    }
 
    /**
     * Navega al scoreboard al terminar la partida.
     *
     * @param winnerName El nombre del jugador ganador.
     */
    public void showScoreboard(String winnerName) {
        ScoreboardView scoreboardView = new ScoreboardView(winnerName, session, this);
        screenContainer.add(scoreboardView, SCREEN_SCOREBOARD);
        cardLayout.show(screenContainer, SCREEN_SCOREBOARD);
    }
 
    /**
     * Vuelve a la pantalla de registro y limpia el estado anterior.
     * Se llama cuando el jugador sale del lobby o termina la partida.
     */
    public void showRegister() {
        if (networkLayer != null) {
            networkLayer.shutdown();
        }
        // Limpiar todos los listeners del EventBus para que la nueva partida
        // empiece desde cero sin listeners duplicados de la partida anterior.
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
