package Vistas;

import Dominio.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import Controles.LobbyController;

/**
 * Pantalla del lobby (sala de espera).
 *
 * <p>
 * Muestra la lista de jugadores conectados, sus estados ("Listo" / "No listo"),
 * el código de sala, y los controles disponibles según el rol del jugador.
 *
 * <p>
 * <b>Rol en MVC: View</b><br>
 * Solo dibuja. No valida nada. El {@link LobbyController} decide qué mostrar y
 * cuándo habilitar o deshabilitar botones.
 *
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 *
 */
public class LobbyView extends JPanel {

    // ─── Componentes principales ─────────────────────────────────────────────
    private final JLabel roomCodeLabel;
    private final JLabel playerCountLabel;
    private final JPanel playerListPanel;
    private final JButton readyButton;
    private final JButton startButton;          // solo visible para el Host
    private final JButton requestStartButton;   // solo visible para los Peers
    private final JButton leaveButton;
    private final JComboBox<Integer> capacitySelector; // solo para el Host

    /**
     * Controller asignado a esta View.
     */
    private LobbyController controller;

    /**
     * Construye la pantalla del lobby.
     */
    public LobbyView() {
        this.roomCodeLabel = new JLabel("Código: ----");
        this.playerCountLabel = new JLabel("Jugadores: 0/?");
        this.playerListPanel = new JPanel();
        this.readyButton = new JButton("✓ Estoy Listo");
        this.startButton = new JButton("▶ Iniciar Partida");
        this.requestStartButton = new JButton("▶ Solicitar Inicio");
        this.leaveButton = new JButton("← Salir");
        this.capacitySelector = new JComboBox<>(new Integer[]{2, 3, 4});

        buildUI();
    }

    /**
     * Construye y organiza la interfaz del lobby.
     */
    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 46));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // ── Encabezado ──
        JPanel header = buildHeader();

        // ── Lista de jugadores ──
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(new Color(22, 22, 35));
        playerListPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 72)));

        JScrollPane scrollPane = new JScrollPane(playerListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(22, 22, 35));

        // ── Panel de controles (botones inferiores) ──
        JPanel controls = buildControls();

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
    }

    /**
     * Construye el encabezado con título, código de sala y contador de
     * jugadores.
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setBackground(new Color(30, 30, 46));

        JLabel title = new JLabel("Sala de Espera", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        roomCodeLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        roomCodeLabel.setForeground(new Color(248, 216, 71));
        roomCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        playerCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playerCountLabel.setForeground(new Color(148, 163, 184));
        playerCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel de capacidad (solo el Host lo verá habilitado)
        JPanel capacityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        capacityPanel.setBackground(new Color(30, 30, 46));
        JLabel capLabel = new JLabel("Tamaño de sala: ");
        capLabel.setForeground(new Color(148, 163, 184));
        capacitySelector.setPreferredSize(new Dimension(60, 28));
        capacitySelector.setSelectedItem(4);
        capacityPanel.add(capLabel);
        capacityPanel.add(capacitySelector);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(new Color(30, 30, 46));
        topRow.add(title, BorderLayout.CENTER);
        topRow.add(capacityPanel, BorderLayout.EAST);

        header.add(topRow, BorderLayout.NORTH);
        header.add(roomCodeLabel, BorderLayout.CENTER);
        header.add(playerCountLabel, BorderLayout.SOUTH);

        return header;
    }

    /**
     * Construye el panel inferior con los botones de acción.
     */
    private JPanel buildControls() {
        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setBackground(new Color(30, 30, 46));
        controls.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Estilizar botones
        styleButton(readyButton, new Color(34, 197, 94));  // verde
        styleButton(startButton, new Color(59, 130, 246)); // azul
        styleButton(requestStartButton, new Color(234, 179, 8));  // amarillo
        styleButton(leaveButton, new Color(71, 85, 105));  // gris

        startButton.setEnabled(false);        // se habilita cuando todos están listos
        requestStartButton.setEnabled(false); // se habilita cuando el Peer está listo

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setBackground(new Color(30, 30, 46));
        rightButtons.add(readyButton);
        rightButtons.add(requestStartButton);
        rightButtons.add(startButton);

        controls.add(leaveButton, BorderLayout.WEST);
        controls.add(rightButtons, BorderLayout.EAST);

        return controls;
    }

    // ─────────────────────────────────────────────
    // Métodos que el Controller llama para actualizar la View
    // ─────────────────────────────────────────────
    /**
     * Actualiza la lista visual de jugadores conectados. Redibuja toda la lista
     * cada vez que cambia.
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
     * Construye la fila visual de un jugador en la lista del lobby.
     *
     * @param player El jugador a mostrar.
     * @param isLocal Si es el jugador local (se resalta diferente).
     * @param hostView Si el que ve esto es el Host.
     * @return El panel de la fila.
     */
    private JPanel buildPlayerRow(Player player, boolean isLocal, boolean hostView) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(isLocal
                ? new Color(30, 58, 95) // azul oscuro: soy yo
                : new Color(30, 35, 50)); // gris oscuro: otro jugador
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        // Ícono según rol: corona para Host, control para jugador normal
        String roleIcon = player.isHost() ? "👑" : "🎮";
        // Etiqueta de rol visible para todos
        String roleLabel = player.isHost() ? " [HOST]" : "";
        String youLabel = isLocal ? " (tú)" : "";

        JLabel nameLabel = new JLabel(
                roleIcon + " " + player.getName() + roleLabel + youLabel
        );
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        // El Host tiene color dorado para destacarlo
        nameLabel.setForeground(player.isHost()
                ? new Color(248, 216, 71)
                : Color.WHITE);

        // Estado "Listo": refleja el valor real de player.isReady()
        // El Host puede estar listo o no listo como cualquier otro jugador
        JLabel readyLabel = new JLabel(player.isReady() ? "✅ Listo" : "⏳ Esperando");
        readyLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        readyLabel.setForeground(player.isReady()
                ? new Color(74, 222, 128)
                : new Color(148, 163, 184));
        readyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(readyLabel, BorderLayout.EAST);

        return row;
    }

    /**
     * Actualiza el código de sala visible en el encabezado.
     *
     * @param code El código de la sala (ej. "A3F2-9KX1").
     */
    public void setRoomCode(String code) {
        roomCodeLabel.setText("Código de Sala: " + code);
    }

    /**
     * Actualiza el contador de jugadores conectados.
     *
     * @param current Cuántos hay ahora.
     * @param capacity Cuántos caben en total.
     */
    public void setPlayerCount(int current, int capacity) {
        playerCountLabel.setText("Jugadores: " + current + "/" + capacity);
    }

    /**
     * Habilita o deshabilita el botón "Iniciar Partida". Solo el Host puede
     * verlo habilitado, y solo cuando todos están listos.
     *
     * @param enabled {@code true} para habilitar.
     */
    public void setStartEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
    }

    /**
     * Configura la visibilidad del botón de inicio y el selector de capacidad
     * según si el jugador local es Host o Peer.
     *
     * @param isHost {@code true} si el jugador local es el Host.
     */
    public void configureForRole(boolean isHost) {
        startButton.setVisible(isHost);
        requestStartButton.setVisible(!isHost);
        capacitySelector.setEnabled(isHost);
    }

    /**
     * Habilita o deshabilita el botón "Solicitar Inicio". Se habilita cuando el
     * Peer ya marcó "Listo".
     *
     * @param enabled {@code true} para habilitar.
     */
    public void setRequestStartEnabled(boolean enabled) {
        requestStartButton.setEnabled(enabled);
    }

    /**
     * Muestra un diálogo al Host notificando que un Peer solicita iniciar.
     *
     * @param requesterName Nombre del Peer que hizo la solicitud.
     * @return {@code true} si el Host acepta iniciar.
     */
    public boolean showStartRequestDialog(String requesterName) {
        int result = JOptionPane.showConfirmDialog(
                this,
                requesterName + " solicita iniciar la partida ahora.\n¿Deseas iniciar con los jugadores actuales?",
                "Solicitud de inicio",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Cambia el texto del botón "Listo" según el estado actual.
     *
     * @param isReady {@code true} si el jugador ya está listo.
     */
    public void updateReadyButton(boolean isReady) {
        readyButton.setText(isReady ? "✗ Cancelar Listo" : "✓ Estoy Listo");
        readyButton.setBackground(isReady
                ? new Color(71, 85, 105)
                : new Color(34, 197, 94));
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
        capacitySelector.addActionListener(e
                -> controller.onCapacityChanged((Integer) capacitySelector.getSelectedItem())
        );
    }

    // ─────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }
}
