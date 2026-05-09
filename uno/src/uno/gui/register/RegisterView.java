package uno.gui.register;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Pantalla de registro del jugador.
 *
 * <p>Muestra el formulario donde el jugador ingresa su nombre y selecciona
 * un avatar antes de entrar al lobby.
 *
 * <p><b>Rol en MVC: View (Vista)</b><br>
 * Solo dibuja componentes Swing y delega acciones al Controller.
 */
public class RegisterView extends JPanel {

    // ─── Avatares disponibles ────────────────────────────────────────────────
    private static final String[] AVATAR_IDS = {
        "avatar_pig", "avatar_bear", "avatar_panda", "avatar_bunny",
        "avatar_fox", "avatar_penguin", "avatar_chick", "avatar_wolf", "avatar_frog"
    };

    private static final String[] AVATAR_EMOJIS = {
        "🐷", "🐻", "🐼", "🐰", "🦊", "🐧", "🐥", "🐺", "🐸"
    };

    private static final Color[] AVATAR_COLORS = {
        new Color(255, 182, 193), // rosa      - cerdo
        new Color(210, 180, 140), // café      - oso
        new Color(220, 220, 220), // gris      - panda
        new Color(255, 209, 220), // rosa claro- conejo
        new Color(255, 200, 150), // naranja   - zorro
        new Color(173, 216, 230), // azul claro- pingüino
        new Color(255, 230, 150), // amarillo  - pollito
        new Color(200, 200, 200), // gris      - lobo
        new Color(144, 238, 144)  // verde     - rana
    };

    // ─── Componentes Swing ───────────────────────────────────────────────────
    private final JTextField nameField;
    private final JLabel     errorLabel;
    private final JButton    continueButton;
    private final JButton[]  avatarButtons;

    private int selectedAvatarIndex = -1;
    private RegisterController controller;

    public RegisterView() {
        this.avatarButtons  = new JButton[AVATAR_IDS.length];
        this.nameField      = new JTextField();
        this.errorLabel     = new JLabel(" ");
        this.continueButton = new JButton("SIGUIENTE");
        buildUI();
    }

    /**
     * Construye la interfaz usando un panel central de ancho fijo dentro de
     * un BorderLayout, lo que garantiza alineación consistente sin importar
     * el tamaño de la ventana.
     */
    private void buildUI() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        // Panel central de ancho proporcional — se adapta al tamaño de ventana.
        // Usamos un porcentaje del ancho disponible para que en pantalla completa
        // haya más separación entre componentes y se vea más espacioso.
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        // Ancho base 560px; en pantalla grande el GridBagLayout lo centrará
        // con espacio generoso a los lados.
        card.setPreferredSize(new Dimension(600, 520));
        card.setMaximumSize(new Dimension(700, 620));

        // ── Logo ──
        JLabel logoLabel = buildLogoLabel("src/assets/logo_uno.png");
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Etiqueta "Nombre de Jugador:" ──
        JLabel nameLabel = new JLabel("Nombre de Jugador:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(new Color(40, 40, 40));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Campo de nombre con ícono lápiz ──
        JPanel nameRow = buildNameRow();
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Mensaje de error ──
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Etiqueta "Selecciona tu avatar:" ──
        JLabel avatarLabel = new JLabel("Selecciona tu avatar:");
        avatarLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        avatarLabel.setForeground(new Color(40, 40, 40));
        avatarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Galería de avatares ──
        JPanel avatarPanel = buildAvatarPanel();
        avatarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Botón SIGUIENTE alineado a la derecha ──
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.setMaximumSize(new Dimension(700, 48));
        styleContinueButton();
        buttonRow.add(continueButton);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Ensamblar ──
        // Los espacios verticales se amplían con el panel Glue en pantalla grande,
        // ya que GridBagLayout centrará el card y dejará espacio libre alrededor.
        card.add(Box.createVerticalStrut(8));
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(32));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(nameRow);
        card.add(Box.createVerticalStrut(4));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(avatarLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(avatarPanel);
        card.add(Box.createVerticalStrut(28));
        card.add(buttonRow);

        add(card);
    }

    /**
     * Construye la etiqueta del logo UNO.
     * Carga el asset de imagen si existe; si no, muestra texto de respaldo.
     *
     * @param logoPath Ruta relativa al archivo de imagen del logo.
     */
    private JLabel buildLogoLabel(String logoPath) {
        try {
            ImageIcon raw = new ImageIcon(logoPath);
            if (raw.getIconWidth() > 0) {
                Image scaled = raw.getImage()
                        .getScaledInstance(200, 110, Image.SCALE_SMOOTH);
                JLabel lbl = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
                lbl.setMaximumSize(new Dimension(560, 120));
                return lbl;
            }
        } catch (Exception ignored) {}
        // Respaldo: texto "UNO" en rojo si no se encuentra el asset
        JLabel fallback = new JLabel("UNO", SwingConstants.CENTER);
        fallback.setFont(new Font("SansSerif", Font.BOLD, 64));
        fallback.setForeground(new Color(220, 38, 38));
        fallback.setMaximumSize(new Dimension(560, 100));
        return fallback;
    }

    /**
     * Construye el campo de nombre con el ícono de lápiz a la derecha.
     * Usa un JPanel con BorderLayout para que el campo se estire al ancho
     * disponible automáticamente, sin coordenadas absolutas.
     */
    private JPanel buildNameRow() {
        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(6, 12, 6, 8)
        ));
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        nameField.setBackground(new Color(248, 248, 250));
        nameField.setForeground(new Color(30, 30, 30));

       

        // Panel que agrupa campo + lápiz con borde compartido
        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(new Color(248, 248, 250));
        fieldWrapper.setBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        fieldWrapper.add(nameField, BorderLayout.CENTER);
        

        row.add(fieldWrapper, BorderLayout.CENTER);
        return row;
    }

    /**
     * Construye el panel de avatares usando GridLayout para garantizar que los 9
     * avatares siempre aparezcan en una sola fila, sin importar el ancho de la ventana.
     * GridLayout(1, N) distribuye el espacio equitativamente entre las N columnas.
     */
    private JPanel buildAvatarPanel() {
        // GridLayout de 1 fila y 9 columnas — los 9 avatares siempre en una sola fila
        JPanel panel = new JPanel(new GridLayout(1, AVATAR_EMOJIS.length, 6, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(700, 70));

        for (int i = 0; i < AVATAR_EMOJIS.length; i++) {
            final int index = i;
            JButton btn = createAvatarButton(AVATAR_EMOJIS[i], AVATAR_COLORS[i], false);
            btn.putClientProperty("emoji", AVATAR_EMOJIS[i]);
            btn.putClientProperty("color", AVATAR_COLORS[i]);
            btn.addActionListener(e -> {
                if (controller != null) controller.onAvatarSelected(index);
            });
            avatarButtons[i] = btn;
            panel.add(btn);
        }
        return panel;
    }

    /**
     * Crea un botón de avatar dibujando un círculo de color con el emoji centrado.
     *
     * @param emoji    El emoji a mostrar.
     * @param bgColor  El color de fondo del círculo.
     * @param selected Si debe mostrarse con borde dorado de selección.
     */
    private JButton createAvatarButton(String emoji, Color bgColor, boolean selected) {
        // Tamaño reducido a 58px para que los 9 avatares quepan en una sola fila
        // dentro de un panel de 600px: 9*58 + 8*6 = 522 + 48 = 570px < 600px
        int totalSize = 58;
        int circleSize = selected ? 50 : 54;
        int offset = selected ? 4 : 2;

        BufferedImage img = new BufferedImage(totalSize, totalSize,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Borde dorado si está seleccionado
        if (selected) {
            g2.setColor(new Color(248, 196, 50));
            g2.fillOval(0, 0, totalSize, totalSize);
        }

        // Círculo de fondo del avatar
        g2.setColor(bgColor);
        g2.fillOval(offset, offset, circleSize, circleSize);

        // Emoji centrado dentro del círculo
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        FontMetrics fm = g2.getFontMetrics();
        int textX = offset + (circleSize - fm.stringWidth(emoji)) / 2;
        int textY = offset + (circleSize - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.BLACK);
        g2.drawString(emoji, textX, textY);
        g2.dispose();

        JButton btn = new JButton(new ImageIcon(img));
        btn.setPreferredSize(new Dimension(totalSize, totalSize));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(emoji);
        return btn;
    }

    /**
     * Aplica los estilos visuales al botón SIGUIENTE.
     */
    private void styleContinueButton() {
        continueButton.setBackground(new Color(220, 38, 38));
        continueButton.setForeground(Color.WHITE);
        continueButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        continueButton.setFocusPainted(false);
        continueButton.setBorderPainted(false);
        continueButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        continueButton.setOpaque(true);
        continueButton.setPreferredSize(new Dimension(120, 40));
    }

    // ─────────────────────────────────────────────
    // Métodos que el Controller llama para actualizar la View
    // ─────────────────────────────────────────────

    /**
     * Resalta el avatar seleccionado con borde dorado y quita el de los demás.
     *
     * @param index El índice del avatar seleccionado.
     */
    public void highlightAvatar(int index) {
        selectedAvatarIndex = index;
        for (int i = 0; i < avatarButtons.length; i++) {
            String emoji = (String) avatarButtons[i].getClientProperty("emoji");
            Color  color = (Color)  avatarButtons[i].getClientProperty("color");
            boolean selected = (i == index);
            JButton newBtn = createAvatarButton(emoji, color, selected);
            newBtn.putClientProperty("emoji", emoji);
            newBtn.putClientProperty("color", color);
            final int idx = i;
            newBtn.addActionListener(e -> {
                if (controller != null) controller.onAvatarSelected(idx);
            });
            // Reemplazar el ícono sin reconstruir el botón
            avatarButtons[i].setIcon(newBtn.getIcon());
        }
    }

    /**
     * Muestra un mensaje de error debajo del campo de nombre.
     *
     * @param message El mensaje de error, o vacío para limpiar.
     */
    public void showError(String message) {
        errorLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    /**
     * Marca un avatar como "en uso": lo oscurece y deshabilita.
     *
     * @param avatarId El identificador del avatar a deshabilitar.
     */
    public void markAvatarTaken(String avatarId) {
        for (int i = 0; i < AVATAR_IDS.length; i++) {
            if (AVATAR_IDS[i].equals(avatarId)) {
                avatarButtons[i].setEnabled(false);
                avatarButtons[i].setToolTipText("En uso por otro jugador");
                String emoji = (String) avatarButtons[i].getClientProperty("emoji");
                // Redibujar con colores desaturados
                int totalSize = 56;
                BufferedImage img = new BufferedImage(totalSize, totalSize,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 180, 180));
                g2.fillOval(2, 2, 54, 54);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(120, 120, 120));
                g2.drawString(emoji,
                    2 + (54 - fm.stringWidth(emoji)) / 2,
                    2 + (54 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
                avatarButtons[i].setIcon(new ImageIcon(img));
            }
        }
    }

    // ─────────────────────────────────────────────
    // Datos que el Controller lee de la View
    // ─────────────────────────────────────────────

    /** @return El nombre ingresado sin espacios extremos. */
    public String getPlayerName() {
        return nameField.getText().trim();
    }

    /** @return El ID del avatar seleccionado, o {@code null} si ninguno. */
    public String getSelectedAvatarId() {
        if (selectedAvatarIndex < 0) return null;
        return AVATAR_IDS[selectedAvatarIndex];
    }

    // ─────────────────────────────────────────────
    // Registro del Controller
    // ─────────────────────────────────────────────

    /**
     * Asigna el Controller y conecta los listeners de los botones.
     *
     * @param controller El controller de esta pantalla.
     */
    public void setController(RegisterController controller) {
        this.controller = controller;
        continueButton.addActionListener(e -> controller.onContinueClicked());
        nameField.addActionListener(e -> controller.onContinueClicked());
    }
}
