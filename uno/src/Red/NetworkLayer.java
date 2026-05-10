package Red;
import Eventos.TurnChangedEvent;
import Eventos.UnoCalledEvent;
import Eventos.PlayerReadyEvent;
import Eventos.PlayerDisconnectedEvent;
import Eventos.GameEventFactory;
import Eventos.DrawCardRequestEvent;
import Eventos.GameOverEvent;
import Eventos.ColorChosenEvent;
import Eventos.CardPlayedEvent;
import Eventos.CardDrawnPrivateEvent;
import Eventos.GameStartedEvent;
import Eventos.GameEvent;
import Logica.GameModel;

import Eventos.EventBus;
import Dominio.LobbyState;
import Dominio.Player;
import Controles.GameSession;
 
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
 
/**
 * Maneja toda la comunicación por sockets entre Host y Peers.
 * El Host abre el servidor y distribuye los mensajes; los Peers
 * se conectan y envían sus acciones al Host para que las valide.
 * Usa el EventBus para publicar los mensajes recibidos al resto
 * de la aplicación sin acoplarse a ningún componente específico.
 */
public class NetworkLayer {
 
    /** Puerto donde el Host escucha conexiones entrantes. */
    public static final int DEFAULT_PORT = 55555;
 
    private final EventBus    eventBus;
    private final GameSession session;
    private final LobbyState  lobbyState;
 
    private ServerSocket serverSocket;
 
    /**
     * Conexiones activas.
     * Host: una entrada por cada Peer conectado (clave = nombre del jugador).
     * Peer: una sola entrada con clave "HOST".
     */
    private final Map<String, PeerConnection> connections;
 
    private volatile boolean running;
 
    /**
     * Referencia al motor del juego.
     * El Host la usa para ejecutar acciones directamente (robar carta)
     * evitando publicar eventos que causarían bucles en el bus.
     */
    private GameModel gameModel;
 
    /**
     * Indica si los listeners del juego ya fueron registrados.
     * Evita doble suscripción si registerGameListeners() se llama más de una vez.
     */
    private boolean gameListenersRegistered = false;
 
    /**
     * Construye la capa de red.
     *
     * @param session    La sesión del jugador local.
     * @param lobbyState El estado de sala compartido.
     */
    public NetworkLayer(GameSession session, LobbyState lobbyState) {
        this.session     = session;
        this.lobbyState  = lobbyState;
        this.eventBus    = EventBus.getInstance();
        this.connections = new ConcurrentHashMap<>();
        this.running     = false;
    }
 
    // ─────────────────────────────────────────────
    // Arranque según rol
    // ─────────────────────────────────────────────
 
    /**
     * Inicia esta instancia como Host.
     * Abre el ServerSocket y espera conexiones en un hilo separado.
     *
     * @param port Puerto donde escuchar.
     */
    public void startAsHost(int port) {
        running = true;
        Thread acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("[Host] Escuchando en puerto " + port);
 
                while (running) {
                    Socket incoming = serverSocket.accept();
                    System.out.println("[Host] Conexión entrante de: "
                            + incoming.getInetAddress().getHostAddress());
                    handleIncomingConnection(incoming);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[Host] Error en ServerSocket: " + e.getMessage());
                }
            }
        }, "host-accept-thread");
 
        acceptThread.setDaemon(true);
        acceptThread.start();
    }
 
    /**
     * Conecta esta instancia al Host como Peer.
     *
     * @param hostIp   IP del Host.
     * @param hostPort Puerto del Host.
     * @throws IOException Si no se puede conectar.
     */
    public void connectToHost(String hostIp, int hostPort) throws IOException {
        running = true;
        System.out.println("[Peer] Conectando a " + hostIp + ":" + hostPort);
 
        Socket socket = new Socket(hostIp, hostPort);
        System.out.println("[Peer] Conexión establecida.");
 
        PeerConnection hostConn = new PeerConnection("HOST", socket, this::onMessageReceived);
        connections.put("HOST", hostConn);
        hostConn.startListening();
 
        // Enviarse al Host para que registre a este jugador en el lobby
        presentToHost(hostConn);
    }
 
    /**
     * Envía los datos del jugador local al Host como mensaje de presentación.
     * Este es el primer mensaje que el Peer envía al conectarse.
     *
     * @param hostConn La conexión con el Host.
     */
    private void presentToHost(PeerConnection hostConn) {
        Player local   = session.getLocalPlayer();
        String message = MessageSerializer.serialize(
                GameEventFactory.playerJoined(local));
        System.out.println("[Peer] Enviando presentación: " + message);
        hostConn.sendMessage(message);
    }
 
    // ─────────────────────────────────────────────
    // Suscripciones al EventBus local
    // ─────────────────────────────────────────────
 
    /**
     * Suscribe los listeners del lobby: cambios de estado "Listo" y eventos de conexión.
     * Se llama una vez al conectarse, antes de navegar al lobby.
     */
    public void registerLobbyListeners() {
        // El jugador local cambió estado "Listo" → enviar al Host
        // Solo los Peers envían su estado listo al Host.
        // El Host NO se envía a sí mismo por socket: causaría un bucle infinito
        // porque publishToLocalBus volvería a publicar PlayerReadyEvent en el bus
        // local, que dispararía este mismo listener indefinidamente.
        eventBus.subscribe(PlayerReadyEvent.class, event -> {
            PlayerReadyEvent e = (PlayerReadyEvent) event;
            if (!session.isHost() && session.isLocalPlayer(e.getPlayerName())) {
                sendToHost(MessageSerializer.serialize(e));
            } else if (session.isHost() && session.isLocalPlayer(e.getPlayerName())) {
                // El Host cambió su estado listo → notificar a los Peers
                broadcast(MessageSerializer.serialize(e));
            }
        });
        
        
        // El jugador local salió del lobby → notificar al Host antes de cerrar la red.
        // Esto garantiza que el Host elimine al Peer de su LobbyState limpiamente,
        // en lugar de depender del cierre abrupto del socket.
        eventBus.subscribe(PlayerDisconnectedEvent.class, event -> {
            PlayerDisconnectedEvent e = (PlayerDisconnectedEvent) event;
            if (!session.isHost() && session.isLocalPlayer(e.getPlayerName())) {
                sendToHost(MessageSerializer.serialize(
                GameEventFactory.playerDisconnected(e.getPlayerName())));
            } else if (session.isHost() && session.isLocalPlayer(e.getPlayerName())) {
                broadcast(MessageSerializer.serialize(
                GameEventFactory.playerDisconnected(e.getPlayerName())));
            }
        });
 
        // Solo el Host hace broadcast de GameStartedEvent
        if (session.isHost()) {
            eventBus.subscribe(GameStartedEvent.class, event ->
                    broadcast(MessageSerializer.serialize((GameEvent) event)));
        }

        // Un Peer solicita al Host iniciar la partida antes de completar el cupo.
        // Solo los Peers envían esta solicitud; el Host la recibe y decide.
        if (!session.isHost()) {
            eventBus.subscribe(Eventos.StartRequestedEvent.class, event -> {
                Eventos.StartRequestedEvent e = (Eventos.StartRequestedEvent) event;
                if (session.isLocalPlayer(e.getRequesterName())) {
                    sendToHost(MessageSerializer.serialize(e));
                }
            });
        }
    }
 
    /** Registra los listeners del juego. Se llama al iniciar la partida. */
    public void registerGameListeners() {
        // Protección contra doble registro: si ya se registraron los listeners
        // del juego, no los registramos de nuevo para evitar que cada evento
        // se procese múltiples veces y cause bucles infinitos.
        if (gameListenersRegistered) return;
        gameListenersRegistered = true;
 
        // El jugador local jugó una carta → enviar al Host
        eventBus.subscribe(CardPlayedEvent.class, event -> {
            CardPlayedEvent e = (CardPlayedEvent) event;
            if (session.isLocalPlayer(e.getPlayer().getName())) {
                sendToHost(MessageSerializer.serialize(e));
            }
        });
        
        // El jugador local eligió un color -> enviar al Host
        eventBus.subscribe(ColorChosenEvent.class, event ->{
            ColorChosenEvent e = (ColorChosenEvent) event;
            if(session.isLocalPlayer(e.getPlayer().getName())){
                sendToHost(MessageSerializer.serialize(e));
            }
        });
 
        // El jugador local quiere robar → enviar intención al Host
        eventBus.subscribe(DrawCardRequestEvent.class, event -> {
            DrawCardRequestEvent e = (DrawCardRequestEvent) event;
            if (session.isLocalPlayer(e.getPlayer().getName())) {
                sendToHost(MessageSerializer.serialize(e));
            }
        });
 
        // El jugador local gritó UNO → enviar al Host
        eventBus.subscribe(UnoCalledEvent.class, event -> {
            UnoCalledEvent e = (UnoCalledEvent) event;
            if (session.isLocalPlayer(e.getPlayer().getName())) {
                sendToHost(MessageSerializer.serialize(e));
            }
        });
 
        // El Peer no declaró UNO a tiempo → enviar penalización al Host.
        // Solo el Peer la envía; el Host la procesa directamente en su GameModel.
        if (!session.isHost()) {
            eventBus.subscribe(Eventos.UnoPenaltyEvent.class, event -> {
                Eventos.UnoPenaltyEvent e = (Eventos.UnoPenaltyEvent) event;
                if (session.isLocalPlayer(e.getPlayer().getName())) {
                    sendToHost(MessageSerializer.serializeUnoPenalty(e));
                }
            });
        }
 
        // Solo el Host hace broadcast o envíos privados de eventos del juego
        if (session.isHost()) {
            // Broadcast de carta jugada para que el Peer confirmado elimine la carta de su mano
            eventBus.subscribe(CardPlayedEvent.class, event ->
                    broadcast(MessageSerializer.serialize((GameEvent) event)));

            eventBus.subscribe(TurnChangedEvent.class, event -> {
                String base = MessageSerializer.serialize((GameEvent) event).trim();
                if (gameModel != null && gameModel.getGameState() != null) {
                    Dominio.GameState gs = gameModel.getGameState();
                    StringBuilder sizes = new StringBuilder();
                    for (Dominio.Player p : gs.getPlayers()) {
                        if (sizes.length() > 0) sizes.append(";");
                        sizes.append(p.getName()).append(":").append(p.getHandSize());
                    }
                    // Incluir el color activo para que el Peer muestre el color
                    // correcto cuando la carta activa es un comodín (WILD/WILD_DRAW_FOUR).
                    String activeColorStr = gs.getActiveColor() != null
                            ? gs.getActiveColor().name() : "";
                    if (base.endsWith("}")) {
                        base = base.substring(0, base.length() - 1)
                               + ",\"sizes\":\"" + sizes + "\""
                               + ",\"activeColor\":\"" + activeColorStr + "\"}\n";
                    }
                }
                broadcast(base);
            });
 
            eventBus.subscribe(GameOverEvent.class, event ->
                    broadcast(MessageSerializer.serialize((GameEvent) event)));
 
            // Periodo de gracia UNO: broadcast para que los demás jugadores vean
            // que un compañero está en el periodo de gracia.
            eventBus.subscribe(Eventos.UnoGracePeriodEvent.class, event ->
                    broadcast(MessageSerializer.serializeUnoGrace((Eventos.UnoGracePeriodEvent) event)));
 
            // Penalización UNO: el Peer le avisa al Host que su timer expiró.
            // El Host aplica la penalización y avanza el turno.
            eventBus.subscribe(Eventos.UnoPenaltyEvent.class, event -> {
                Eventos.UnoPenaltyEvent e = (Eventos.UnoPenaltyEvent) event;
                Player penalizedPlayer = findPlayerByName(e.getPlayer().getName());
                if (penalizedPlayer != null && gameModel != null) {
                    gameModel.onUnoTimerExpired();
                }
            });
 
            // Cartas privadas: solo al peer destinatario, nunca al Host mismo
            eventBus.subscribe(CardDrawnPrivateEvent.class, event -> {
                CardDrawnPrivateEvent e = (CardDrawnPrivateEvent) event;
                String targetName = e.getPlayer().getName();
                if (!session.isLocalPlayer(targetName)) {
                    sendPrivate(targetName, MessageSerializer.serialize(e));
                }
            });
        }
    }
 
   
 
    // ─────────────────────────────────────────────
    // Recepción y enrutamiento de mensajes
    // ─────────────────────────────────────────────
 
    /**
     * Recibe un mensaje y lo enruta según el rol: Host valida y reenvía, Peer publica en bus.
     *
     * @param senderName Nombre del peer que envió el mensaje.
     * @param message    Texto JSON recibido.
     */
    private void onMessageReceived(String senderName, String message) {
        System.out.println("[" + (session.isHost() ? "Host" : "Peer")
                + "] Mensaje recibido de '" + senderName + "': " + message);
 
        Map<String, String> fields = MessageSerializer.deserialize(message);
        String type = MessageSerializer.getType(fields);
 
        if (session.isHost()) {
            handleAsHost(type, fields, senderName);
        } else {
            publishToLocalBus(type, fields);
        }
    }
 
    /**
     * El Host procesa un mensaje entrante de un Peer.
     *
    	 * Para cada tipo de mensaje, el Host:
    
     * - Publica el evento en su bus local (para que GameModel lo procese).
     * - Decide si reenviar el resultado a otros peers.
    
     *
     * @param type       Tipo del mensaje.
     * @param fields     Campos del mensaje.
     * @param senderName Nombre del peer que lo envió.
     */
    private void handleAsHost(String type, Map<String, String> fields, String senderName) {
        switch (type) {
 
            case MessageSerializer.TYPE_PLAYER_JOINED:
                // Un nuevo peer se presentó. El Host lo registra en el LobbyState,
                // publica el evento en su bus local para actualizar su propio lobby,
                // y hace broadcast a los demás peers para que actualicen el suyo.
                Player newPlayer = new Player(
                        fields.get("player"),
                        fields.get("avatarId"),
                        false // los peers que se unen nunca son Host
                );
 
                // Validar que la sala no esté llena
                if (!lobbyState.hasSpace()) {
                    System.out.println("[Host] Sala llena. Rechazando: " + newPlayer.getName());
                    PeerConnection fullConn = connections.get(senderName);
                    if (fullConn != null) {
                        try {
                            fullConn.sendMessage("{\"type\":\"PLAYER_REJECTED\",\"reason\":\"La sala está llena\"}\n");
                            Thread.sleep(100);
                        } catch (Exception ex) { /* ignorar */ }
                        connections.remove(senderName);
                        fullConn.close();
                    }
                    return;
                }
 
                // Validar unicidad de nombre y avatar
                if (lobbyState.isNameTaken(newPlayer.getName())
                        || lobbyState.isAvatarTaken(newPlayer.getAvatarId())) {
                    System.out.println("[Host] Nombre o avatar duplicado. Rechazando: "
                            + newPlayer.getName());
                    // Notificar al Peer antes de cerrar la conexión.
                    // Sin este mensaje el Peer solo ve el socket cerrarse y no
                    // sabe por qué; con PLAYER_REJECTED puede mostrar un aviso.
                    PeerConnection rejectConn = connections.get(senderName);
                    if (rejectConn != null) {
                        try {
                            rejectConn.sendMessage("{\"type\":\"PLAYER_REJECTED\"," +
                                "\"reason\":\"Nombre o avatar ya en uso\"}\n");
                            Thread.sleep(100); // Dar tiempo a que el mensaje se envíe
                        } catch (Exception ex) { /* socket ya cerrado */ }
                        connections.remove(senderName);
                        rejectConn.close();
                    }
                    return;
                }
 
                lobbyState.addPlayer(newPlayer);
                System.out.println("[Host] Jugador registrado en lobby: " + newPlayer.getName()
                        + " | Total: " + lobbyState.getPlayerCount());
 
                // Publicar en el bus local del Host → LobbyController del Host lo ve
                eventBus.publish(GameEventFactory.playerJoined(newPlayer));
 
                // Broadcast a todos los otros peers para que actualicen su lista
                broadcastExcept(senderName, MessageSerializer.serialize(
                        GameEventFactory.playerJoined(newPlayer)));
 
                // Enviar al peer recién unido la lista actual de jugadores
                sendPrivate(senderName, MessageSerializer.serialize(
                        GameEventFactory.playerJoined(newPlayer)));
                sendCurrentLobbyState(senderName);
                // Enviar la capacidad configurada para que el peer muestre el tamaño correcto
                sendPrivate(senderName, "{\"type\":\"LOBBY_STATE\",\"capacity\":\""
                        + lobbyState.getCapacity() + "\"}\n");
                break;
 
            case MessageSerializer.TYPE_PLAYER_READY:
                // El Host procesa el cambio de estado "Listo" en su bus local
                // y hace broadcast a todos los peers para que actualicen su GUI.
                publishToLocalBus(type, fields);
                broadcastExcept(senderName, MessageSerializer.serializeFields(type, fields));
                break;
 
            case MessageSerializer.TYPE_CARD_PLAYED:
                // Ejecutar la jugada directamente en el GameModel del Host.
                // Si la jugada es inválida, notificar al Peer con CARD_REJECTED
                // para que restaure la carta en su mano y vuelva a habilitar su turno.
                if (gameModel != null) {
                    String cardStr  = fields.get("card");
                    Player cardPlyr = findPlayerByName(fields.get("player"));
                    if (cardPlyr != null && cardStr != null) {
                        boolean valid = false;
                        int dashIdx = cardStr.indexOf('-');
                        if (dashIdx > 0) {
                            Dominio.Card.Color col = parseCardColor(cardStr.substring(0, dashIdx));
                            String val = cardStr.substring(dashIdx + 1);
                            if (col != null) {
                                valid = gameModel.playCard(cardPlyr, new Dominio.Card(col, val, null));
                            }
                        } else {
                            valid = gameModel.playCard(cardPlyr,
                                new Dominio.Card(Dominio.Card.Color.WILD, cardStr, null));
                        }
                        if (!valid && gameModel.getGameState() != null) {
                            Dominio.GameState st = gameModel.getGameState();
                            String topText = st.getTopCard() != null ? st.getTopCard().toString() : "?";
                            String cw = String.valueOf(st.isClockwise());
                            sendPrivate(senderName,
                                "{\"type\":\"" + MessageSerializer.TYPE_CARD_REJECTED + "\""
                                + ",\"player\":\"" + fields.get("player") + "\""
                                + ",\"card\":\"" + cardStr + "\""
                                + ",\"topCard\":\"" + topText + "\""
                                + ",\"clockwise\":\"" + cw + "\"}\n");
                        }
                    }
                }
                break;
 
            case MessageSerializer.TYPE_DRAW_REQUEST:
                // Ejecutar el robo directamente en el GameModel del Host.
                // Mismo principio que CARD_PLAYED: evitar el bucle via bus.
                Player drwPlyr = findPlayerByName(fields.get("player"));
                if (drwPlyr != null && gameModel != null) {
                    gameModel.drawCard(drwPlyr);
                }
                break;
 
            case MessageSerializer.TYPE_COLOR_CHOSEN:
                // Aplicar el color elegido directamente en el GameState del Host.
                // Publicarlo en el bus local causaría que llegue de forma asíncrona
                // y podría procesarse DESPUÉS del CARD_PLAYED que llega a continuación.
                // Llamarlo directamente garantiza el orden correcto.
                if (gameModel != null && gameModel.getGameState() != null) {
                    String chosenColor = fields.get("color");
                    if (chosenColor != null) {
                        Dominio.Card.Color chosen = parseCardColor(chosenColor);
                        gameModel.getGameState().setActiveColor(chosen);
                        System.out.println("[Host] Color activo establecido: " + chosen);
                    }
                }
                break;
 
            case MessageSerializer.TYPE_UNO_CALLED:
                publishToLocalBus(type, fields);
                // Notificar a los demás peers que este jugador gritó UNO
                broadcastExcept(senderName, MessageSerializer.serializeFields(type, fields));
                // El Peer declaró UNO: avanzar el turno en el GameModel del Host
                if (gameModel != null) {
                    gameModel.onUnoDeclared();
                }
                break;

            case MessageSerializer.TYPE_UNO_PENALTY:
                // El Peer no declaró UNO a tiempo: aplicar penalización y avanzar turno
                if (gameModel != null) {
                    gameModel.onUnoTimerExpired();
                }
                break;

            case MessageSerializer.TYPE_START_REQUESTED:
                publishToLocalBus(type, fields);
                break;
 
            case MessageSerializer.TYPE_PLAYER_LEFT:
                lobbyState.removePlayer(fields.get("player"));
                eventBus.publish(GameEventFactory.playerDisconnected(fields.get("player")));
                broadcastExcept(senderName, MessageSerializer.serialize(
                        GameEventFactory.playerDisconnected(fields.get("player"))));
                break;
 
            default:
                System.out.println("[Host] Tipo de mensaje desconocido ignorado: " + type);
                break;
        }
    }
 
    /**
     * Convierte los campos del mensaje en un evento y lo publica en el bus local.
     * Usado tanto por el Host (para eventos de juego) como por el Peer (para todo).
     *
     * @param type   Tipo del mensaje.
     * @param fields Campos del mensaje.
     */
    private void publishToLocalBus(String type, Map<String, String> fields) {
        switch (type) {
            case MessageSerializer.TYPE_PLAYER_JOINED:
                // El Peer recibe notificación de que otro jugador se unió.
                // También restauramos el estado "Listo" para que el Peer
                // vea correctamente si el Host ya estaba listo.
                Player joined = new Player(
                        fields.get("player"),
                        fields.getOrDefault("avatarId", "avatar_1"),
                        Boolean.parseBoolean(fields.getOrDefault("isHost", "false"))
                );
                joined.setReady(Boolean.parseBoolean(fields.getOrDefault("ready", "false")));
                if (!lobbyState.isNameTaken(joined.getName())) {
                    lobbyState.addPlayer(joined);
                }
                eventBus.publish(GameEventFactory.playerJoined(joined));
                break;
 
            case MessageSerializer.TYPE_PLAYER_READY:
                // Actualizar estado "Listo" en el LobbyState local del Peer
                boolean isReady = Boolean.parseBoolean(fields.get("ready"));
                lobbyState.getConnectedPlayers().stream()
                        .filter(p -> p.getName().equals(fields.get("player")))
                        .findFirst()
                        .ifPresent(p -> p.setReady(isReady));
                eventBus.publish(GameEventFactory.playerReady(
                        fields.get("player"), isReady));
                break;
 
            case MessageSerializer.TYPE_PLAYER_LEFT:
                String leftName = fields.get("player");
                if ("HOST".equals(leftName)) {
                    leftName = lobbyState.getConnectedPlayers().stream()
                        .filter(Dominio.Player::isHost)
                        .map(Dominio.Player::getName)
                        .findFirst().orElse("HOST");
                }
                eventBus.publish(GameEventFactory.playerDisconnected(leftName));
                lobbyState.removePlayer(leftName);
                break;
 
            case MessageSerializer.TYPE_LOBBY_STATE:
                // El Host cambió el tamaño de sala → actualizar lobbyState y refrescar vista
                String newCapStr = fields.get("capacity");
                if (newCapStr != null) {
                    try { lobbyState.setCapacity(Integer.parseInt(newCapStr)); }
                    catch (NumberFormatException ignored) {}
                }
                // Reutilizar PlayerJoinedEvent para disparar el refresh del contador en LobbyController
                if (!lobbyState.getConnectedPlayers().isEmpty()) {
                    eventBus.publish(GameEventFactory.playerJoined(
                        lobbyState.getConnectedPlayers().get(0)));
                }
                break;

            case MessageSerializer.TYPE_GAME_STARTED:
                eventBus.publish(GameEventFactory.gameStarted());
                break;
 
            case MessageSerializer.TYPE_TURN_CHANGED:
                java.util.Map<String, Integer> parsedSizes = null;
                String sizesStr = fields.get("sizes");
                if (sizesStr != null && !sizesStr.isEmpty()) {
                    parsedSizes = new java.util.HashMap<>();
                    for (String entry : sizesStr.split(";")) {
                        int colon = entry.indexOf(':');
                        if (colon > 0) {
                            try {
                                parsedSizes.put(entry.substring(0, colon),
                                    Integer.parseInt(entry.substring(colon + 1)));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                Dominio.Card.Color parsedActiveColor = null;
                String activeColorStr = fields.get("activeColor");
                if (activeColorStr != null && !activeColorStr.isEmpty()) {
                    parsedActiveColor = parseCardColor(activeColorStr);
                }
                eventBus.publish(GameEventFactory.networkTurnChanged(
                        fields.get("currentPlayer"),
                        fields.get("topCard"),
                        Boolean.parseBoolean(fields.getOrDefault("clockwise", "true")),
                        parsedSizes,
                        parsedActiveColor));
                break;

            case MessageSerializer.TYPE_CARD_PLAYED:
                // El Host confirmó que esta carta fue jugada exitosamente.
                // Solo el Peer (no el Host) elimina la carta: el Host ya la
                // eliminó internamente en GameModel.playCard().
                // Usamos removeCard() porque getHand() devuelve lista no modificable.
                if (!session.isHost() && session.isLocalPlayer(fields.get("player"))) {
                    String confirmedText = fields.get("card");
                    Dominio.Card.Color confColor = Dominio.Card.Color.WILD;
                    String confValue = confirmedText;
                    int confDash = confirmedText != null ? confirmedText.indexOf('-') : -1;
                    if (confDash > 0) {
                        confColor = parseCardColor(confirmedText.substring(0, confDash));
                        confValue = confirmedText.substring(confDash + 1);
                    }
                    Dominio.Card toRemove = null;
                    for (Dominio.Card c : session.getLocalPlayer().getHand()) {
                        if (c.getColor() == confColor && c.getValue().equals(confValue)) {
                            toRemove = c;
                            break;
                        }
                    }
                    if (toRemove != null) session.getLocalPlayer().removeCard(toRemove);
                }
                break;

            case MessageSerializer.TYPE_CARD_REJECTED:
                // El Host rechazó la jugada. La carta ya está en la mano del Peer
                // (no se elimina optimistamente), así que solo re-habilitamos la vista.
                eventBus.publish(GameEventFactory.networkTurnChanged(
                    fields.get("player"),
                    fields.get("topCard"),
                    Boolean.parseBoolean(fields.getOrDefault("clockwise", "true")),
                    null, null));
                break;
 
            case MessageSerializer.TYPE_CARD_DRAWN_PRIVATE:
                eventBus.publish(GameEventFactory.networkCardDrawnPrivate(
                        fields.get("player"),
                        fields.get("card")));
                break;
 
            case MessageSerializer.TYPE_DRAW_REQUEST:
                // El Host llama a GameModel.drawCard() directamente en vez de
                // publicar DrawCardRequestEvent en el bus. Publicarlo en el bus
                // causaría un bucle: el listener de registerGameListeners lo
                // interceptaría y llamaría a sendToHost() que volvería a llamar
                // a publishToLocalBus() indefinidamente.
                Player requester = findPlayerByName(fields.get("player"));
                if (requester != null && gameModel != null) {
                    gameModel.drawCard(requester);
                }
                break;
 
            case MessageSerializer.TYPE_GAME_OVER:
                // El Host terminó el juego. Publicamos GameOverEvent localmente
                // para que GameController muestre el scoreboard al Peer.
                eventBus.publish(GameEventFactory.gameOver(new Dominio.Player(fields.get("winner"), "", false)));
                break;
 
            case MessageSerializer.TYPE_UNO_CALLED:
                eventBus.publish(GameEventFactory.networkUnoCalled(fields.get("player")));
                break;
 
            case MessageSerializer.TYPE_UNO_GRACE:
                // Otro jugador entró al periodo de gracia — publicar localmente
                // para feedback visual en la GUI de todos los jugadores.
                eventBus.publish(new Eventos.UnoGracePeriodEvent(
                    new Dominio.Player(fields.get("player"), "", false)));
                break;
 
            case MessageSerializer.TYPE_UNO_PENALTY:
                // Un Peer envió su penalización UNO al Host.
                // El Host lo procesa en el listener de registerGameListeners.
                // Para el Peer que lo recibe por broadcast, no hay acción aquí.
                break;
 
            case MessageSerializer.TYPE_START_REQUESTED:
                // Un Peer solicitó al Host iniciar antes de completar el cupo.
                // Publicamos el evento en el bus del Host para que LobbyController lo maneje.
                eventBus.publish(GameEventFactory.startRequested(fields.get("player")));
                break;

            case "PLAYER_REJECTED":
                // El Host rechazó la conexión por nombre o avatar duplicado.
                // Mostramos un aviso al usuario y cerramos la red para que
                // el jugador pueda volver a intentarlo con datos diferentes.
                String rejReason = fields.getOrDefault("reason", "Nombre o avatar ya en uso");
                System.out.println("[Peer] Conexion rechazada: " + rejReason);
                SwingUtilities.invokeLater(() ->
                    javax.swing.JOptionPane.showMessageDialog(null,
                        rejReason + "\nPor favor elige un nombre y avatar diferentes.",
                        "Registro rechazado",
                        javax.swing.JOptionPane.WARNING_MESSAGE)
                );
                shutdown();
                break;
 
            default:
                System.out.println("[Peer] Tipo de mensaje ignorado: " + type);
                break;
        }
    }
 
    // ─────────────────────────────────────────────
    // Sincronización del estado del lobby al unirse
    // ─────────────────────────────────────────────
 
    /**
     * Envía al peer recién unido la lista completa de jugadores que ya están
     * en el lobby, para que su pantalla quede sincronizada desde el primer momento.
     *
    	 * Sin esto, el Peer solo vería a los jugadores que se unan DESPUÉS de él,
     * nunca a los que ya estaban.
     *
     * @param targetPeerName El nombre del peer que acaba de unirse.
     */
    private void sendCurrentLobbyState(String targetPeerName) {
        PeerConnection target = connections.get(targetPeerName);
        if (target == null) return;
 
        for (Player p : lobbyState.getConnectedPlayers()) {
            // No enviar al peer su propia información de vuelta
            if (p.getName().equals(targetPeerName)) continue;
 
            String msg = MessageSerializer.serialize(GameEventFactory.playerJoined(p));
            System.out.println("[Host] Enviando jugador existente a '"
                    + targetPeerName + "': " + p.getName());
            target.sendMessage(msg);
        }
    }
 
    // ─────────────────────────────────────────────
    // Envío de mensajes
    // ─────────────────────────────────────────────
 
    /**
     * Envía un mensaje a un peer específico por nombre.
     *
     * @param playerName El nombre del jugador destino.
     * @param message    El mensaje serializado.
     */
    public void sendPrivate(String playerName, String message) {
        PeerConnection conn = connections.get(playerName);
        if (conn != null) {
            conn.sendMessage(message);
        }
    }
 
    /**
     * Envía un mensaje a todos los peers conectados.
     *
     * @param message El mensaje serializado.
     */
    public void broadcast(String message) {
        connections.values().forEach(conn -> conn.sendMessage(message));
    }
 
    /**
     * Envía un mensaje a todos los peers EXCEPTO al indicado.
     * Útil para no reenviarle a quien originó el mensaje su propio evento.
     *
     * @param excludeName Nombre del peer a excluir.
     * @param message     El mensaje serializado.
     */
    public void broadcastExcept(String excludeName, String message) {
        connections.entrySet().stream()
                .filter(e -> !e.getKey().equals(excludeName))
                .forEach(e -> e.getValue().sendMessage(message));
    }
 
    /**
     * Envía un mensaje al Host.
     * Si somos el Host, lo procesamos localmente.
     *
     * @param message El mensaje serializado.
     */
    private void sendToHost(String message) {
        if (session.isHost()) {
            Map<String, String> fields = MessageSerializer.deserialize(message);
            publishToLocalBus(MessageSerializer.getType(fields), fields);
        } else {
            PeerConnection hostConn = connections.get("HOST");
            if (hostConn != null) {
                System.out.println("[Peer] Enviando al Host: " + message);
                hostConn.sendMessage(message);
            } else {
                System.err.println("[Peer] Sin conexión al Host. Mensaje descartado.");
            }
        }
    }
 
    // ─────────────────────────────────────────────
    // Manejo de conexiones entrantes (solo Host)
    // ─────────────────────────────────────────────
 
    /**
     * Acepta una conexión entrante y la registra con nombre temporal.
     * El nombre real se asigna cuando llega el mensaje de presentación.
     *
     * @param socket El socket aceptado.
     */
    private void handleIncomingConnection(Socket socket) {
        String tempName = "peer-" + connections.size();
        PeerConnection[] holder = new PeerConnection[1];
 
        holder[0] = new PeerConnection(tempName, socket,
                (sender, msg) -> {
                    Map<String, String> fields = MessageSerializer.deserialize(msg);
 
                    // Si es la presentación, actualizar el nombre real en el mapa
                    if (MessageSerializer.TYPE_PLAYER_JOINED.equals(
                            MessageSerializer.getType(fields))) {
                        String realName = fields.get("player");
                        connections.remove(tempName);
                        connections.put(realName, holder[0]);
                        holder[0].setName(realName);
                        System.out.println("[Host] Peer renombrado: "
                                + tempName + " → " + realName);
                    }
                    onMessageReceived(holder[0].getName(), msg);
                });
 
        connections.put(tempName, holder[0]);
        holder[0].startListening();
        System.out.println("[Host] Conexión registrada como: " + tempName);
    }
 
    // ─────────────────────────────────────────────
    // Cierre
    // ─────────────────────────────────────────────
 
    /**
     * Asigna el GameModel al NetworkLayer.
     * Debe llamarse desde GameController después de crear el GameModel.
     *
     * @param gameModel El motor del juego.
     */
    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
    }
 
    /**
     * Cierra todas las conexiones y detiene los hilos de escucha.
     */
    public void shutdown() {
        running = false;
        connections.values().forEach(PeerConnection::close);
        connections.clear();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        System.out.println("[NetworkLayer] Cerrada.");
    }
 
    // ─────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────
 
    /**
     * Busca un jugador en el LobbyState por su nombre.
     *
     * @param name El nombre a buscar.
     * @return El jugador encontrado, o {@code null} si no existe.
     */
    private Dominio.Card.Color parseCardColor(String s) {
        switch (s) {
            case "RED":    return Dominio.Card.Color.RED;
            case "BLUE":   return Dominio.Card.Color.BLUE;
            case "GREEN":  return Dominio.Card.Color.GREEN;
            case "YELLOW": return Dominio.Card.Color.YELLOW;
            default:       return Dominio.Card.Color.WILD;
        }
    }
 
    private Player findPlayerByName(String name) {
        if (name == null) return null;
        return lobbyState.getConnectedPlayers().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
 
    /** @return El número de peers conectados actualmente. */
    public int getConnectedPeerCount() { return connections.size(); }
}
