package Vistas;

import Controles.LobbyController;
import Dominio.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Sala de espera (lobby) antes de iniciar la partida.
 *
 * <p>
 * Construida íntegramente con layouts nativos de Swing (BorderLayout,
 * BoxLayout, FlowLayout) — sin AbsoluteLayout ni dependencias externas. Esto
 * garantiza que la GUI escale correctamente a cualquier tamaño de ventana.
 *
 * <p>
 * <b>Corrección de nombre extenso:</b> Los JLabel de la lista de jugadores usan
 * {@code setMinimumSize} con ancho suficiente y {@code setToolTipText} para
 * mostrar el nombre completo sin wrapping artificial. El texto nunca se corta
 * pero tampoco introduce saltos de línea donde no los hay.
 *
 * <p>
 * <b>Rol en MVC: View</b><br>
 * Solo dibuja. El {@link LobbyController} decide qué mostrar y cuándo habilitar
 * o deshabilitar botones.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class SalaEspera extends JPanel {

    // ─── Header ──────────────────────────────────────────────────────────────
    private final JLabel roomCodeLabel;
    private final JLabel playerCountLabel;
    private final JLabel waitingStatusLabel;

    // ─── Lista de jugadores ──────────────────────────────────────────────────
    private final JPanel playerListPanel;

    // ─── Botones ─────────────────────────────────────────────────────────────
    private final JButton readyButton;
    private final JButton startButton;
    private final JButton requestStartButton;
    private final JButton leaveButton;

    private LobbyController controller;

    /**
     * Construye la sala de espera.
     */
    public SalaEspera() {
        this.roomCodeLabel = new JLabel("Código: ----");
        this.playerCountLabel = new JLabel("Jugadores: 0/?");
        this.waitingStatusLabel = new JLabel("Esperando jugadores...");
        this.playerListPanel = new JPanel();
        this.readyButton = new JButton("✓ Estoy Listo");
        this.startButton = new JButton("▶ Iniciar Partida");
        this.requestStartButton = new JButton("▶ Solicitar Inicio");
        this.leaveButton = new JButton("← Salir");
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 46));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────
    // Construcción de sub-paneles
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(6, 4));
        header.setBackground(new Color(30, 30, 46));

        // Título
        JLabel title = new JLabel("Sala de Espera", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        // Código de sala (amarillo)
        roomCodeLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        roomCodeLabel.setForeground(new Color(248, 216, 71));
        roomCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Contador de jugadores
        playerCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playerCountLabel.setForeground(new Color(148, 163, 184));
        playerCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Estado de espera
        waitingStatusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        waitingStatusLabel.setForeground(new Color(100, 160, 220));
        waitingStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(new Color(30, 30, 46));
        center.add(roomCodeLabel);
        center.add(Box.createVerticalStrut(2));
        center.add(playerCountLabel);
        center.add(Box.createVerticalStrut(2));
        center.add(waitingStatusLabel);

        header.add(title, BorderLayout.NORTH);
        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private JScrollPane buildList() {
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(new Color(22, 22, 35));
        playerListPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 72)));

        JScrollPane scroll = new JScrollPane(playerListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(22, 22, 35));
        return scroll;
    }

    private JPanel buildControls() {
        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setBackground(new Color(30, 30, 46));
        controls.setBorder(new EmptyBorder(10, 0, 0, 0));

        styleButton(readyButton, new Color(34, 197, 94));
        styleButton(startButton, new Color(59, 130, 246));
        styleButton(requestStartButton, new Color(234, 179, 8));
        styleButton(leaveButton, new Color(71, 85, 105));

        startButton.setEnabled(false);
        requestStartButton.setEnabled(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(new Color(30, 30, 46));
        right.add(readyButton);
        right.add(requestStartButton);
        right.add(startButton);

        controls.add(leaveButton, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);
        return controls;
    }

    // ─────────────────────────────────────────────
    // Métodos que el Controller llama para actualizar la View
    // ─────────────────────────────────────────────
    /**
     * Actualiza la lista visual de jugadores conectados.
     *
     * <p>
     * <b>Corrección de nombre extenso:</b> El JLabel del nombre nunca hace
     * wrapping porque tiene HTML desactivado y usa {@code setMinimumSize} con
     * un ancho mínimo garantizado. Nombres dentro del límite de 15 caracteres
     * siempre caben en una sola línea.
     *
     * @param players Lista actual de jugadores.
     * @param localName Nombre del jugador local (para resaltarlo).
     * @param isHost Si el jugador local es Host.
     */
    public void updatePlayerList(List<Player> players, String localName, boolean isHost) {
        playerListPanel.removeAll();

        for (Player p : players) {
            JPanel row = buildPlayerRow(p, localName.equals(p.getName()), isHost);
            playerListPanel.add(row);
            playerListPanel.add(Box.createVerticalStrut(2));
        }

        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    /**
     * Construye la fila de un jugador.
     *
     * <p>
     * El nombre se renderiza como texto plano (sin HTML) dentro de un JLabel
     * con ancho mínimo fijo para evitar el wrapping incorrecto que ocurría
     * cuando nombres largos (hasta 15 chars) se recortaban o partían en varias
     * líneas.
     */
    private JPanel buildPlayerRow(Player player, boolean isLocal, boolean hostView) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(isLocal ? new Color(30, 58, 95) : new Color(30, 35, 50));
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        String roleIcon = player.isHost() ? "👑" : "🎮";
        String roleLabel = player.isHost() ? " [HOST]" : "";
        String youLabel = isLocal ? " (tú)" : "";

        // ── Corrección nombre extenso ──
        // Concatenamos en un solo String y lo asignamos con setText (sin HTML).
        // setMinimumSize garantiza que el label no colapse a un ancho mínimo
        // que forzaría al Look&Feel a partir el texto con saltos artificiales.
        String fullName = roleIcon + " " + player.getName() + roleLabel + youLabel;
        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(player.isHost() ? new Color(248, 216, 71) : Color.WHITE);
        // Ancho mínimo calculado: 15 chars * ~10px/char + extras = 260px suficiente
        nameLabel.setMinimumSize(new Dimension(260, 20));
        nameLabel.setPreferredSize(new Dimension(260, 20));
        // Mostrar nombre completo en tooltip por si acaso
        nameLabel.setToolTipText(player.getName());

        JLabel readyLabel = new JLabel(player.isReady() ? "✅ Listo" : "⏳ Esperando");
        readyLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        readyLabel.setForeground(player.isReady() ? new Color(74, 222, 128) : new Color(148, 163, 184));
        readyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(readyLabel, BorderLayout.EAST);
        return row;
    }

    /**
     * Actualiza el código de sala en el header.
     */
    public void setRoomCode(String code) {
        roomCodeLabel.setText("Código de Sala: " + code);
    }

    /**
     * Actualiza el contador de jugadores.
     */
    public void setPlayerCount(int current, int capacity) {
        playerCountLabel.setText("Jugadores: " + current + "/" + capacity);
    }

    /**
     * Habilita o deshabilita el botón Iniciar Partida.
     */
    public void setStartEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
    }

    /**
     * Configura visibilidad de botones según el rol.
     */
    public void configureForRole(boolean isHost) {
        startButton.setVisible(isHost);
        requestStartButton.setVisible(!isHost);
        revalidate();
        repaint();
    }

    /**
     * Muestra el estado de espera en el header.
     */
    public void setWaitingStatus(String text) {
        waitingStatusLabel.setText(text);
    }

    /**
     * Habilita o deshabilita el botón Solicitar Inicio.
     */
    public void setRequestStartEnabled(boolean enabled) {
        requestStartButton.setEnabled(enabled);
    }

    /**
     * Cambia el texto y color del botón Listo/Cancelar según el estado.
     */
    public void updateReadyButton(boolean isReady) {
        readyButton.setText(isReady ? "✗ Cancelar Listo" : "✓ Estoy Listo");
        readyButton.setBackground(isReady ? new Color(71, 85, 105) : new Color(34, 197, 94));
    }

    /**
     * Muestra el diálogo de confirmación al Host cuando un Peer solicita
     * iniciar.
     *
     * @param requesterName Nombre del Peer solicitante.
     * @return {@code true} si el Host acepta.
     */
    public boolean showStartRequestDialog(String requesterName) {
        int result = JOptionPane.showConfirmDialog(
                this,
                requesterName + " solicita iniciar la partida.\n¿Deseas iniciar con los jugadores actuales?",
                "Solicitud de inicio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    // ─────────────────────────────────────────────
    // Registro del Controller
    // ─────────────────────────────────────────────
    /**
     * Asigna el Controller y conecta los listeners de los botones.
     *
     * @param controller El Controller de esta pantalla.
     */
    public void setController(LobbyController controller) {
        this.controller = controller;

        readyButton.addActionListener(e -> controller.onReadyClicked());
        startButton.addActionListener(e -> controller.onStartClicked());
        requestStartButton.addActionListener(e -> controller.onRequestStartClicked());
        leaveButton.addActionListener(e -> controller.onLeaveClicked());
    }

    // ─────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }
}
