package Vistas;

import Controles.GameController;
import Dominio.Card;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Pantalla principal del juego (la mesa de UNO).
 *
 * <p>
 * Muestra:
 * <ul>
 * <li>Los indicadores de los oponentes (nombre + cantidad de cartas)
 * arriba.</li>
 * <li>La carta activa en la mesa y el mazo de robo en el centro.</li>
 * <li>El botón "UNO" y el indicador de turno.</li>
 * <li>Las cartas del jugador local abajo.</li>
 * </ul>
 *
 * <p>
 * <b>Rol en MVC: View</b><br>
 * Solo dibuja. Recibe un {@link GameViewModel} con todos los datos ya
 * preparados y los renderiza. No toma ninguna decisión de juego.
 *
 * <p>
 * El único método público de actualización es {@link #render(GameViewModel)}.
 * Todo lo que se ve en pantalla proviene de ese ViewModel.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameView extends JPanel {

    // ─── Paneles principales ─────────────────────────────────────────────────
    private final JPanel opponentsPanel;  // fila superior con los oponentes
    private final JPanel centerPanel;     // área central: mazo, carta activa
    private final JPanel handPanel;       // fila inferior: cartas del jugador

    // ─── Componentes del centro ──────────────────────────────────────────────
    private final JPanel topCardPanel;   // muestra la carta activa
    private final JLabel topCardLabel;   // texto de la carta activa
    private final JButton drawButton;     // botón para robar del mazo
    private final JButton unoButton;      // botón "¡UNO!"
    private final JLabel turnLabel;      // "Tu turno" o nombre del jugador activo
    private final JLabel directionLabel; // indicador de dirección

    /**
     * Controller de esta pantalla.
     */
    private GameController controller;

    /**
     * Temporizador de turno: cuenta regresivamente desde TURN_SECONDS hasta 0.
     * Al llegar a 0 notifica al Controller para pasar el turno automáticamente.
     */
    private Timer turnTimer;
    private int secondsLeft;
    private static final int TURN_SECONDS = 30;

    /**
     * Construye la pantalla del juego con fondo verde oscuro (mesa de juego).
     */
    public GameView() {
        this.opponentsPanel = new JPanel();
        this.centerPanel = new JPanel();
        this.handPanel = new JPanel();
        this.topCardPanel = new JPanel();
        this.topCardLabel = new JLabel("?", SwingConstants.CENTER);
        this.drawButton = new JButton("🂠 ROBAR");
        this.unoButton = new JButton("¡UNO!");
        this.turnLabel = new JLabel("Esperando...", SwingConstants.CENTER);
        this.directionLabel = new JLabel("→", SwingConstants.CENTER);

        buildUI();
    }

    /**
     * Construye y organiza toda la interfaz de la mesa de juego.
     */
    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(21, 128, 61)); // verde mesa
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Zona de oponentes (arriba) ──
        opponentsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 4));
        opponentsPanel.setBackground(new Color(20, 83, 45));
        opponentsPanel.setPreferredSize(new Dimension(0, 80));

        // ── Zona central ──
        buildCenterPanel();

        // ── Zona de mano del jugador (abajo) ──
        handPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        handPanel.setBackground(new Color(15, 100, 50));
        handPanel.setPreferredSize(new Dimension(0, 130));

        add(opponentsPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(handPanel, BorderLayout.SOUTH);
    }

    /**
     * Construye el panel central donde están el mazo, la carta activa y los
     * botones.
     */
    private void buildCenterPanel() {
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(new Color(21, 128, 61));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);

        // Mazo de robo
        styleButton(drawButton, new Color(30, 30, 46), 56, 80);
        drawButton.setFont(new Font("Arial", Font.BOLD, 11));

        // Carta activa
        topCardPanel.setPreferredSize(new Dimension(72, 100));
        topCardPanel.setLayout(new BorderLayout());
        topCardPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        topCardPanel.setBackground(Color.RED);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topCardLabel.setForeground(Color.WHITE);
        topCardPanel.add(topCardLabel, BorderLayout.CENTER);

        // Botón UNO
        unoButton.setFont(new Font("Arial Black", Font.BOLD, 18));
        unoButton.setBackground(new Color(234, 179, 8));
        unoButton.setForeground(Color.WHITE);
        unoButton.setPreferredSize(new Dimension(90, 90));
        unoButton.setFocusPainted(false);
        unoButton.setBorderPainted(false);
        unoButton.setOpaque(true);
        unoButton.setEnabled(false);

        // Etiqueta de turno
        turnLabel.setFont(new Font("Arial", Font.BOLD, 14));
        turnLabel.setForeground(Color.WHITE);

        // Dirección
        directionLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        directionLabel.setForeground(new Color(134, 239, 172));

        // Ensamblar en el grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(drawButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(topCardPanel, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        centerPanel.add(unoButton, gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        centerPanel.add(directionLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        centerPanel.add(turnLabel, gbc);
    }

    // ─────────────────────────────────────────────
    // Método principal de actualización
    // ─────────────────────────────────────────────
    /**
     * Renderiza toda la pantalla con los datos del ViewModel.
     *
     * <p>
     * Este es el único método que la View expone para actualizarse. Siempre
     * debe llamarse desde el hilo de Swing (EDT). El {@link GameController} lo
     * envuelve con {@code invokeLater()}.
     *
     * @param vm El ViewModel con todos los datos a mostrar.
     */
    public void render(GameViewModel vm) {
        updateTopCard(vm.topCardValue, vm.topCardColor);
        updateTurnIndicator(vm.currentPlayerName, vm.isMyTurn);
        updateOpponents(vm.opponentNames, vm.opponentHandSizes);
        updateHand(vm.localHand, vm.isMyTurn);
        updateUnoButton(vm.localPlayerHasUno && vm.isMyTurn);
        updateDirection(vm.clockwise);

        drawButton.setEnabled(vm.isMyTurn);

        // Iniciar temporizador solo cuando es nuestro turno
        if (vm.isMyTurn) {
            startTurnTimer();
        } else {
            stopTurnTimer();
            // Mostrar dirección normal cuando no es nuestro turno
            updateDirection(vm.clockwise);
        }

        revalidate();
        repaint();
    }

    /**
     * Actualiza la carta activa en la mesa.
     *
     * @param value El valor o nombre de la carta (ej. "7", "SKIP").
     * @param color El color de la carta.
     */
    private void updateTopCard(String value, Card.Color color) {
        topCardLabel.setText(value);
        topCardPanel.setBackground(colorOf(color));
    }

    /**
     * Actualiza el indicador de turno.
     *
     * @param playerName Nombre del jugador activo.
     * @param isLocalTurn Si es el turno del jugador local.
     */
    private void updateTurnIndicator(String playerName, boolean isLocalTurn) {
        if (isLocalTurn) {
            turnLabel.setText("¡Es tu turno!");
            turnLabel.setForeground(new Color(250, 204, 21)); // amarillo brillante
        } else {
            turnLabel.setText("Turno de: " + playerName);
            turnLabel.setForeground(Color.WHITE);
        }
    }

    /**
     * Actualiza los paneles de oponentes en la parte superior.
     *
     * @param names Nombres de los oponentes.
     * @param handSizes Cantidad de cartas de cada oponente.
     */
    private void updateOpponents(List<String> names, List<Integer> handSizes) {
        opponentsPanel.removeAll();
        for (int i = 0; i < names.size(); i++) {
            JPanel opPanel = buildOpponentPanel(names.get(i), handSizes.get(i));
            opponentsPanel.add(opPanel);
        }
        opponentsPanel.revalidate();
        opponentsPanel.repaint();
    }

    /**
     * Construye el panel visual de un oponente (nombre + cantidad de cartas).
     *
     * @param name Nombre del oponente.
     * @param cardCount Cuántas cartas tiene.
     * @return El panel del oponente.
     */
    private JPanel buildOpponentPanel(String name, int cardCount) {
        JPanel panel = new JPanel(new BorderLayout(4, 2));
        panel.setBackground(new Color(20, 83, 45));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 222, 128), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        panel.setPreferredSize(new Dimension(130, 60));

        JLabel nameLabel = new JLabel("🎮 " + name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);

        JLabel countLabel = new JLabel(cardCount + " cartas", SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        countLabel.setForeground(cardCount == 1
                ? new Color(250, 204, 21) // amarillo si tiene UNO
                : new Color(134, 239, 172));

        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Actualiza la fila de cartas en mano del jugador local. Cada carta es un
     * botón que el jugador puede presionar para jugarla.
     *
     * @param hand La mano actual del jugador.
     * @param isMyTurn Si es el turno del jugador local (habilita los botones).
     */
    private void updateHand(List<Card> hand, boolean isMyTurn) {
        handPanel.removeAll();

        for (Card card : hand) {
            JButton cardBtn = buildCardButton(card, isMyTurn);
            handPanel.add(cardBtn);
        }

        handPanel.revalidate();
        handPanel.repaint();
    }

    /**
     *
     */
    public void disableHand() {
        // Deshabilitar cartas y el robo de carta del mazo — el jugador debe elegir color para continuar
        for (java.awt.Component c : handPanel.getComponents()) {
            c.setEnabled(false);
        }
        drawButton.setEnabled(false);

    }

    /**
     * Construye un botón visual para una carta de la mano.
     *
     * @param card La carta a representar.
     * @param enabled Si el botón debe estar activo (es el turno del jugador).
     * @return El botón de la carta.
     */
    private JButton buildCardButton(Card card, boolean enabled) {
        JButton btn = new JButton("<html><center>" + card.getValue() + "</center></html>");
        btn.setPreferredSize(new Dimension(60, 90));
        btn.setBackground(colorOf(card.getColor()));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setEnabled(enabled);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (enabled) {
            btn.addActionListener(e -> {
                if (controller != null) {
                    controller.onCardClicked(card);
                }
            });
            // Efecto hover: iluminar al pasar el mouse
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBorder(UIManager.getBorder("Button.border"));
                }
            });
        }

        return btn;
    }

    /**
     * Habilita o deshabilita el botón "¡UNO!" según si el jugador tiene una
     * carta y es su turno.
     *
     * @param enabled {@code true} para habilitar el botón UNO.
     */
    private void updateUnoButton(boolean enabled) {
        unoButton.setEnabled(enabled);
        unoButton.setBackground(enabled
                ? new Color(234, 179, 8) // amarillo brillante si activo
                : new Color(100, 100, 100) // gris si no
        );
    }

    /**
     * Actualiza el indicador de dirección del juego.
     *
     * @param clockwise {@code true} si va en sentido normal (→).
     */
    private void updateDirection(boolean clockwise) {
        directionLabel.setText(clockwise ? "→" : "←");
    }

    // ─────────────────────────────────────────────
    // Registro del Controller
    // ─────────────────────────────────────────────
    /**
     * Inicia el temporizador de turno con el número de segundos configurado. Si
     * ya había un temporizador corriendo, lo detiene primero.
     *
     * <p>
     * El temporizador corre en el hilo de Swing (usa {@link Timer} de Swing),
     * por lo que es seguro actualizar la GUI directamente desde el callback.
     */
    public void startTurnTimer() {
        stopTurnTimer();
        secondsLeft = TURN_SECONDS;
        updateTimerLabel();

        turnTimer = new Timer(1000, e -> {
            secondsLeft--;
            updateTimerLabel();
            if (secondsLeft <= 0) {
                stopTurnTimer();
                if (controller != null) {
                    controller.onTurnTimerExpired();
                }
            }
        });
        turnTimer.start();
    }

    /**
     * Detiene el temporizador de turno si está corriendo.
     */
    public void stopTurnTimer() {
        if (turnTimer != null && turnTimer.isRunning()) {
            turnTimer.stop();
        }
    }

    /**
     * Activa o desactiva el modo de periodo de gracia UNO.
     *
     * <p>
     * En modo gracia ocurre lo siguiente:
     * <ul>
     * <li>El timer se reinicia a 5 segundos.</li>
     * <li>Todas las cartas de la mano se deshabilitan.</li>
     * <li>El botón UNO se habilita con color especial urgente.</li>
     * <li>El botón de robar se deshabilita.</li>
     * </ul>
     * Al desactivar el modo, la interfaz vuelve al estado normal del siguiente
     * turno.
     *
     * @param graceMode {@code true} para entrar al modo gracia; {@code false}
     * para salir.
     */
    public void setUnoGraceMode(boolean graceMode) {
        if (graceMode) {
            // Deshabilitar cartas y robar — el jugador solo puede presionar UNO
            for (java.awt.Component c : handPanel.getComponents()) {
                c.setEnabled(false);
            }
            drawButton.setEnabled(false);

            // Botón UNO con color urgente y parpadeante
            unoButton.setEnabled(true);
            unoButton.setBackground(new Color(220, 38, 38));   // rojo urgente
            unoButton.setText("¡UNO! 🚨");

            // Timer reducido a 5 segundos para el periodo de gracia
            stopTurnTimer();
            secondsLeft = 5;
            updateTimerLabel();
            turnTimer = new Timer(1000, e -> {
                secondsLeft--;
                updateTimerLabel();
                if (secondsLeft <= 0) {
                    stopTurnTimer();
                    if (controller != null) {
                        controller.onTurnTimerExpired();
                    }
                }
            });
            turnTimer.start();
        } else {
            // Restaurar botón UNO a su estado normal
            unoButton.setEnabled(false);
            unoButton.setBackground(new Color(234, 179, 8));
            unoButton.setText("¡UNO!");
        }
    }

    /**
     * Actualiza la etiqueta del temporizador con el tiempo restante. Cambia a
     * rojo cuando quedan 10 segundos o menos.
     */
    private void updateTimerLabel() {
        String timeStr = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
        turnLabel.setText(turnLabel.getText().contains("Tu turno")
                ? "¡Es tu turno! " + timeStr
                : turnLabel.getText());
        // Mostrar el tiempo también en el indicador de dirección temporalmente
        directionLabel.setText((secondsLeft <= 10 ? "⏰ " : "")
                + (secondsLeft <= 0 ? "00:00" : timeStr));
        if (secondsLeft <= 10) {
            directionLabel.setForeground(new Color(252, 100, 100)); // rojo
        } else {
            directionLabel.setForeground(new Color(134, 239, 172)); // verde
        }
    }

    /**
     * Asigna el Controller y conecta los botones que no dependen de las cartas.
     *
     * @param controller El Controller de esta pantalla.
     */
    public void setController(GameController controller) {
        this.controller = controller;

        drawButton.addActionListener(e -> controller.onDrawClicked());
        unoButton.addActionListener(e -> controller.onUnoClicked());
    }

    // ─────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────
    /**
     * Convierte un color de carta UNO a un color de Swing para el fondo.
     *
     * @param color El color de la carta.
     * @return El {@link Color} de Swing correspondiente.
     */
    private Color colorOf(Card.Color color) {
        switch (color) {
            case RED:
                return new Color(220, 38, 38);
            case BLUE:
                return new Color(37, 99, 235);
            case GREEN:
                return new Color(22, 163, 74);
            case YELLOW:
                return new Color(202, 138, 4);
            default:
                return new Color(30, 30, 46); // WILD → oscuro
        }
    }

    private void styleButton(JButton btn, Color bg, int w, int h) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
    }
}
