package Vistas;

import Dominio.Card;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import Controles.GameController;
import static Dominio.Card.Color.BLUE;
import static Dominio.Card.Color.GREEN;
import static Dominio.Card.Color.RED;
import static Dominio.Card.Color.YELLOW;

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
    private final JPanel topCardPanel;    // muestra la carta activa
    private final JLabel topCardLabel;    // texto de la carta activa
    private final JButton drawButton;     // botón para robar del mazo
    private final JButton unoButton;      // botón "¡UNO!"
    private final JLabel turnLabel;       // "Tu turno" o nombre del jugador activo
    private final JLabel directionLabel;  // indicador de dirección
    private final JButton leaveButton;    // botón para abandonar la partida
    private final JPanel activeColorPanel; // pastilla de color activo (visible tras comodín)
    private final JLabel activeColorLabel; // texto "Color: ROJO" etc.

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
        this.leaveButton = new JButton("✖ Salir");
        this.activeColorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        this.activeColorLabel = new JLabel("", SwingConstants.CENTER);

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
        // BorderLayout para colocar los oponentes al centro y el botón Salir a la derecha
        opponentsPanel.setLayout(new BorderLayout(4, 0));
        opponentsPanel.setBackground(new Color(20, 83, 45));
        opponentsPanel.setPreferredSize(new Dimension(0, 80));

        // Panel interno para los avatares de oponentes (centro)
        JPanel opponentsInner = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        opponentsInner.setBackground(new Color(20, 83, 45));
        opponentsInner.setName("opponentsInner");
        opponentsPanel.add(opponentsInner, BorderLayout.CENTER);

        // Botón Salir — esquina superior derecha
        leaveButton.setBackground(new Color(153, 27, 27));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.setFont(new Font("Arial", Font.BOLD, 11));
        leaveButton.setFocusPainted(false);
        leaveButton.setBorderPainted(false);
        leaveButton.setOpaque(true);
        leaveButton.setPreferredSize(new Dimension(85, 36));
        leaveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel leaveWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 22));
        leaveWrapper.setBackground(new Color(20, 83, 45));
        leaveWrapper.add(leaveButton);
        opponentsPanel.add(leaveWrapper, BorderLayout.EAST);

        // ── Zona central ──
        buildCenterPanel();

        // ── Zona de mano del jugador (abajo) ──
        handPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
        handPanel.setBackground(new Color(15, 100, 50));
        handPanel.setPreferredSize(new Dimension(0, 130));

        // Pastilla de color activo — esquina inferior izquierda del área central
        activeColorLabel.setFont(new Font("Arial", Font.BOLD, 12));
        activeColorLabel.setForeground(Color.WHITE);
        activeColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        activeColorPanel.setOpaque(true);
        activeColorPanel.setBackground(new Color(30, 30, 46));
        activeColorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)
        ));
        activeColorPanel.add(activeColorLabel);
        activeColorPanel.setVisible(false);

        // Tira inferior del área central: solo lleva la pastilla a la izquierda
        JPanel centerSouth = new JPanel(new BorderLayout());
        centerSouth.setBackground(new Color(21, 128, 61));
        centerSouth.add(activeColorPanel, BorderLayout.WEST);

        // Wrapper que envuelve el área de juego + la tira de color
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 0));
        centerWrapper.setBackground(new Color(21, 128, 61));
        centerWrapper.add(centerPanel, BorderLayout.CENTER);
        centerWrapper.add(centerSouth, BorderLayout.SOUTH);

        add(opponentsPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
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
        updateActiveColor(vm.topCardValue, vm.activeColor);
        updateTurnIndicator(vm.currentPlayerName, vm.isMyTurn);
        updateOpponents(vm.opponentNames, vm.opponentHandSizes, vm.opponentAvatarIds);
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
    /**
     * Actualiza la carta activa en la mesa.
     *
     * <p>
     * Si la carta es especial o comodín, carga su imagen desde el classpath y
     * la muestra como {@link JLabel} con icono. Si es numérica, muestra el
     * número centrado sobre el fondo de color correspondiente.
     *
     * @param value El valor/nombre de la carta (ej. "7", "SKIP", "WILD").
     * @param color El color de la carta activa.
     */
    private void updateTopCard(String value, Card.Color color) {
        // Crear una carta temporal para obtener la clave de imagen
        // (no necesitamos efecto, solo color y valor para la imagen)
        Dominio.Card tempCard = new Dominio.Card(color, value, null);
        ImageIcon icon = CardImageHelper.getScaledIcon(tempCard, 68, 96);

        topCardPanel.setBackground(colorOf(color));

        if (icon != null) {
            topCardLabel.setIcon(icon);
            topCardLabel.setText("");
        } else {
            // Carta numérica: mostrar número sin icono
            topCardLabel.setIcon(null);
            topCardLabel.setText(value);
        }
    }

    /**
     * Muestra u oculta la pastilla de color activo.
     *
     * <p>
     * Cuando la carta en la mesa es un comodín (WILD o WILD_DRAW_FOUR), el
     * color real que rige el juego no es el de la carta sino el elegido por el
     * jugador que la jugó. Esta pastilla lo hace visible para todos.
     *
     * <p>
     * Si la carta activa no es comodín, la pastilla se oculta porque el color
     * ya se ve en el fondo de la propia carta.
     *
     * @param topCardColor Color de la carta en la mesa (puede ser WILD).
     * @param activeColor Color activo que rige el juego.
     */
    private void updateActiveColor(String topCardValue, Card.Color activeColor) {
        // Detectar comodín por el VALOR de la carta, no por su color.
        // El GameController ya reemplaza topCardColor con el color elegido,
        // así que topCardColor nunca llega como WILD aquí. El valor sí es
        // "WILD" o "WILD_DRAW_FOUR" cuando hay un comodín en la mesa.
        boolean isWild = "WILD".equals(topCardValue) || "WILD_DRAW_FOUR".equals(topCardValue);
        boolean hasChosenColor = (activeColor != null && activeColor != Card.Color.WILD);

        if (isWild && hasChosenColor) {
            activeColorPanel.setBackground(colorOf(activeColor));
            activeColorLabel.setText("Color activo: " + colorName(activeColor));
            activeColorPanel.setVisible(true);
        } else {
            activeColorPanel.setVisible(false);
        }
    }

    /**
     * Convierte un color de carta al nombre en español para mostrarlo al
     * jugador.
     *
     * @param color El color.
     * @return El nombre en español ("ROJO", "AZUL", "VERDE", "AMARILLO").
     */
    private String colorName(Card.Color color) {
        switch (color) {
            case RED:
                return "ROJO";
            case BLUE:
                return "AZUL";
            case GREEN:
                return "VERDE";
            case YELLOW:
                return "AMARILLO";
            default:
                return "";
        }
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
    private void updateOpponents(List<String> names, List<Integer> handSizes, List<String> avatarIds) {
        // Buscar el panel interno por nombre para no afectar el botón Salir
        JPanel inner = null;
        for (java.awt.Component c : opponentsPanel.getComponents()) {
            if (c instanceof JPanel && "opponentsInner".equals(((JPanel) c).getName())) {
                inner = (JPanel) c;
                break;
            }
        }
        if (inner == null) {
            return;
        }
        inner.removeAll();
        for (int i = 0; i < names.size(); i++) {
            String avatarId = (avatarIds != null && i < avatarIds.size()) ? avatarIds.get(i) : "";
            inner.add(buildOpponentPanel(names.get(i), handSizes.get(i), avatarId));
        }

        inner.revalidate();
        inner.repaint();

    }

    /**
     * Construye el panel visual de un oponente (nombre + cantidad de cartas).
     *
     * @param name Nombre del oponente.
     * @param cardCount Cuántas cartas tiene.
     * @param avatarId id del avatar
     * @return El panel del oponente.
     */
    private JPanel buildOpponentPanel(String name, int cardCount, String avatarId) {
        JPanel panel = new JPanel(new BorderLayout(4, 2));
        panel.setBackground(new Color(20, 83, 45));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 222, 128), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        panel.setPreferredSize(new Dimension(130, 60));

        String emoji = AvatarHelper.emojiFor(avatarId);
        JLabel nameLabel = new JLabel(emoji + " " + name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
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
     * Deshabilita la mano del jugador y el botón de robo.
     *
     * Se utiliza cuando el jugador debe elegir un color antes de continuar con
     * su turno.
     */
    /**
     * Deshabilita la mano del jugador y el botón de robo.
     *
     * <p>
     * Para botones con imagen (cartas especiales) no se usa
     * {@code setEnabled(false)} porque Swing aplica un GrayFilter automático
     * que grisea la imagen. En su lugar se elimina el borde interactivo y se
     * retiran los listeners existentes mediante el truco de reemplazar el
     * componente en el EventDispatchThread. La solución práctica aquí es
     * simplemente no agregar el listener cuando {@code enabled=false} en
     * {@link #buildCardButton}, lo que ya se hace; este método solo asegura que
     * el cursor cambie y el borde se actualice para los botones de imagen.
     */
    public void disableHand() {
        for (java.awt.Component c : handPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                if (btn.getIcon() != null) {
                    // Carta con imagen: dejar colores intactos, solo cambiar borde
                    btn.setBorder(BorderFactory.createLineBorder(
                            new Color(255, 255, 255, 60), 1));
                    btn.setCursor(Cursor.getDefaultCursor());
                } else {
                    // Carta numérica: setEnabled normal (no hay ImageIcon que grisearse)
                    btn.setEnabled(false);
                }
            }
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
    /**
     * Construye un botón visual para una carta de la mano.
     *
     * <p>
     * Si la carta tiene imagen (especial o comodín) se muestra como
     * {@link ImageIcon} escalada al tamaño del botón. Las cartas numéricas se
     * renderizan como rectángulo de color con el número centrado.
     *
     * @param card La carta a representar.
     * @param enabled Si el botón debe estar activo (es el turno del jugador).
     * @return El botón de la carta.
     */
    private JButton buildCardButton(Card card, boolean enabled) {
        final int CARD_W = 60;
        final int CARD_H = 90;

        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(CARD_W, CARD_H));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Imagen si existe (cartas especiales / comodines) ──────────────
        ImageIcon icon = CardImageHelper.getScaledIcon(card, CARD_W, CARD_H);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("");
            btn.setBackground(colorOf(card.getColor()));
            // Fondo transparente: solo se ve la imagen
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
        } else {
            // ── Carta numérica → rectángulo de color + número ─────────────
            btn.setText("<html><center>" + card.getValue() + "</center></html>");
            btn.setBackground(colorOf(card.getColor()));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
        }

        // ── Indicar turno con borde, NO con setEnabled(false) ──────────────
        // setEnabled(false) en Swing grisea los ImageIcon automáticamente.
        // En su lugar: el botón siempre está "enabled" a nivel Swing para
        // preservar los colores; el ActionListener solo se agrega cuando es
        // el turno del jugador, por lo que clicar fuera de turno no hace nada.
        btn.setEnabled(true);
        if (!enabled) {
            // Borde tenue para indicar visualmente que no es interactuable
            btn.setBorder(BorderFactory.createLineBorder(
                    new Color(255, 255, 255, 60), 1));
        }

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

    // Registro del Controller
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
            // Deshabilitar cartas y robar — el jugador solo puede presionar UNO.
            // Para botones con imagen: solo cambiar borde (no setEnabled) para no grisearse.
            for (java.awt.Component c : handPanel.getComponents()) {
                if (c instanceof JButton) {
                    JButton btn = (JButton) c;
                    if (btn.getIcon() != null) {
                        btn.setBorder(BorderFactory.createLineBorder(
                                new Color(255, 255, 255, 60), 1));
                        btn.setCursor(Cursor.getDefaultCursor());
                    } else {
                        btn.setEnabled(false);
                    }
                } else {
                    c.setEnabled(false);
                }
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
        leaveButton.addActionListener(e -> controller.onLeaveClicked());
    }

    // Utilidades
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
