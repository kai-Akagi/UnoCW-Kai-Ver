package Controles;

import Logica.GameModel;
import Eventos.*;
import Eventos.EventBus;
import Main.MainWindow;
import Dominio.Card;
import Dominio.GameState;
import Dominio.LobbyState;
import Red.NetworkLayer;
import Controles.GameSession;
import Vistas.GameView;
import Vistas.GameViewModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller de la pantalla del juego.
 *
 * <p>
 * <b>Rol en MVC: Controller</b><br>
 * Tiene dos responsabilidades:
 * <ol>
 * <li>Recibir acciones del usuario desde {@link GameView} (hacer clic en una
 * carta, robar, gritar UNO).</li>
 * <li>Escuchar eventos del {@link EventBus} (cambios de turno, cartas jugadas
 * por otros) y pedirle a la View que se actualice.</li>
 * </ol>
 *
 * <p>
 * <b>Diferencia clave con el LobbyController:</b><br>
 * En el juego, el Controller también accede al {@link GameModel} local para
 * construir el {@link GameViewModel}. No lo modifica directamente (las
 * modificaciones pasan por el EventBus), pero sí lo lee para obtener el estado
 * actual y preparar los datos para la View.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameController {

    private final GameView view;
    private final MainWindow mainWindow;
    private final GameSession session;
    private final EventBus eventBus;
    private final GameModel gameModel;
    private final NetworkLayer networkLayer;
    private final LobbyState lobbyState;

    /**
     * Construye el Controller del juego.
     *
     * @param view La View del juego.
     * @param mainWindow La ventana principal para navegación.
     * @param session La sesión del jugador local.
     * @param networkLayer La capa de red ya conectada.
     * @param lobbyState El estado de sala con la lista de jugadores.
     */
    public GameController(GameView view, MainWindow mainWindow,
            GameSession session, NetworkLayer networkLayer,
            LobbyState lobbyState) {
        this.view = view;
        this.mainWindow = mainWindow;
        this.session = session;
        this.networkLayer = networkLayer;
        this.lobbyState = lobbyState;
        this.eventBus = EventBus.getInstance();
        this.gameModel = new GameModel();
    }

    /**
     * Inicializa el juego: suscribe listeners y, si somos el Host, arranca la
     * partida en el GameModel.
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
        // Inicializar el conteo de cartas de oponentes para el Peer.
        // Todos empiezan con 7 cartas; el mapa se actualiza durante el juego.
        if (!session.isHost()) {
            for (Dominio.Player p : lobbyState.getConnectedPlayers()) {
                if (!p.getName().equals(session.getLocalPlayer().getName())) {
                    peerOpponentHandSizes.put(p.getName(), 7);
                }
            }
        }

        // Los Peers esperan CardDrawnPrivateEvent (sus cartas) y
        // TurnChangedEvent (quién empieza) que llegarán del Host por la red.
    }

    // ─────────────────────────────────────────────
    // Acciones del usuario → vienen de la View
    // ─────────────────────────────────────────────
    /**
     * Se llama cuando el jugador hace clic en una carta de su mano. Publica la
     * intención en el EventBus local. La NetworkLayer la llevará al Host para
     * validación.
     *
     * @param card La carta en la que hizo clic.
     */
    public void onCardClicked(Card card) {
        view.disableHand();
        view.stopTurnTimer();
        if (card.getColor() == Card.Color.WILD) {
            // Para comodines: pedir color primero, luego enviar jugada.
            Card.Color chosen = showColorChooser();
            if (chosen == null) {
                // El jugador canceló: restaurar la vista con el último estado conocido.
                // buildCurrentViewModel() solo funciona para el Host (necesita GameState),
                // así que usamos lastViewModel que funciona para Host y Peer.
                GameViewModel restore = lastViewModel != null
                        ? lastViewModel : buildCurrentViewModel();
                if (restore != null) {
                    view.render(restore);
                }
                return;
            }
            // Enviar el color elegido al Host ANTES que la jugada.
            // La carta se elimina de la mano local solo cuando el Host confirma
            // via CardPlayedEvent (broadcast). Si el Host rechaza, envía
            // CARD_REJECTED y NetworkLayer restaura la carta y re-habilita la vista.
            eventBus.publish(GameEventFactory.colorChosen(
                    session.getLocalPlayer(), chosen));
            eventBus.publish(GameEventFactory.cardPlayed(
                    session.getLocalPlayer(), card));
        } else {
            eventBus.publish(
                    GameEventFactory.cardPlayed(session.getLocalPlayer(), card)
            );
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
                mainWindow,
                "Elige el color que continuará:",
                "¿Qué color?",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );
        switch (choice) {
            case 0:
                return Card.Color.RED;
            case 1:
                return Card.Color.BLUE;
            case 2:
                return Card.Color.GREEN;
            case 3:
                return Card.Color.YELLOW;
            default:
                return null;
        }
    }

    /**
     * Construye el ViewModel actual para el jugador local (Host) o null si no
     * aplica.
     */
    private GameViewModel buildCurrentViewModel() {
        if (gameModel.getGameState() == null) {
            return null;
        }
        return new GameViewModel(gameModel.getGameState(), session.getLocalPlayer());
    }

    /**
     * Se llama cuando el jugador presiona el botón de robar carta.
     *
     * <p>
     * Publica una <b>intención</b> de robo, no el robo en sí. El Peer no sabe
     * qué carta saldrá del mazo: esa decisión la toma el Host. La NetworkLayer
     * lleva esta intención al Host, quien ejecuta el robo real en su GameModel
     * y devuelve el resultado vía {@link Eventos.CardDrawnPrivateEvent} (al que
     * robó) y {@link Eventos.CardDrawnPublicEvent} (a todos los demás).
     */
    public void onDrawClicked() {
        eventBus.publish(
                GameEventFactory.drawCardRequest(session.getLocalPlayer())
        );
    }

    /**
     * Se llama cuando el jugador presiona el botón "Abandonar". Muestra
     * confirmación. Si el Host abandona termina la partida para todos. Si un
     * Peer abandona, notifica al Host y navega al registro.
     */
    public void onLeaveClicked() {
        int choice = JOptionPane.showConfirmDialog(
                mainWindow,
                "¿Estás seguro de que deseas abandonar la partida?",
                "Abandonar partida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        view.stopTurnTimer();

        if (session.isHost()) {
            // Notificar a todos con nombre especial "HOST" para que sepan
            // que la partida termina, no solo que un jugador salió
            networkLayer.broadcast(
                    "{\"type\":\"PLAYER_LEFT\",\"player\":\"HOST\"}\n");
            SwingUtilities.invokeLater(mainWindow::showRegister);
        } else {
            // El listener de NetworkLayer enviará PLAYER_LEFT al Host
            eventBus.publish(GameEventFactory.playerDisconnected(
                    session.getLocalPlayer().getName()));
            SwingUtilities.invokeLater(mainWindow::showRegister);
        }
    }

    /**
     * Indica si estamos en el periodo de gracia de UNO (5 segundos para
     * declarar). El turno no avanza hasta que el jugador presione UNO o el
     * timer expire.
     */
    private boolean inUnoPeriod = false;

    /**
     * Último ViewModel renderizado. Permite restaurar la vista al cancelar un
     * diálogo.
     */
    private GameViewModel lastViewModel = null;

    /**
     * Mapa nombre→cartas para los oponentes del Peer. El Peer no tiene
     * GameState, así que mantiene este conteo localmente actualizando cuando
     * recibe TurnChanged y CardDrawnPrivate.
     */
    private final java.util.Map<String, Integer> peerOpponentHandSizes
            = new java.util.HashMap<>();

    /**
     * Se llama cuando el jugador presiona el botón "¡UNO!".
     */
    public void onUnoClicked() {
        if (inUnoPeriod) {
            // Periodo de gracia activo: declarar UNO termina el turno sin penalización
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
     *
     * <p>
     * Si es el turno del jugador local, se fuerza un robo de carta para pasar
     * el turno automáticamente, igual que si el jugador hubiera presionado
     * "Robar" manualmente.
     */
    public void onTurnTimerExpired() {
        if (inUnoPeriod) {
            // El timer de gracia UNO expiró sin que el jugador declarara → penalización
            inUnoPeriod = false;
            view.setUnoGraceMode(false);
            if (session.isHost()) {
                gameModel.onUnoTimerExpired();
            } else {
                eventBus.publish(new Eventos.UnoPenaltyEvent(session.getLocalPlayer()));
            }
        } else if (gameModel.getGameState() != null
                && gameModel.getGameState().getCurrentPlayer().getName()
                        .equals(session.getLocalPlayer().getName())) {
            onDrawClicked();
        } else if (!session.isHost() && lastViewModel != null && lastViewModel.isMyTurn) {
            // Peer: el timer expiró y es nuestro turno — forzar robo igual que el Host
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

        // Cambio de turno: usar los datos del evento directamente para evitar
        // cualquier posible condición de carrera al leer del GameState.
        eventBus.subscribe(TurnChangedEvent.class, event -> {
            TurnChangedEvent tce = (TurnChangedEvent) event;
            GameState state = gameModel.getGameState();
            if (state == null) {
                return;
            }

            Card evTopCard = tce.getTopCard();
            String topValue = evTopCard != null ? evTopCard.getValue() : "?";
            Card.Color topColor = evTopCard != null ? evTopCard.getColor() : Card.Color.WILD;
            // Mostrar el color activo cuando la carta es comodín
            if (topColor == Card.Color.WILD) {
                Card.Color ac = state.getActiveColor();
                if (ac != null && ac != Card.Color.WILD) {
                    topColor = ac;
                }
            }
            String currentName = tce.getCurrentPlayer().getName();
            boolean isMyTurn = currentName.equals(session.getLocalPlayer().getName());

            List<String> oppNames = new ArrayList<>();
            List<Integer> oppSizes = new ArrayList<>();
            for (Dominio.Player p : state.getPlayers()) {
                if (!p.getName().equals(session.getLocalPlayer().getName())) {
                    oppNames.add(p.getName());
                    oppSizes.add(p.getHandSize());
                }
            }
            List<String> oppAvatarIds = new ArrayList<>();
            for (Dominio.Player p : lobbyState.getConnectedPlayers()) {
                if (!p.getName().equals(session.getLocalPlayer().getName())) {
                    oppAvatarIds.add(p.getAvatarId());
                }
            }

            // Resolver el color activo (puede diferir de topColor cuando es comodín)
            Card.Color resolvedActive = state.getActiveColor();
            if (resolvedActive == null || resolvedActive == Card.Color.WILD) {
                resolvedActive = topColor;
            }
            final Card.Color finalActiveColor = resolvedActive;
            GameViewModel vm = new GameViewModel(
                    currentName,
                    topValue,
                    topColor,
                    finalActiveColor,
                    new ArrayList<>(session.getLocalPlayer().getHand()),
                    session.getLocalPlayer().getName(),
                    oppNames, oppSizes,
                    oppAvatarIds,
                    tce.isClockwise()
            );
            System.out.println("[GameController] TurnChanged → current='" + currentName
                    + "' local='" + session.getLocalPlayer().getName()
                    + "' isMyTurn=" + isMyTurn + " hand=" + vm.localHand.size());
            SwingUtilities.invokeLater(() -> {
                lastViewModel = vm;
                view.render(vm);
            });
        });

        // Cambio de turno recibido por la red (Peer).
        // El Host ya incluye el conteo exacto de cartas en el mensaje,
        // así que no necesitamos inferirlo.
        eventBus.subscribe(NetworkTurnChangedEvent.class, event -> {
            NetworkTurnChangedEvent e = (NetworkTurnChangedEvent) event;

            final String currentPlayerName = e.getCurrentPlayerName();
            final String topCardText = e.getTopCardText();
            final boolean eventClockwise = e.isClockwise();
            final java.util.Map<String, Integer> sizes = e.getHandSizes();

            // Actualizar peerOpponentHandSizes con los datos del Host (fuente de verdad)
            if (sizes != null) {
                sizes.forEach((name, count) -> {
                    if (!session.isLocalPlayer(name)) {
                        peerOpponentHandSizes.put(name, count);
                    }
                });
            }

            final boolean isLocalTurn = currentPlayerName.equals(session.getLocalPlayer().getName());
            // Red de seguridad: si llega nuestro turno y seguíamos en grace period,
            // limpiarlo para que la mano y el mazo queden habilitados.
            if (isLocalTurn && inUnoPeriod) {
                inUnoPeriod = false;
                SwingUtilities.invokeLater(() -> view.setUnoGraceMode(false));
            }

            final Dominio.Card.Color networkActiveColor = e.getActiveColor();
            SwingUtilities.invokeLater(() -> {
                Card parsedTopCard = parseCard(topCardText);
                String topValue = parsedTopCard != null ? parsedTopCard.getValue()
                        : topCardText;
                Card.Color topColor = parsedTopCard != null ? parsedTopCard.getColor()
                        : Card.Color.WILD;
                // Mostrar el color activo cuando la carta es comodín
                if (topColor == Card.Color.WILD && networkActiveColor != null
                        && networkActiveColor != Card.Color.WILD) {
                    topColor = networkActiveColor;
                }

                List<String> oppNames = new ArrayList<>();
                List<Integer> oppSizes = new ArrayList<>();
                List<String> oppAvatarIds = new ArrayList<>();
                for (Dominio.Player p : lobbyState.getConnectedPlayers()) {
                    if (!p.getName().equals(session.getLocalPlayer().getName())) {
                        oppNames.add(p.getName());
                        oppSizes.add(peerOpponentHandSizes.getOrDefault(p.getName(), 7));
                        oppAvatarIds.add(p.getAvatarId());
                    }
                }

                // activeColor ya fue resuelto arriba (networkActiveColor o topColor)
                Card.Color peerActiveColor = (networkActiveColor != null
                        && networkActiveColor != Card.Color.WILD)
                                ? networkActiveColor : topColor;
                GameViewModel vm = new GameViewModel(
                        currentPlayerName,
                        topValue,
                        topColor,
                        peerActiveColor,
                        new ArrayList<>(session.getLocalPlayer().getHand()),
                        session.getLocalPlayer().getName(),
                        oppNames, oppSizes,
                        oppAvatarIds,
                        eventClockwise
                );
                lastViewModel = vm;
                view.render(vm);
            });
        });

        // El Peer recibe una carta privada del Host (cartas iniciales o robadas)
        // y la agrega a la mano del jugador local.
        eventBus.subscribe(NetworkCardDrawnPrivateEvent.class, event -> {
            NetworkCardDrawnPrivateEvent e = (NetworkCardDrawnPrivateEvent) event;
            if (session.isLocalPlayer(e.getPlayerName())) {
                Card card = parseCard(e.getCardText());
                if (card != null) {
                    final Card cardToAdd = card;
                    SwingUtilities.invokeLater(()
                            -> session.getLocalPlayer().addCard(cardToAdd));
                }
            } else {
                // Una carta fue dada a un oponente (DrawTwo, DrawFour, etc.)
                // Actualizar el contador local del Peer para mostrarlo en la GUI.
                peerOpponentHandSizes.merge(e.getPlayerName(), 1, Integer::sum);
            }
        });

        // El jugador local quedó con 1 carta → periodo de gracia de 5 segundos.
        // Se verifica inUnoPeriod dentro del invokeLater para evitar que
        // setUnoGraceMode(true) se ejecute si el jugador ya declaró UNO
        // antes de que el invokeLater procesara (race condition en el EDT).
        eventBus.subscribe(UnoGracePeriodEvent.class, event -> {
            UnoGracePeriodEvent e = (UnoGracePeriodEvent) event;
            if (session.isLocalPlayer(e.getPlayer().getName())) {
                inUnoPeriod = true;
                SwingUtilities.invokeLater(() -> {
                    if (inUnoPeriod) {
                        view.setUnoGraceMode(true);
                    }
                });
            }
        });

        // Fin del juego
        eventBus.subscribe(GameOverEvent.class, event -> {
            GameOverEvent e = (GameOverEvent) event;
            SwingUtilities.invokeLater(()
                    -> mainWindow.showScoreboard(e.getWinner().getName())
            );
        });

        // Un jugador gritó UNO (feedback visual)
        eventBus.subscribe(UnoCalledEvent.class, event -> {
            UnoCalledEvent e = (UnoCalledEvent) event;
            SwingUtilities.invokeLater(()
                    -> JOptionPane.showMessageDialog(mainWindow,
                            "¡" + e.getPlayer().getName() + " gritó UNO!",
                            "UNO", JOptionPane.INFORMATION_MESSAGE)
            );
        });

        // Notificación de UNO recibida por la red (para los demás jugadores)
        eventBus.subscribe(NetworkUnoCalledEvent.class, event -> {
            NetworkUnoCalledEvent e = (NetworkUnoCalledEvent) event;
            if (!session.isLocalPlayer(e.getPlayerName())) {
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(mainWindow,
                                "¡" + e.getPlayerName() + " gritó UNO!",
                                "UNO", JOptionPane.INFORMATION_MESSAGE)
                );
            }
        });

        // Un jugador abandonó la partida en curso.
        eventBus.subscribe(PlayerDisconnectedEvent.class, event -> {
            PlayerDisconnectedEvent e = (PlayerDisconnectedEvent) event;
            String leavingName = e.getPlayerName();

            boolean leavingIsHost = lobbyState.getConnectedPlayers().stream()
                    .anyMatch(p -> p.getName().equals(leavingName) && p.isHost());
            if (leavingIsHost) {
                // El Host salió: todos los Peers regresan al registro
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(mainWindow,
                            "El Host abandonó la partida.",
                            "Partida terminada",
                            JOptionPane.INFORMATION_MESSAGE);
                    mainWindow.showRegister();
                });
                return;
            }

            // Un Peer abandonó: el Host actualiza su GameModel
            if (session.isHost() && gameModel.getGameState() != null) {
                gameModel.removePlayer(leavingName);
            }

            // Todos eliminan al jugador del LobbyState
            lobbyState.removePlayer(leavingName);

            // El Peer limpia su mapa y re-renderiza sin ese jugador
            if (!session.isHost()) {
                peerOpponentHandSizes.remove(leavingName);
                if (lastViewModel != null) {
                    SwingUtilities.invokeLater(() -> {
                        List<String> names = new ArrayList<>(lastViewModel.opponentNames);
                        List<Integer> sizes = new ArrayList<>(lastViewModel.opponentHandSizes);
                        int idx = names.indexOf(leavingName);
                        if (idx >= 0) {
                            names.remove(idx);
                            sizes.remove(idx);
                        }
                        List<String> updatedAvatarIds = new ArrayList<>();
                        if (lastViewModel.opponentAvatarIds != null) {
                            updatedAvatarIds.addAll(lastViewModel.opponentAvatarIds);
                            int avatarIdx = lastViewModel.opponentNames.indexOf(leavingName);
                            if (avatarIdx >= 0 && avatarIdx < updatedAvatarIds.size()) {
                                updatedAvatarIds.remove(avatarIdx);
                            }
                        }
                        GameViewModel updated = new GameViewModel(
                                lastViewModel.currentPlayerName,
                                lastViewModel.topCardValue,
                                lastViewModel.topCardColor,
                                lastViewModel.activeColor,
                                new ArrayList<>(session.getLocalPlayer().getHand()),
                                session.getLocalPlayer().getName(),
                                names, sizes,
                                updatedAvatarIds,
                                lastViewModel.clockwise
                        );
                        lastViewModel = updated;
                        view.render(updated);
                    });
                }
            }
        });

    }

    // ─────────────────────────────────────────────
    // Utilidades de parseo de red
    // ─────────────────────────────────────────────
    /**
     * Reconstruye un objeto {@link Card} a partir de su representación en
     * texto.
     *
     * <p>
     * El formato es "COLOR-VALUE" (ej. "RED-7", "BLUE-SKIP", "WILD_DRAW_FOUR").
     * Esto es necesario porque los sockets solo transportan texto, no objetos
     * Java.
     *
     * @param cardText El texto de la carta recibido por la red.
     * @return La carta reconstruida, o {@code null} si el formato es inválido.
     */
    private Card parseCard(String cardText) {
        if (cardText == null || cardText.isBlank()) {
            return null;
        }

        // Comodines no tienen guión de color
        if (cardText.equals("WILD")) {
            return new Card(Card.Color.WILD, "WILD", null);
        }
        if (cardText.equals("WILD_DRAW_FOUR")) {
            return new Card(Card.Color.WILD, "WILD_DRAW_FOUR", null);
        }

        // Formato: "COLOR-VALUE"
        int dashIdx = cardText.indexOf('-');
        if (dashIdx < 0) {
            return null;
        }

        String colorStr = cardText.substring(0, dashIdx);
        String value = cardText.substring(dashIdx + 1);

        Card.Color color = parseColor(colorStr);
        if (color == null) {
            return null;
        }

        return new Card(color, value, null);
        // Nota: el efecto no se reconstruye porque el Peer no aplica efectos.
        // Solo el Host ejecuta la lógica de reglas. El Peer solo muestra la carta.
    }

    /**
     * Convierte un texto de color al enum {@link Card.Color}.
     *
     * @param colorStr El color como texto (ej. "RED", "BLUE").
     * @return El color correspondiente, o {@code null} si no se reconoce.
     */
    private Card.Color parseColor(String colorStr) {
        switch (colorStr) {
            case "RED":
                return Card.Color.RED;
            case "BLUE":
                return Card.Color.BLUE;
            case "GREEN":
                return Card.Color.GREEN;
            case "YELLOW":
                return Card.Color.YELLOW;
            case "WILD":
                return Card.Color.WILD;
            default:
                return null;
        }
    }

}
