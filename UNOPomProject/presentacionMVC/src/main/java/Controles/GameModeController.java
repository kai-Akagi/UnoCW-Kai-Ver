package Controles;

import javax.swing.SwingUtilities;
import Main.MainWindow;
import Dominio.LobbyState;
import Dominio.Player;
import Red.NetworkLayer;
import Controles.GameSession;
import Vistas.ModoJuego;

public class GameModeController {

    private ModoJuego view;
    private MainWindow mainWindow;
    private String name, avatarId;

    public GameModeController(ModoJuego view, MainWindow mainWindow, String name, String avatarid) {
        this.view = view;
        this.mainWindow = mainWindow;
        this.avatarId = avatarid;
        this.name = name;
    }

    public void onContinueClicked() {
        Player localPlayer = new Player(name, avatarId, true);
        localPlayer.setReady(true); // el Host siempre está listo

        LobbyState lobbyState = new LobbyState();
        lobbyState.addPlayer(localPlayer);

        GameSession session = new GameSession(localPlayer, true);
        session.setRoomCode(lobbyState.getRoomCode());

        NetworkLayer network = new NetworkLayer(session, lobbyState);
        network.startAsHost(NetworkLayer.DEFAULT_PORT);
        network.registerLobbyListeners();

//        SwingUtilities.invokeLater(() -> // aqui deberiamos de mostrar el configurar sala dejate mi intento abajo
//                mainWindow.showLobby(session, lobbyState, network));
        SwingUtilities.invokeLater(()
                -> // aqui deberiamos de mostrar el configurar sala
                mainWindow.showRoomConfiguration(session, lobbyState, network));
    }

}
