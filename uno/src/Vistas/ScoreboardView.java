package Vistas;

import Main.MainWindow;
import Controles.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pantalla de resultados finales (Scoreboard).
 *
 * <p>Muestra al ganador de la partida y un botón para volver al menú principal.
 * Es la pantalla más simple del proyecto: solo presenta datos, no interactúa
 * con el bus ni con la red.
 *
 * <p>En esta pantalla el patrón MVC se simplifica: View y Controller
 * están unidos porque la lógica es mínima (solo un botón). Para pantallas
 * tan simples, separar en dos clases sería sobreingeniería.
 */
public class ScoreboardView extends JPanel {

    /**
     * Construye la pantalla del scoreboard.
     *
     * @param winnerName El nombre del jugador ganador.
     * @param session    La sesión actual (para mostrar al jugador local).
     * @param mainWindow La ventana principal para navegar de vuelta al inicio.
     */
    public ScoreboardView(String winnerName, GameSession session, MainWindow mainWindow) {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 38, 38)); // fondo rojo UNO
        setBorder(new EmptyBorder(40, 60, 40, 60));

        // Panel central blanco
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Título
        JLabel title = new JLabel("¡VICTORIA!", SwingConstants.CENTER);
        title.setFont(new Font("Arial Black", Font.BOLD, 36));
        title.setForeground(new Color(220, 38, 38));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtítulo
        JLabel subtitle = new JLabel("El ganador es...", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel del ganador (fondo dorado)
        JPanel winnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        winnerPanel.setBackground(new Color(234, 179, 8));
        winnerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
        winnerPanel.setMaximumSize(new Dimension(400, 70));
        winnerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerLabel = new JLabel("🏆 " + winnerName);
        winnerLabel.setFont(new Font("Arial Black", Font.BOLD, 22));
        winnerLabel.setForeground(Color.WHITE);
        winnerPanel.add(winnerLabel);

        // Mensaje personalizado
        boolean localWon = session.getLocalPlayer().getName().equals(winnerName);
        JLabel personalMsg = new JLabel(
            localWon ? "¡Felicidades, ganaste!" : "¡Mejor suerte la próxima vez!",
            SwingConstants.CENTER
        );
        personalMsg.setFont(new Font("Arial", Font.ITALIC, 15));
        personalMsg.setForeground(new Color(80, 80, 80));
        personalMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón volver
        JButton backButton = new JButton("Volver al Menú");
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(new Color(220, 38, 38));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setOpaque(true);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(200, 45));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e ->
            SwingUtilities.invokeLater(() ->
                mainWindow.showRegister()
            )
        );

        // Ensamblar
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));
        card.add(winnerPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(personalMsg);
        card.add(Box.createVerticalStrut(30));
        card.add(backButton);

        add(card, BorderLayout.CENTER);
    }
}
