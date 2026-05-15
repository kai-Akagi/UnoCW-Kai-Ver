package Vistas;

import Controles.LobbyController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Pantalla de selección del tamaño de sala.
 *
 * <p>
 * El Host elige entre 2, 3 o 4 jugadores haciendo clic en la carta
 * correspondiente. El clic configura la capacidad Y navega directamente al
 * lobby, sin necesidad de pulsar un botón "Siguiente" separado.
 *
 * <p>
 * Usa GridBagLayout con un panel central de ancho proporcional, igual que
 * RegisterView, para que la GUI escale correctamente tanto en el tamaño por
 * defecto de la ventana como en pantalla completa.
 *
 * <p>
 * <b>Rol en MVC: View</b><br>
 * Solo dibuja. El {@link LobbyController} decide qué hacer cuando el jugador
 * selecciona un tamaño.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class CrearSala extends JPanel {

    // ─── Colores de las cartas UNO ───────────────────────────────────────────
    private static final Color COLOR_2 = new Color(220, 38, 38);  // rojo
    private static final Color COLOR_3 = new Color(22, 163, 74);  // verde
    private static final Color COLOR_4 = new Color(37, 99, 235); // azul
    private static final Color OVAL_BG = Color.WHITE;
    private static final Color SELECTED_BORDER = new Color(248, 196, 50); // dorado

    private static final int[] SIZES = {2, 3, 4};
    private static final Color[] CARD_COLORS = {COLOR_2, COLOR_3, COLOR_4};

    // ─── Componentes ─────────────────────────────────────────────────────────
    private final JPanel[] cardPanels = new JPanel[3];
    private final JLabel errorLabel;
    private final JButton cancelButton;

    private LobbyController controller;

    /**
     * Construye la pantalla de selección de tamaño de sala.
     */
    public CrearSala() {
        this.errorLabel = new JLabel(" ");
        this.cancelButton = new JButton("Cancelar");
        buildUI();
    }

    /**
     * Construye la UI con GridBagLayout para centrar el contenido y adaptarse
     * al tamaño de la ventana, igual que RegisterView.
     */
    private void buildUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(242, 239, 225)); // fondo crema UNO

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(242, 239, 225));
        card.setPreferredSize(new Dimension(700, 520));
        card.setMaximumSize(new Dimension(800, 600));

        // ── Logo UNO (pequeño, esquina superior izquierda del card) ──
        JLabel logoLabel = buildLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Título ──
        JLabel titleLabel = new JLabel("Crear sala");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Subtítulo ──
        JLabel subtitleLabel = new JLabel("Selecciona el tamaño de la sala");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(80, 80, 80));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Cartas ──
        JPanel cardsRow = buildCardsRow();
        cardsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Error ──
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Botón cancelar (alineado a la izquierda del card) ──
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomRow.setBackground(new Color(242, 239, 225));
        bottomRow.setMaximumSize(new Dimension(800, 48));
        styleCancelButton();
        bottomRow.add(cancelButton);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(Box.createVerticalStrut(12));
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(cardsRow);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);
        card.add(Box.createVerticalGlue());
        card.add(bottomRow);
        card.add(Box.createVerticalStrut(8));

        add(card);
    }

    /**
     * Construye el logo UNO pequeño desde los recursos del módulo. Si no se
     * encuentra la imagen, usa texto de respaldo.
     */
    private JLabel buildLogoLabel() {
        try {
            java.net.URL url = getClass().getResource("/imagenes/UNO_Log_pequenho.png");
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                Image scaled = raw.getImage().getScaledInstance(120, 100, Image.SCALE_SMOOTH);
                JLabel lbl = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
                lbl.setMaximumSize(new Dimension(700, 110));
                return lbl;
            }
        } catch (Exception ignored) {
        }
        JLabel fallback = new JLabel("UNO", SwingConstants.CENTER);
        fallback.setFont(new Font("SansSerif", Font.BOLD, 48));
        fallback.setForeground(new Color(220, 38, 38));
        fallback.setMaximumSize(new Dimension(700, 90));
        return fallback;
    }

    /**
     * Construye la fila de tres cartas UNO (2, 3 y 4 jugadores). Cada carta es
     * un panel clickeable que al pulsarse configura la capacidad y navega
     * directamente al lobby.
     */
    private JPanel buildCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 24, 0));
        row.setBackground(new Color(242, 239, 225));
        row.setMaximumSize(new Dimension(700, 360));
        row.setPreferredSize(new Dimension(660, 340));

        String[] imgNames = {"Tamanho2.png", "tamanho3.png", "tamanho4.png"};

        for (int i = 0; i < 3; i++) {
            final int size = SIZES[i];
            final Color cardColor = CARD_COLORS[i];
            JPanel cardPanel = buildUnoCardPanel(size, CARD_COLORS[i], imgNames[i]);
            cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (controller != null) {
                        controller.onCardSizeClicked(size);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    cardPanel.setBorder(new LineBorder(SELECTED_BORDER, 4, true));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    cardPanel.setBorder(new LineBorder(cardColor.darker(), 2, true));
                }
            });
            cardPanels[i] = cardPanel;
            row.add(cardPanel);
        }
        return row;
    }

    /**
     * Construye la representación visual de una carta UNO. Intenta cargar la
     * imagen PNG desde recursos; si no está disponible, dibuja la carta con
     * colores y el número usando Swing nativo.
     *
     * @param size El número de jugadores (2, 3 o 4).
     * @param cardColor El color base de la carta.
     * @param imgName Nombre del archivo PNG en /imagenes/.
     */
    private JPanel buildUnoCardPanel(int size, Color cardColor, String imgName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(cardColor);
        panel.setBorder(new LineBorder(cardColor.darker(), 2, true));

        // Intentar cargar imagen PNG del recurso
        try {
            java.net.URL url = getClass().getResource("/imagenes/" + imgName);
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                Image scaled = raw.getImage().getScaledInstance(190, 310, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(imgLabel, BorderLayout.CENTER);
                return panel;
            }
        } catch (Exception ignored) {
        }

        // Respaldo: dibujar carta UNO con Swing puro
        panel.setPreferredSize(new Dimension(190, 310));
        JPanel drawn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                // Fondo de carta
                g2.setColor(cardColor);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                // Óvalo blanco central
                int ow = (int) (w * 0.65), oh = (int) (h * 0.55);
                int ox = (w - ow) / 2, oy = (h - oh) / 2;
                g2.setColor(OVAL_BG);
                g2.fillOval(ox, oy, ow, oh);

                // Número grande en color de carta dentro del óvalo
                g2.setColor(cardColor);
                g2.setFont(new Font("Arial Black", Font.BOLD, (int) (h * 0.30)));
                FontMetrics fm = g2.getFontMetrics();
                String num = String.valueOf(size);
                int tx = (w - fm.stringWidth(num)) / 2;
                int ty = oy + (oh + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(num, tx, ty);

                // Número pequeño en esquinas
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, (int) (h * 0.10)));
                fm = g2.getFontMetrics();
                g2.drawString(num, 10, fm.getAscent() + 4);
                g2.drawString(num, w - fm.stringWidth(num) - 10,
                        h - fm.getDescent() - 4);
            }
        };
        drawn.setOpaque(false);
        panel.add(drawn, BorderLayout.CENTER);
        return panel;
    }

    private void styleCancelButton() {
        cancelButton.setBackground(Color.BLACK);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setPreferredSize(new Dimension(140, 42));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ─────────────────────────────────────────────
    // Métodos que el Controller llama
    // ─────────────────────────────────────────────
    /**
     * Muestra un mensaje de error debajo de las cartas.
     *
     * @param message El mensaje, o vacío para limpiar.
     */
    public void showError(String message) {
        errorLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    // ─────────────────────────────────────────────
    // Registro del Controller
    // ─────────────────────────────────────────────
    /**
     * Asigna el Controller y conecta el botón Cancelar. Los clics en cartas ya
     * se conectan internamente a {@code controller.onCardSizeClicked}.
     *
     * @param controller El Controller de esta pantalla.
     */
    public void setController(LobbyController controller) {
        this.controller = controller;
        cancelButton.addActionListener(e -> controller.onLeaveClicked());
    }
}
