package Eventos;

import Dominio.Card;
import Dominio.Player;

/**
 * Fábrica central de eventos del juego.
 *
 * <p>
 * Es el único punto de creación de eventos en todo el sistema. Cualquier clase
 * que quiera publicar un evento en el EventBus lo solicita aquí. Esto
 * centraliza la creación y hace que cambiar los datos de un evento solo
 * requiera modificar este archivo.
 *
 * <p>
 * <b>Patrón Factory:</b> ocultamos los constructores de cada evento detrás de
 * métodos con nombres descriptivos. El código que llama a
 * {@code GameEventFactory.turnChanged(player, card)} es más legible que
 * {@code new TurnChangedEvent(player, card)}.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class GameEventFactory {

    /**
     * Constructor privado: clase de utilidad, no se instancia.
     */
    private GameEventFactory() {
    }

    // ─────────────────────────────────────────────
    // Eventos del juego
    // ─────────────────────────────────────────────
    /**
     * Crea el evento de que un jugador jugó una carta.
     *
     * @param player El jugador que jugó.
     * @param card La carta jugada.
     *
     * @return Una nueva instancia de CardPlayedEvent.
     */
    public static CardPlayedEvent cardPlayed(Player player, Card card) {
        return new CardPlayedEvent(player, card);
    }

    /**
     * Crea el evento público de que un jugador robó una carta. Este evento
     * llega a todos los peers (sin revelar la carta).
     *
     * @param player El jugador que robó.
     *
     * @return Una nueva instancia de CardDrawnPublicEvent.
     */
    public static CardDrawnPublicEvent cardDrawnPublic(Player player) {
        return new CardDrawnPublicEvent(player);
    }

    /**
     * Crea el evento privado de carta robada. Solo llega al jugador que robó
     * (contiene la carta real).
     *
     * @param player El jugador que robó.
     * @param card La carta que tomó del mazo.
     *
     * @return Una nueva instancia de CardDrawnPrivateEvent.
     */
    public static CardDrawnPrivateEvent cardDrawnPrivate(Player player, Card card) {
        return new CardDrawnPrivateEvent(player, card);
    }

    /**
     * Crea el evento de cambio de turno.
     *
     * <p>
     * <b>Corrección respecto a Fase 2:</b> ahora incluye la carta activa para
     * que MessageSerializer pueda serializarla y los peers actualicen su vista
     * sin pedir el estado completo al Host.
     *
     * @param currentPlayer El jugador que ahora tiene el turno.
     * @param topCard La carta activa en la mesa.
     * @param clockwise Indica si el turno avanza en sentido horario.
     * @return Una nueva instancia de TurnChangedEvent.
     */
    public static TurnChangedEvent turnChanged(Player currentPlayer, Card topCard, boolean clockwise) {
        return new TurnChangedEvent(currentPlayer, topCard, clockwise);
    }

    /**
     * Crea el evento de que un nuevo jugador se unió al lobby.
     *
     * @param player El jugador que se unió.
     *
     * @return Una nueva instancia de PlayerJoinedEvent
     */
    public static PlayerJoinedEvent playerJoined(Player player) {
        return new PlayerJoinedEvent(player);
    }

    /**
     * Crea el evento de cambio de estado "Listo" de un jugador.
     *
     * @param playerName El nombre del jugador.
     * @param ready {@code true} si se marcó como listo.
     *
     * @return Una nueva instancia de PlayerReadyEvent
     */
    public static PlayerReadyEvent playerReady(String playerName, boolean ready) {
        return new PlayerReadyEvent(playerName, ready);
    }

    /**
     * Crea el evento de que un jugador se desconectó.
     *
     * @param playerName El nombre del jugador que se fue.
     *
     * @return Una nueva instancia de PlayerDisconnectedEvent
     */
    public static PlayerDisconnectedEvent playerDisconnected(String playerName) {
        return new PlayerDisconnectedEvent(playerName);
    }

    /**
     * Crea el evento de que un jugador gritó UNO.
     *
     * @param player El jugador que gritó UNO.
     *
     * @return Una nueva instancia de UnoCalledEvent
     */
    public static UnoCalledEvent unoCalled(Player player) {
        return new UnoCalledEvent(player);
    }

    /**
     * Crea el evento de fin de partida.
     *
     * @param winner El jugador ganador.
     *
     * @return Una nueva instancia de GameOverEvent
     */
    public static GameOverEvent gameOver(Player winner) {
        return new GameOverEvent(winner);
    }

    /**
     * Crea el evento de inicio de partida.
     *
     * @return Una nueva instancia de GameStartedEvent
     */
    public static GameStartedEvent gameStarted() {
        return new GameStartedEvent();
    }

    /**
     * Crea el evento de color elegido tras jugar un comodín.
     *
     * @param player El jugador que eligió.
     * @param chosenColor El color elegido.
     *
     * @return Una nueva instancia de ColorChosenEvent
     */
    public static ColorChosenEvent colorChosen(Player player, Card.Color chosenColor) {
        return new ColorChosenEvent(player, chosenColor);
    }

    /**
     * Crea el evento de solicitud de robo de carta.
     *
     * <p>
     * Lo publica el Peer cuando presiona el botón "Robar". Representa una
     * intención, no el resultado: el Host decide qué carta sale del mazo y se
     * lo comunica de vuelta al Peer.
     *
     * @param player El jugador que quiere robar.
     *
     * @return Una nueva instancia de drawCardRequest
     */
    public static DrawCardRequestEvent drawCardRequest(Player player) {
        return new DrawCardRequestEvent(player);
    }

    /**
     * Crea el evento de solicitud de inicio por parte de un Peer.
     *
     * @param requesterName El nombre del peer que solicita iniciar.
     *
     * @return Una nueva instancia de StartRequestedEvent
     */
    public static StartRequestedEvent startRequested(String requesterName) {
        return new StartRequestedEvent(requesterName);
    }

    /**
     * Crea el evento de cierre del lobby.
     *
     * @param reason La razón del cierre.
     *
     * @return Una nueva instancia de LobbyClosedEvent
     */
    public static LobbyClosedEvent lobbyClosed(String reason) {
        return new LobbyClosedEvent(reason);
    }

    // ─────────────────────────────────────────────
    // Eventos de red (recibidos desde sockets)
    // ─────────────────────────────────────────────
    /**
     * Crea el evento de red de cambio de turno. Contiene solo texto (no objetos
     * Java) porque viene deserializado del mensaje JSON recibido por socket.
     *
     * @param currentPlayerName Nombre del jugador activo.
     * @param topCardText Carta activa como texto (ej. "RED-7").
     * @param clockwise Indica si el turno avanza en sentido horario.
     * @param handSizes Mapa con la cantidad de cartas de cada jugador.
     * @param activeColor Color activo actual del juego.
     *
     * @return Una nueva instancia de NetworkTurnChangedEvent
     */
    public static NetworkTurnChangedEvent networkTurnChanged(
            String currentPlayerName, String topCardText, boolean clockwise,
            java.util.Map<String, Integer> handSizes, Dominio.Card.Color activeColor) {
        return new NetworkTurnChangedEvent(currentPlayerName, topCardText, clockwise,
                handSizes, activeColor);
    }

    /**
     * Crea el evento de red de carta robada (privado).
     *
     * @param playerName Nombre del jugador que robó.
     * @param cardText Carta robada como texto.
     *
     * @return Una nueva instancia de NetworkTurnChangedEvent
     */
    public static NetworkCardDrawnPrivateEvent networkCardDrawnPrivate(
            String playerName, String cardText) {
        return new NetworkCardDrawnPrivateEvent(playerName, cardText);
    }

    /**
     * Crea el evento de red de UNO gritado.
     *
     * @param playerName Nombre del jugador que gritó UNO.
     *
     * @return Una nueva instancia de NetworkUnoCalledEvent
     */
    public static NetworkUnoCalledEvent networkUnoCalled(String playerName) {
        return new NetworkUnoCalledEvent(playerName);
    }
}
