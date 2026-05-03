package uno.gui.register;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Pantalla de registro del jugador.
 *
 * <p>Muestra el formulario donde el jugador ingresa su nombre y selecciona
 * un avatar antes de entrar al lobby.
 *
 * <p><b>Rol en MVC: View (Vista)</b><br>
 * Esta clase solo dibuja componentes Swing. No tiene lógica de negocio,
 * no valida datos y no conoce al EventBus. Solo:
 * <ul>
 *   <li>Muestra los campos al usuario.</li>
 *   <li>Llama al Controller cuando el usuario hace algo.</li>
 *   <li>Actualiza su apariencia cuando el Controller se lo pide.</li>
 * </ul>
 *
 * <p>La comunicación con el Controller ocurre a través de métodos simples
 * como {@link #getPlayerName()} y {@link #getSelectedAvatarId()}, y de
 * listeners que el Controller registra con {@link #setController}.
 */
public class RegisterView extends JPanel {

    // ─── Avatares disponibles ────────────────────────────────────────────────
    /** Identificadores de los avatares disponibles. */
    private static final String[] AVATAR_IDS = {
        "avatar_pig", "avatar_bear", "avatar_panda", "avatar_bunny",
        "avatar_fox",  "avatar_penguin", "avatar_chick", "avatar_wolf", "avatar_frog"
    };

    /** Emojis usados para representar los avatares en texto (mientras no hay imágenes). */
    private static final String[] AVATAR_EMOJIS = {
        "🐷", "🐻", "🐼", "🐰", "🦊", "🐧", "🐥", "🐺", "🐸"
    };

    // ─── Componentes Swing ───────────────────────────────────────────────────
    private final JTextField nameField;
    private final JLabel     errorLabel;
    private final JButton    continueButton;
    private final JButton[]  avatarButtons;

    /** El índice del avatar actualmente seleccionado. -1 = ninguno. */
    private int selectedAvatarIndex = -1;

    /** Referencia al controller. La View la usa para delegar acciones. */
    private RegisterController controller;

    /**
     * Construye la pantalla de registro con todos sus componentes.
     */
    public RegisterView() {
        this.avatarButtons = new JButton[AVATAR_IDS.length];
        this.nameField     = new JTextField(20);
        this.errorLabel    = new JLabel(" "); // espacio para que no cambie el tamaño
        this.continueButton = new JButton("SIGUIENTE →");

        buildUI();
    }

    /**
     * Construye y organiza todos los componentes visuales.
     */
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46));
        setBorder(new EmptyBorder(30, 60, 30, 60));

        // ── Panel central ──
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(30, 30, 46));

        // Logo / título
        JLabel title = new JLabel("🃏 UNO", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(new Color(248, 216, 71)); // amarillo UNO
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtítulo
        JLabel subtitle = new JLabel("Registra tu jugador", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Campo de nombre ──
        JLabel nameLabel = new JLabel("Nombre de Jugador:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        nameField.setMaximumSize(new Dimension(300, 40));
        nameField.setHorizontalAlignment(JTextField.CENTER);

        // ── Mensaje de error ──
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        errorLabel.setForeground(new Color(252, 100, 100));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Galería de avatares ──
        JLabel avatarLabel = new JLabel("Selecciona tu avatar:");
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 14));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatarPanel = buildAvatarPanel();
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Botón continuar ──
        styleButton(continueButton, new Color(220, 38, 38)); // rojo UNO
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.setMaximumSize(new Dimension(200, 45));

        // ── Ensamblar ──
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(subtitle);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(nameField);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(errorLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(avatarLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(avatarPanel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(continueButton);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Construye el panel de botones de avatar.
     * Cada botón muestra un emoji y se resalta al seleccionarlo.
     *
     * @return El panel con la galería de avatares.
     */
    private JPanel buildAvatarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panel.setBackground(new Color(30, 30, 46));

        for (int i = 0; i < AVATAR_EMOJIS.length; i++) {
            final int index = i;
            JButton btn = new JButton(AVATAR_EMOJIS[i]);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            btn.setPreferredSize(new Dimension(56, 56));
            btn.setBackground(new Color(51, 51, 72));
            btn.setBorderPainted(true);
            btn.setFocusPainted(false);
            btn.setToolTipText("Avatar " + (i + 1));

            // Al hacer clic, seleccionar este avatar
            btn.addActionListener(e -> {
                if (controller != null) controller.onAvatarSelected(index);
            });

            avatarButtons[i] = btn;
            panel.add(btn);
        }

        return panel;
    }

    // ─────────────────────────────────────────────
    // Métodos que el Controller llama para actualizar la View
    // ─────────────────────────────────────────────

    /**
     * Resalta el avatar seleccionado y quita el resaltado de los demás.
     * El Controller llama a esto cuando el usuario hace clic en un avatar.
     *
     * @param index El índice del avatar seleccionado.
     */
    public void highlightAvatar(int index) {
        selectedAvatarIndex = index;
        for (int i = 0; i < avatarButtons.length; i++) {
            if (i == index) {
                // Borde dorado para el seleccionado
                avatarButtons[i].setBackground(new Color(248, 216, 71));
                avatarButtons[i].setBorder(
                    BorderFactory.createLineBorder(new Color(248, 216, 71), 3));
            } else {
                avatarButtons[i].setBackground(new Color(51, 51, 72));
                avatarButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }

    /**
     * Muestra un mensaje de error debajo del campo de nombre.
     * Pasa cadena vacía o " " para limpiar el error.
     *
     * @param message El mensaje de error a mostrar.
     */
    public void showError(String message) {
        errorLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    /**
     * Marca un avatar como "en uso" (ya elegido por otro jugador).
     * Lo deshabilita visualmente para que el usuario no lo seleccione.
     *
     * @param avatarId El identificador del avatar a deshabilitar.
     */
    public void markAvatarTaken(String avatarId) {
        for (int i = 0; i < AVATAR_IDS.length; i++) {
            if (AVATAR_IDS[i].equals(avatarId)) {
                avatarButtons[i].setEnabled(false);
                avatarButtons[i].setToolTipText("En uso por otro jugador");
                avatarButtons[i].setBackground(new Color(30, 30, 46));
            }
        }
    }

    // ─────────────────────────────────────────────
    // Datos que el Controller lee de la View
    // ─────────────────────────────────────────────

    /**
     * Devuelve el texto ingresado en el campo de nombre.
     * El Controller lo usa para validar antes de continuar.
     *
     * @return El nombre ingresado, sin espacios extremos.
     */
    public String getPlayerName() {
        return nameField.getText().trim();
    }

    /**
     * Devuelve el identificador del avatar actualmente seleccionado.
     *
     * @return El ID del avatar, o {@code null} si no hay ninguno seleccionado.
     */
    public String getSelectedAvatarId() {
        if (selectedAvatarIndex < 0) return null;
        return AVATAR_IDS[selectedAvatarIndex];
    }

    // ─────────────────────────────────────────────
    // Registro del Controller
    // ─────────────────────────────────────────────

    /**
     * Asigna el Controller de esta pantalla.
     * Conecta el botón "SIGUIENTE" al método del Controller.
     *
     * @param controller El controller que manejará las acciones de esta View.
     */
    public void setController(RegisterController controller) {
        this.controller = controller;

        // Conectar el botón continuar al controller
        continueButton.addActionListener(
            e -> controller.onContinueClicked()
        );

        // También permitir presionar Enter en el campo de nombre
        nameField.addActionListener(
            e -> controller.onContinueClicked()
        );
    }

    // ─────────────────────────────────────────────
    // Utilidades de estilo
    // ─────────────────────────────────────────────

    /**
     * Aplica un estilo consistente a un botón de acción.
     *
     * @param button El botón a estilizar.
     * @param color  El color de fondo del botón.
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }
}
