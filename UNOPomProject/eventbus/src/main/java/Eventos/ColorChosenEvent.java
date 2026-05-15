package Eventos;

import Dominio.Card;
import Dominio.Player;

/**
 * Evento: un jugador eligió un color tras jugar un comodín WILD.
 *
 * <p>
 * La GUI muestra un selector de color cuando el jugador juega WILD. El color
 * elegido viaja como este evento para que el GameModel del Host actualice la
 * carta activa con el nuevo color.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class ColorChosenEvent extends GameEvent {

    private final Player player;
    private final Card.Color chosenColor;

    /**
     * Constructor para ColorChosenEvent
     *
     * @param player El jugador que eligió.
     * @param chosenColor El color elegido.
     */
    public ColorChosenEvent(Player player, Card.Color chosenColor) {
        super();
        this.player = player;
        this.chosenColor = chosenColor;
    }

    /**
     * Regresa el jugador que eligió el color.
     *
     * @return El jugador que eligió el color.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Regresa el color elegido.
     *
     * @return El color elegido.
     */
    public Card.Color getChosenColor() {
        return chosenColor;
    }
}
