package uno.gui.game;

import uno.gamemodel.GameModel;
import uno.events.*;
import uno.events.bus.EventBus;
import uno.gui.MainWindow;
import uno.model.Card;
import uno.model.LobbyState;
import uno.network.NetworkLayer;
import uno.session.GameSession;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller de la pantalla del juego.
 *
 * <p><b>Rol en MVC: Controller</b><br>
 * Tiene dos responsabilidades:
 * <ol>
 *   <li>Recibir acciones del usuario desde {@link GameView} (hacer clic en
 *       una carta, robar, gritar UNO).</li>
 *   <li>Escuchar eventos del {@link EventBus} (cambios de turno, cartas
 *       jugadas por otros) y pedirle a la View que se actualice.</li>
 * </ol>
 */
public class GameController {

    private final GameView     view;
    private final MainWindow   mainWindow;
    private final GameSession  session;
    private final EventBus     eventBus;
    private final GameModel    gameModel;
    private final NetworkLayer networkLayer;
    private final LobbyState   lobbyState;

    /**
     * Indica si estamos en el periodo de gracia de UNO (5 segundos para declarar).
     * El turno no avanza hasta que el jugador presione UNO o el timer expire.
     */
    private boolean inUnoPeriod = false;

    /**
     * Construye el Controller del juego.
     *
     * @param view         La View del juego.
     * @param mainWindow   La ventana principal para navegación.
     * @param session      La sesión del jugador local.
     * @param networkLayer La capa de red ya conectada.
     * @param lobbyState   El estado de sala con la lista de jugadores.
     */
    public GameController(GameView view, MainWindow mainWindow,
                          GameSession session, NetworkLayer networkLayer,
                          LobbyState lobbyState) {
        this.view         = view;
        this.mainWindow   = mainWindow;
        this.session      = session;
        this.networkLayer = networkLayer;
        this.lobbyState   = lobbyState;
        this.eventBus     = EventBus.getInstance();
        this.gameModel    = new GameModel();
    }

    /**
     * Inicializa el juego: suscribe listeners y, si somos el Host,
     * arranca la partida en el GameModel.
     */
    public void initialize() {
        // Registrar primero los listeners de red del juego, ANTES de suscribir
        // los listeners de la GUI. Así cuando startGame() publique eventos,
        // la NetworkLayer ya está lista para hacer broadcast al Peer.
        networkLayer.registerGameListeners();

        // Pasar el GameModel a NetworkLayer para que pueda ejecutar
        // acciones directamente (como drawCard) sin publicar eventos
        // que causarían bucles en el bus local.
        networkLayer.setGameModel(gameModel);

        // Luego registrar los listeners de la GUI
        registerEventListeners();

        if (session.isHost()) {
            // startGame en hilo separado para no bloquear el EDT de Swing
            new Thread(() -> {
                gameModel.startGame(new ArrayList<>(lobbyState.getConnectedPlayers()));
            }, "start-game-thread").start();
        }
    }

    // ─────────────────────────────────────────────
    // Acciones del usuario → vienen de la View
    // ─────────────────────────────────────────────

    /**
     * Se llama cuando el jugador hace clic en una carta de su mano.
     *
     * @param card La carta en la que hizo clic.
     */
    public void onCardClicked(Card card) {
        view.disableHand();
        view.stopTurnTimer();
        if (card.getColor() == Card.Color.WILD) {
            Card.Color chosen = showColorChooser();
            if (chosen == null) {
                if (buildCurrentViewModel() != null) view.render(buildCurrentViewModel());
                return;
            }
            if (!session.isHost()) {
                session.getLocalPlayer().removeCard(card);
            }
            eventBus.publish(GameEventFactory.colorChosen(session.getLocalPlayer(), chosen));
            eventBus.publish(GameEventFactory.cardPlayed(session.getLocalPlayer(), card));
        } else {
            if (!session.isHost()) {
                session.getLocalPlayer().removeCard(card);
            }
            eventBus.publish(GameEventFactory.cardPlayed(session.getLocalPlayer(), card));
        }
    }

    /**
     * Muestra un diálogo para elegir el color tras jugar un comodín.
     *
     * @return El color elegido, o null si el jugador canceló.
     */
    private Card.Color showColorChooser() {
        Object[] options = {"🔴 Rojo", "🔵 Azul", "🟢 Verde", "🟡 Amarillo"};
        int choice = javax.swing.JOptionPane.showOptionDialog(
                null,
                "Elige el color que continuará:",
                "¿Qué color?",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );
        switch (choice) {
            case 0: return Card.Color.RED;
            case 1: return Card.Color.BLUE;
            case 2: return Card.Color.GREEN;
            case 3: return Card.Color.YELLOW;
            default: return null;
        }
    }

    /** Construye el ViewModel actual para el jugador local (Host). */
    private GameViewModel buildCurrentViewModel() {
        if (gameModel.getGameState() == null) return null;
        return new GameViewModel(gameModel.getGameState(), session.getLocalPlayer());
    }

    /**
     * Se llama cuando el jugador presiona el botón de robar carta.
     */
    public void onDrawClicked() {
        eventBus.publish(GameEventFactory.drawCardRequest(session.getLocalPlayer()));
    }

    /**
     * Se llama cuando el jugador presiona el botón "¡UNO!".
     */
    public void onUnoClicked() {
        if (inUnoPeriod) {
            inUnoPeriod = false;
            view.stopTurnTimer();
            view.setUnoGraceMode(false);
            eventBus.publish(GameEventFactory.unoCalled(session.getLocalPlayer()));
            if (session.isHost()) {
                gameModel.onUnoDeclared();
            }
        } else {
            eventBus.publish(GameEventFactory.unoCalled(session.getLocalPlayer()));
        }
    }

    /**
     * Se llama cuando el temporizador de turno llega a cero.
     */
    public void onTurnTimerExpired() {
        if (inUnoPeriod) {
            inUnoPeriod = false;
            view.setUnoGraceMode(false);
            if (session.isHost()) {
                gameModel.onUnoTimerExpired();
            } else {
                eventBus.publish(new uno.events.UnoPenaltyEvent(session.getLocalPlayer()));
            }
        } else if (gameModel.getGameState() != null &&
            gameModel.getGameState().getCurrentPlayer().getName()
                .equals(session.getLocalPlayer().getName())) {
            onDrawClicked();
        }
    }

    // ─────────────────────────────────────────────
    // Eventos del bus → actualizan la View
    // ─────────────────────────────────────────────

    /**
     * Suscribe este Controller a los eventos del juego que afectan la View.
     */
    private void registerEventListeners() {
        
        // Jugada inválida: rehabilitar la mano del jugador local si era su turno.
        // Solo el Host genera este evento, pero todos los jugadores lo reciben.
        // Solo actuamos si somos el jugador local cuya mano quedó deshabilitada.
        eventBus.subscribe(InvalidPlayEvent.class, event ->{
            if (gameModel.getGameState() != null &&
        gameModel.getGameState().getCurrentPlayer().getName()
            .equals(session.getLocalPlayer().getName())) {
                SwingUtilities.invokeLater(() ->
                    view.render(buildCurrentViewModel()));
            }
        });

        // Cambio de turno local (Host): reconstruir el ViewModel desde GameState
        eventBus.subscribe(TurnChangedEvent.class, event -> {
            if (gameModel.getGameState() != null) {
                GameViewModel vm = new GameViewModel(
                    gameModel.getGameState(),
                    session.getLocalPlayer()
                );
                System.out.println("[GameController] TurnChanged → current='"
                    + vm.currentPlayerName + "' local='"
                    + session.getLocalPlayer().getName()
                    + "' isMyTurn=" + vm.isMyTurn
                    + " hand=" + vm.localHand.size());
                SwingUtilities.invokeLater(() -> view.render(vm));
            }
        });

        // Cambio de turno recibido por la red (Peer).
        // Los conteos de cartas vienen directamente del Host — sin inferencia.
        // El Host es la única fuente de verdad; el Peer solo lee y renderiza.
        eventBus.subscribe(NetworkTurnChangedEvent.class, event -> {
            NetworkTurnChangedEvent e = (NetworkTurnChangedEvent) event;

            final String currentPlayerName        = e.getCurrentPlayerName();
            final String topCardText              = e.getTopCardText();
            final Map<String, Integer> handSizes  = e.getHandSizes();

            SwingUtilities.invokeLater(() -> {
                Card parsedTopCard = parseCard(topCardText);
                String topValue = parsedTopCard != null ? parsedTopCard.getValue()
                                                        : topCardText;
                Card.Color topColor = parsedTopCard != null ? parsedTopCard.getColor()
                                                            : Card.Color.WILD;

                // Leer conteos directamente del Host — sin cálculos ni aproximaciones
                List<String> oppNames = new ArrayList<>();
                List<Integer> oppSizes = new ArrayList<>();
                for (uno.model.Player p : lobbyState.getConnectedPlayers()) {
                    if (!p.getName().equals(session.getLocalPlayer().getName())) {
                        oppNames.add(p.getName());
                        oppSizes.add(handSizes.getOrDefault(p.getName(), 0));
                    }
                }

                GameViewModel vm = new GameViewModel(
                    currentPlayerName,
                    topValue,
                    topColor,
                    new ArrayList<>(session.getLocalPlayer().getHand()),
                    session.getLocalPlayer().getName(),
                    oppNames, oppSizes,
                    true
                );
                view.render(vm);
            });
        });

        // El Peer recibe una carta privada del Host (cartas iniciales o robadas)
        eventBus.subscribe(NetworkCardDrawnPrivateEvent.class, event -> {
            NetworkCardDrawnPrivateEvent e = (NetworkCardDrawnPrivateEvent) event;
            if (session.isLocalPlayer(e.getPlayerName())) {
                Card card = parseCard(e.getCardText());
                if (card != null) {
                    final Card cardToAdd = card;
                    SwingUtilities.invokeLater(() ->
                        session.getLocalPlayer().addCard(cardToAdd));
                }
            }
            // Los conteos de oponentes vienen del Host vía TURN_CHANGED — no se rastrean aquí.
        });

        // El jugador local quedó con 1 carta → periodo de gracia de 5 segundos
        eventBus.subscribe(UnoGracePeriodEvent.class, event -> {
            UnoGracePeriodEvent e = (UnoGracePeriodEvent) event;
            if (session.isLocalPlayer(e.getPlayer().getName())) {
                inUnoPeriod = true;
                SwingUtilities.invokeLater(() -> view.setUnoGraceMode(true));
            }
        });

        // Fin del juego
        eventBus.subscribe(GameOverEvent.class, event -> {
            GameOverEvent e = (GameOverEvent) event;
            SwingUtilities.invokeLater(() ->
                mainWindow.showScoreboard(e.getWinner().getName()));
        });

        // Un jugador gritó UNO (feedback visual)
        eventBus.subscribe(UnoCalledEvent.class, event -> {
            UnoCalledEvent e = (UnoCalledEvent) event;
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainWindow,
                    "¡" + e.getPlayer().getName() + " gritó UNO!",
                    "UNO", JOptionPane.INFORMATION_MESSAGE));
        });
    }

    // ─────────────────────────────────────────────
    // Utilidades de parseo de red
    // ─────────────────────────────────────────────

    /**
     * Reconstruye un objeto {@link Card} a partir de su representación en texto.
     *
     * @param cardText El texto de la carta recibido por la red.
     * @return La carta reconstruida, o {@code null} si el formato es inválido.
     */
    private Card parseCard(String cardText) {
        if (cardText == null || cardText.isBlank()) return null;

        if (cardText.equals("WILD")) {
            return new Card(Card.Color.WILD, "WILD", null);
        }
        if (cardText.equals("WILD_DRAW_FOUR")) {
            return new Card(Card.Color.WILD, "WILD_DRAW_FOUR", null);
        }

        int dashIdx = cardText.indexOf('-');
        if (dashIdx < 0) return null;

        String colorStr = cardText.substring(0, dashIdx);
        String value    = cardText.substring(dashIdx + 1);

        Card.Color color = parseColor(colorStr);
        if (color == null) return null;

        return new Card(color, value, null);
    }

    /**
     * Convierte un texto de color al enum {@link Card.Color}.
     *
     * @param colorStr El color como texto (ej. "RED", "BLUE").
     * @return El color correspondiente, o {@code null} si no se reconoce.
     */
    private Card.Color parseColor(String colorStr) {
        switch (colorStr) {
            case "RED":    return Card.Color.RED;
            case "BLUE":   return Card.Color.BLUE;
            case "GREEN":  return Card.Color.GREEN;
            case "YELLOW": return Card.Color.YELLOW;
            case "WILD":   return Card.Color.WILD;
            default:       return null;
        }
    }
}