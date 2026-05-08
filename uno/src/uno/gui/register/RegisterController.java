package uno.gui.register;

import Vistas.MenuPrincipal;
import uno.events.PlayerJoinedEvent;
import uno.events.NetworkPlayerRejectedEvent;
import uno.events.bus.EventBus;
import uno.events.bus.GameEventListener;
import uno.gui.MainWindow;
import uno.model.LobbyState;
import uno.model.Player;
import uno.network.NetworkLayer;
import uno.session.GameSession;
 
import javax.swing.*;
import java.awt.*;
 
/**
 * Controller de la pantalla de registro.
 *
 * <p>Valida los datos del jugador, crea la {@link GameSession},
 * el {@link LobbyState} y la {@link NetworkLayer}, y los pasa
 * juntos a {@link MainWindow#showLobby} para que el lobby
 * los use directamente sin crear instancias nuevas.
 *
 * <p><b>Corrección:</b> Antes, {@code LobbyController} creaba su propio
 * {@code LobbyState}, perdiendo el código de sala del Host y provocando
 * que el Peer generara un código distinto al unirse.
 */
public class RegisterController {
 
//    private final RegisterView view;
    private final MainWindow   mainWindow;
    private final MenuPrincipal view;
 
//    public RegisterController(RegisterView view, MainWindow mainWindow) {
//        this.view       = view;
//        this.mainWindow = mainWindow;
//    }
        public RegisterController(MenuPrincipal view, MainWindow mainWindow) {
        this.view       = view;
        this.mainWindow = mainWindow;
    }
 
    // ─────────────────────────────────────────────
    // Acciones del usuario
    // ─────────────────────────────────────────────
 
    /**
     * Llamado cuando el usuario selecciona un avatar.
     *
     * @param avatarIndex Índice del avatar seleccionado.
     */
    public void onAvatarSelected(int avatarIndex) {
        view.highlightAvatar(avatarIndex);
//        view.showError(""); al escojer avatar sale un Joptionpane en blanco (en el jPanel MenuPrincipal)
    }
 
    /**
     * Llamado cuando el usuario presiona "SIGUIENTE".
     * Valida los datos y pregunta el rol.
     */
    public void onContinueClicked() {
        String name     = view.getPlayerName();
        String avatarId = view.getSelectedAvatarId();
 
        String nameError = validateName(name);
        if (nameError != null) {
            view.showError(nameError);
            return;
        }
        if (avatarId == null) {
            view.showError("Selecciona un avatar para continuar.");
            return;
        }
 
//        view.showError("");  al escojer avatar sale un Joptionpane en blanco (en el jPanel MenuPrincipal)
        name = name.trim().toLowerCase();
//        SwingUtilities.invokeLater(() -> // aqui deberiamos de mostrar el configurar sala
//                mainWindow.showSelectGameMode(user, avatarId));
        askRoleAndProceed(name, avatarId);
        
        
    }
    
    
 
    // ─────────────────────────────────────────────
    // Flujo de rol
    // ─────────────────────────────────────────────
 
    /**
     * Pregunta al jugador si quiere ser Host o Peer y navega al lobby.
     */
    private void askRoleAndProceed(String name, String avatarId) {
        Object[] options = { "Crear Sala (Host)", "Unirse a Partida" };
 
        int choice = JOptionPane.showOptionDialog(
                mainWindow,
                "¿Cómo quieres participar?",
                "Tipo de Juego",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );
 
        if (choice == 0) {
            
            proceedAsHost(name, avatarId);
        } else if (choice == 1) {
            proceedAsPeer(name, avatarId);
        }
    }
 
    /**
     * Crea la sesión como Host, abre el ServerSocket y navega al lobby.
     *
     * <p>El {@link LobbyState} se crea aquí y su código de sala se almacena
     * en la {@link GameSession}. Ambos se pasan a {@link MainWindow#showLobby}
     * para que el {@code LobbyController} los use directamente.
     */
    private void proceedAsHost(String name, String avatarId) {
        Player     localPlayer = new Player(name, avatarId, true);
        localPlayer.setReady(true); // el Host siempre está listo
 
        LobbyState  lobbyState = new LobbyState();
        lobbyState.addPlayer(localPlayer);
 
        GameSession session    = new GameSession(localPlayer, true);
        session.setRoomCode(lobbyState.getRoomCode());
 
        NetworkLayer network = new NetworkLayer(session, lobbyState);
        network.startAsHost(NetworkLayer.DEFAULT_PORT);
        network.registerLobbyListeners();
 
//        SwingUtilities.invokeLater(() -> // aqui deberiamos de mostrar el configurar sala dejate mi intento abajo
//                mainWindow.showLobby(session, lobbyState, network));
        
         SwingUtilities.invokeLater(() -> // aqui deberiamos de mostrar el configurar sala
                mainWindow.showRoomConfiguration(session, lobbyState, network));
    }
 
    /**
     * Pide la IP del Host al Peer, conecta y navega al lobby.
     *
     * <p>El {@link LobbyState} del Peer comienza vacío: se poblará
     * automáticamente cuando lleguen los eventos {@code PlayerJoinedEvent}
     * desde el Host a través de la red.
     *
     * <p>El código de sala que ingresa el Peer es solo referencial para el
     * usuario; la conexión real se hace por IP y puerto. El Host valida
     * que el código coincida antes de aceptar al Peer (fase futura).
     */
    private void proceedAsPeer(String name, String avatarId) {
       // IP: máximo 45 caracteres (suficiente para IPv6 completo)
        JTextField ipField = new JTextField("localhost", 15);
        ((javax.swing.text.AbstractDocument) ipField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String text,
                        javax.swing.text.AttributeSet attr)
                        throws javax.swing.text.BadLocationException {
                    if (fb.getDocument().getLength() + text.length() <= 45)
                        super.insertString(fb, offset, text, attr);
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text,
                        javax.swing.text.AttributeSet attr)
                        throws javax.swing.text.BadLocationException {
                    int current = fb.getDocument().getLength() - length;
                    if (current + text.length() <= 45)
                        super.replace(fb, offset, length, text, attr);
                }
            });
 
        // Codigo de sala: formato XXXX-XXXX (9 caracteres con guion).
        // - Solo letras y numeros (el guion se inserta automaticamente).
        // - Mayusculas automaticas al escribir.
        // - Al escribir el 5to caracter se agrega el guion automaticamente.
        JTextField codeField = new JTextField(10);
        ((javax.swing.text.AbstractDocument) codeField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                private boolean updating = false;
                @Override
                public void insertString(FilterBypass fb, int offset, String text,
                        javax.swing.text.AttributeSet attr)
                        throws javax.swing.text.BadLocationException {
                    replace(fb, offset, 0, text, attr);
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text,
                        javax.swing.text.AttributeSet attr)
                        throws javax.swing.text.BadLocationException {
                    if (updating) return;
                    String filtered = text.toUpperCase().replaceAll("[^A-Z0-9]", "");
                    if (filtered.isEmpty() && !text.isEmpty()) return;
                    updating = true;
                    try {
                        super.replace(fb, offset, length, filtered, attr);
                        String current = fb.getDocument()
                                .getText(0, fb.getDocument().getLength());
                        String base = current.replace("-", "");
                        if (base.length() > 8) base = base.substring(0, 8);
                        String formatted = base.length() > 4
                                ? base.substring(0, 4) + "-" + base.substring(4)
                                : base;
                        super.replace(fb, 0, fb.getDocument().getLength(),
                                formatted, attr);
                    } finally {
                        updating = false;
                    }
                }
            });
 
        JPanel panel = new JPanel(new GridLayout(4, 1, 4, 4));
        panel.add(new JLabel("IP del Host:"));
        panel.add(ipField);
        panel.add(new JLabel("Codigo de Sala:"));
        panel.add(codeField);
 
        int result = JOptionPane.showConfirmDialog(
                mainWindow, panel, "Unirse a Partida",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
 
        if (result != JOptionPane.OK_OPTION) return;
 
        String hostIp   = ipField.getText().trim();
        String roomCode = codeField.getText().trim().toUpperCase();
 
        if (hostIp.isBlank() || roomCode.isBlank()) {
            view.showError("Debes ingresar la IP y el código de sala.");
            return;
        }

 
        Player     localPlayer = new Player(name, avatarId, false);
        // El LobbyState del Peer comienza con el jugador local ya agregado.
        // Sin esto, el Peer nunca se vería a sí mismo en su propia lista del lobby.
        LobbyState lobbyState  = new LobbyState();
        lobbyState.addPlayer(localPlayer);
 
        GameSession session = new GameSession(localPlayer, false);
        session.setRoomCode(roomCode);
 
        NetworkLayer network = new NetworkLayer(session, lobbyState);
 
        new Thread(() -> {
            try {
                
 
                // Listeners de confirmación/rechazo: solo activos hasta recibir respuesta.
                // Usamos referencias a los listeners para poder desuscribirlos después
                // de procesar la primera respuesta, evitando acumulación en el EventBus
                // si el usuario reintenta varias veces (cada reintento crea una NetworkLayer
                // nueva pero comparte el mismo EventBus Singleton).
                final boolean[] handled = { false };
                final GameEventListener[] joinListener   = { null };
                final GameEventListener[] rejectListener = { null };
 
                joinListener[0] = joinEvent -> {
                    PlayerJoinedEvent e = (PlayerJoinedEvent) joinEvent;
                    // El Host reenvía nuestro propio PLAYER_JOINED como aceptación.
                    if (!handled[0] && e.getPlayer().getName().equals(name)) {
                        handled[0] = true;
                        // Desuscribir ambos listeners temporales — ya no se necesitan.
                        EventBus.getInstance().unsubscribe(PlayerJoinedEvent.class,
                            joinListener[0]);
                        EventBus.getInstance().unsubscribe(NetworkPlayerRejectedEvent.class,
                            rejectListener[0]);
                        // Registrar los listeners permanentes del lobby AQUÍ,
                        // solo cuando sabemos que el Host nos aceptó.
                        // Hacerlo antes causaría que queden huérfanos en el bus
                        // si el Host nos rechaza.
                        network.registerLobbyListeners();
                        SwingUtilities.invokeLater(() ->
                            mainWindow.showLobby(session, lobbyState, network));
                    }
                };
 
                rejectListener[0] = rejEvent -> {
                    if (!handled[0]) {
                        handled[0] = true;
                        // Desuscribir ambos listeners temporales.
                        EventBus.getInstance().unsubscribe(PlayerJoinedEvent.class,
                            joinListener[0]);
                        EventBus.getInstance().unsubscribe(NetworkPlayerRejectedEvent.class,
                            rejectListener[0]);
                        NetworkPlayerRejectedEvent e = (NetworkPlayerRejectedEvent) rejEvent;
                        SwingUtilities.invokeLater(() ->
                            view.showError(e.getReason()
                                + "Por favor elige otro nombre o avatar."));
                    }
                };
 
                EventBus.getInstance().subscribe(PlayerJoinedEvent.class,         joinListener[0]);
                EventBus.getInstance().subscribe(NetworkPlayerRejectedEvent.class, rejectListener[0]);
                
                
                network.connectToHost(hostIp, NetworkLayer.DEFAULT_PORT);
 
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    view.showError("No se pudo conectar: " + e.getMessage()));
            }
        }, "connect-thread").start();
    }
 
    // ─────────────────────────────────────────────
    // Validaciones
    // ─────────────────────────────────────────────
 
    /**
     * Valida el nombre del jugador.
     * Reglas: entre 3 y 15 caracteres, solo letras, números y espacios.
     *
     * @param name El nombre a validar.
     * @return Mensaje de error si es inválido, {@code null} si es válido.
     */
    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            return "El nombre no puede estar vacío.";
        }
        if (name.length() < 3 || name.length() > 15) {
            return "El nombre debe tener entre 3 y 15 caracteres.";
        }
        if (!name.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+")) {
            return "Solo se permiten letras, números y espacios.";
        }
        return null;
    }
}


