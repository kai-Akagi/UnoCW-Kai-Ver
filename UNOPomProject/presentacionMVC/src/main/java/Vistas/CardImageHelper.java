package Vistas;

import Dominio.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Cargador y caché de imágenes para las cartas especiales de UNO.
 *
 * <p>
 * Las imágenes viven en {@code src/main/resources/img/} y Maven las copia al
 * classpath en compilación. Esta clase las carga <b>una sola vez</b> y las
 * guarda en un mapa para no releer disco en cada render.
 *
 * <p>
 * <b>Decisiones de diseño:</b>
 * <ul>
 * <li><b>Sin preservación de proporción:</b> las imágenes se estiran para
 * rellenar exactamente el rectángulo de la carta (width x height). Así todas
 * las cartas llenan el mismo espacio sin importar las dimensiones originales
 * del PNG (las REVERSE eran 512x512 cuadradas, las demás ~2:3); todas quedan
 * igual de grandes.</li>
 * <li><b>Sin gris al deshabilitar:</b> Swing aplica automáticamente un
 * GrayFilter a los ImageIcon cuando el botón está deshabilitado. Para evitarlo,
 * dibujamos la imagen sobre un BufferedImage propio; Swing no reconoce ese
 * bitmap como "el mismo icono" y no lo filtra.</li>
 * </ul>
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class CardImageHelper {

    private static final String IMG_PATH = "/img/";

    /**
     * Caché: imageKey → Image original. null = recurso no existe.
     */
    private static final Map<String, Image> originalCache = new HashMap<>();

    private CardImageHelper() {
    }

    // ── API pública ───────────────────────────────────────────────────────
    /**
     * Devuelve un ImageIcon escalado EXACTAMENTE a width x height, sin gris
     * aunque el botón esté deshabilitado.
     *
     * @param card La carta cuya imagen se quiere.
     * @param width Ancho exacto en píxeles.
     * @param height Alto exacto en píxeles.
     * @return El icono, o null si la carta es numérica o el recurso no existe.
     */
    public static ImageIcon getScaledIcon(Card card, int width, int height) {
        if (card == null) {
            return null;
        }
        String key = card.getImageKey();
        if (key == null) {
            return null;
        }
        return buildIcon(key, width, height);
    }

    /**
     * Variante para cuando solo se tiene la clave (reconstrucción desde red).
     *
     * @param imageKey Clave sin extensión, ej. "SKIP_RED", "WILD".
     * @param width Ancho exacto.
     * @param height Alto exacto.
     * @return El icono, o null si no existe el recurso.
     */
    public static ImageIcon getScaledIcon(String imageKey, int width, int height) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        return buildIcon(imageKey, width, height);
    }

    /**
     * Indica si la carta tiene imagen disponible.
     */
    public static boolean hasImage(Card card) {
        return card != null && card.getImageKey() != null;
    }

    // ── Implementación interna ────────────────────────────────────────────
    /**
     * Dibuja la imagen estirada a w x h sobre un BufferedImage propio. Al ser
     * un bitmap nuevo, Swing NO aplica GrayFilter aunque el botón receptor esté
     * deshabilitado.
     */
    private static ImageIcon buildIcon(String key, int w, int h) {
        Image original = originalCache.computeIfAbsent(key, CardImageHelper::loadImage);
        if (original == null) {
            return null;
        }

        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // Estirar a dimensiones exactas (sin preservar proporción)
        g.drawImage(original, 0, 0, w, h, null);
        g.dispose();

        return new ImageIcon(buf);
    }

    /**
     * Carga el PNG del classpath de forma síncrona (MediaTracker espera a que
     * la carga termine antes de devolver).
     */
    private static Image loadImage(String key) {
        String resource = IMG_PATH + key + ".png";
        URL url = CardImageHelper.class.getResource(resource);
        if (url == null) {
            System.err.println("[CardImageHelper] No encontrada: " + resource);
            return null;
        }
        Image img = Toolkit.getDefaultToolkit().createImage(url);
        MediaTracker mt = new MediaTracker(new Canvas());
        mt.addImage(img, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ignored) {
        }
        return img;
    }
}
