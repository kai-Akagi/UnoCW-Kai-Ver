/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vistas;

import java.awt.Color;

/**
 * Utilidad central para convertir un avatarId en su emoji y color
 * correspondientes.
 *
 * <p>Los mismos 9 avatares que el jugador elige en RegisterView deben
 * mostrarse correctamente en SalaEspera y GameView. Esta clase centraliza
 * esa conversión para que no haya que duplicar los arreglos en cada vista.</p>
 *
 * <p>Si en el futuro se añade un avatar nuevo, solo se modifica esta clase.</p>
 * 
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 * 
 * 
 */
public class AvatarHelper {

    /** Identificadores de avatar en el mismo orden que los arreglos. */
    private static final String[] AVATAR_IDS = {
        "avatar_pig", "avatar_bear", "avatar_panda", "avatar_bunny",
        "avatar_fox", "avatar_penguin", "avatar_chick", "avatar_wolf", "avatar_frog"
    };

    /** Emoji que corresponde a cada avatarId. */
    private static final String[] AVATAR_EMOJIS = {
        "🐷", "🐻", "🐼", "🐰", "🦊", "🐧", "🐥", "🐺", "🐸"
    };

    /** Color de fondo que corresponde a cada avatarId. */
    private static final Color[] AVATAR_COLORS = {
        new Color(255, 182, 193),
        new Color(210, 180, 140),
        new Color(220, 220, 220),
        new Color(255, 209, 220),
        new Color(255, 200, 150),
        new Color(173, 216, 230),
        new Color(255, 230, 150),
        new Color(200, 200, 200),
        new Color(144, 238, 144)
    };

    /** Emoji de respaldo cuando el avatarId no se reconoce. */
    private static final String FALLBACK_EMOJI = "🎮";

    /** Color de respaldo. */
    private static final Color FALLBACK_COLOR = new Color(180, 180, 180);

    private AvatarHelper() {}

    /**
     * Devuelve el emoji que corresponde al avatarId dado.
     *
     * @param avatarId El identificador del avatar (ej. "avatar_fox").
     * @return El emoji correspondiente, o "🎮" si no se reconoce.
     */
    public static String emojiFor(String avatarId) {
        for (int i = 0; i < AVATAR_IDS.length; i++) {
            if (AVATAR_IDS[i].equals(avatarId)) {
                return AVATAR_EMOJIS[i];
            }
        }
        return FALLBACK_EMOJI;
    }

    /**
     * Devuelve el color de fondo que corresponde al avatarId dado.
     *
     * @param avatarId El identificador del avatar.
     * @return El color correspondiente, o gris si no se reconoce.
     */
    public static Color colorFor(String avatarId) {
        for (int i = 0; i < AVATAR_IDS.length; i++) {
            if (AVATAR_IDS[i].equals(avatarId)) {
                return AVATAR_COLORS[i];
            }
        }
        return FALLBACK_COLOR;
    }
}
