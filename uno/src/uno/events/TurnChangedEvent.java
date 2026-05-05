package uno.events;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import uno.model.Card;
import uno.model.Player;

/**
 * Evento: el turno pasó al siguiente jugador.
 *
 * <p>Se publica después de que cualquier jugada es procesada por el GameModel:
 * jugar carta, robar, SKIP, REVERSE, etc. La GUI lo usa para resaltar
 * al jugador activo y actualizar la carta en la mesa.
 *
 * <p><b>Corrección respecto a la Fase 2:</b> Se agregó {@code topCard} porque
 * {@link uno.network.MessageSerializer} necesita serializar la carta activa
 * junto con el cambio de turno para que los peers puedan actualizar su vista.
 */
public class TurnChangedEvent extends GameEvent {

    /** El jugador que ahora tiene el turno. */
    private final Player currentPlayer;

    /**
     * La carta activa en la mesa en el momento del cambio de turno.
     * Los peers la necesitan para actualizar su vista sin pedir
     * el estado completo al Host.
     */
    private final Card topCard;

    
    private final Map<String,Integer> handSizes;
    
    /**
     * @param currentPlayer El jugador que ahora debe jugar.
     * @param topCard       La carta activa en la mesa.
     * @param handSizes     Mapa nombre->cantidad de cartas de cada jugador
     */
    public TurnChangedEvent(Player currentPlayer, Card topCard, Map<String, Integer> handSizes) {
        super();
        this.currentPlayer = currentPlayer;
        this.topCard       = topCard;
        this.handSizes     = Collections.unmodifiableMap(
                new LinkedHashMap<>(handSizes));

    }

    /** @return El jugador que tiene el turno ahora. */
    public Player getCurrentPlayer() { return currentPlayer; }

    /** @return La carta activa en la mesa. */
    public Card getTopCard() { return topCard; }
    
    
        /**
     * @return Mapa inmutable de nombre→cantidad de cartas de cada jugador.
     *         Refleja el estado real del GameState del Host.
     */
    public Map<String, Integer> getHandSizes() { return handSizes; }

}
