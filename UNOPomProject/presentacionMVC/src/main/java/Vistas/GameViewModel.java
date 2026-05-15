package Vistas;

import Dominio.Card;
import Dominio.GameState;
import Dominio.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * El modelo de vista de la pantalla del juego.
 *
 * <p>
 * Contiene exactamente los datos que {@link GameView} necesita para dibujarse.
 * Ni más ni menos. Se construye a partir del {@link GameState} en el
 * {@link GameController} y se le entrega a la View para que la renderice.
 *
 * <p>
 * <b>¿Por qué existe este objeto?</b><br>
 * La View no debe conocer al {@link GameState} directamente porque:
 * <ul>
 * <li>GameState tiene lógica de dominio (índices, listas mutables) que no le
 * corresponde a la View manejar.</li>
 * <li>Algunos datos deben filtrarse: la View nunca debe ver las cartas reales
 * de los oponentes, solo cuántas tienen.</li>
 * <li>Si GameState cambia su estructura, solo GameController necesita
 * actualizarse, no la View.</li>
 * </ul>
 *
 * <p>
 * Este objeto es inmutable una vez creado: la View lo lee pero no lo modifica.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameViewModel {

    /**
     * Nombre del jugador que tiene el turno actualmente.
     */
    public final String currentPlayerName;

    /**
     * Si el turno actual es del jugador local de esta instancia.
     */
    public final boolean isMyTurn;

    /**
     * Texto de la carta activa en la mesa (ej. "SKIP", "7").
     */
    public final String topCardValue;

    /**
     * Color de la carta activa en la mesa.
     */
    public final Card.Color topCardColor;

    /**
     * Las cartas que tiene el jugador local en su mano.
     */
    public final List<Card> localHand;

    /**
     * Nombres de los oponentes (para mostrar en los paneles).
     */
    public final List<String> opponentNames;

    /**
     * Cuántas cartas tiene cada oponente (en el mismo orden que opponentNames).
     */
    public final List<Integer> opponentHandSizes;

    /**
     * Si el jugador local tiene exactamente una carta (debe gritar UNO).
     */
    public final boolean localPlayerHasUno;

    /**
     * Si el juego va en sentido normal o invertido (para el indicador visual).
     */
    public final boolean clockwise;

    /**
     * Construye el modelo de vista a partir del estado del juego.
     *
     * <p>
     * El filtrado ocurre aquí: los oponentes solo exponen cuántas cartas
     * tienen, nunca cuáles.
     *
     * @param state El estado actual del juego (fuente de verdad).
     * @param localPlayer El jugador local de esta instancia.
     */
    public GameViewModel(GameState state, Player localPlayer) {
        Player current = state.getCurrentPlayer();

        this.currentPlayerName = current.getName();
        this.isMyTurn = current.getName().equals(localPlayer.getName());
        this.topCardValue = state.getTopCard() != null
                ? state.getTopCard().getValue() : "?";
        this.topCardColor = state.getTopCard() != null
                ? state.getTopCard().getColor() : Card.Color.WILD;
        this.localHand = new ArrayList<>(localPlayer.getHand());
        this.localPlayerHasUno = localPlayer.hasUno();
        this.clockwise = state.isClockwise();

        // Filtrar oponentes: solo nombre y cantidad de cartas
        this.opponentNames = new ArrayList<>();
        this.opponentHandSizes = new ArrayList<>();

        for (Player p : state.getPlayers()) {
            if (!p.getName().equals(localPlayer.getName())) {
                opponentNames.add(p.getName());
                opponentHandSizes.add(p.getHandSize());
            }
        }
    }

    /**
     * Constructor alternativo para actualizar desde datos de red (cuando no
     * tenemos el GameState completo, solo los campos que mandó el Host).
     *
     * @param currentPlayerName Nombre del jugador activo.
     * @param topCardValue Valor de la carta activa.
     * @param topCardColor Color de la carta activa.
     * @param localHand Cartas en mano del jugador local.
     * @param localPlayerName Nombre del jugador local.
     * @param opponentNames Nombres de oponentes.
     * @param opponentHandSizes Cantidad de cartas de cada oponente.
     * @param clockwise Dirección del juego.
     */
    public GameViewModel(
            String currentPlayerName, String topCardValue, Card.Color topCardColor,
            List<Card> localHand, String localPlayerName,
            List<String> opponentNames, List<Integer> opponentHandSizes,
            boolean clockwise) {
        this.currentPlayerName = currentPlayerName;
        this.isMyTurn = currentPlayerName.equals(localPlayerName);
        this.topCardValue = topCardValue;
        this.topCardColor = topCardColor;
        this.localHand = localHand;
        this.localPlayerHasUno = localHand.size() == 1;
        this.opponentNames = opponentNames;
        this.opponentHandSizes = opponentHandSizes;
        this.clockwise = clockwise;
    }
}
