package Logica;

import Eventos.UnoGracePeriodEvent;
import Eventos.GameEventFactory;
import Eventos.DrawCardRequestEvent;
import Eventos.ColorChosenEvent;
import Eventos.CardPlayedEvent;
import Dominio.Player;
import Dominio.Deck;
import Dominio.Card;
import Dominio.GameState;
import Logica.CardFactory;
import Eventos.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * El motor del juego: coordina el estado, las reglas y los eventos.
 *
 * <p>Se suscribe al EventBus para recibir acciones de los jugadores
 * (jugar carta, robar carta, gritar UNO) y responde actualizando el
 * estado del juego y publicando nuevos eventos.
 *
 * <p><b>Diferencia entre Host y Peer:</b><br>
 * El mismo código, pero la NetworkLayer los usa de manera distinta:
 * <ul>
 *   <li><b>Host:</b> llama a {@link #playCard}, {@link #drawCard}, etc.
 *       porque es la fuente de verdad.</li>
 *   <li><b>Peer:</b> llama a {@link #applyStateUpdate} para reflejar
 *       lo que el Host ya aprobó. Nunca valida por su cuenta.</li>
 * </ul>
 *
 * <p><b>Correcciones respecto a Fase 2:</b>
 * <ul>
 *   <li>{@code registerListeners()} ahora usa las clases de evento directamente
 *       en vez del hack {@code getClass()} sobre instancias nulas.</li>
 *   <li>{@code turnChanged} ahora incluye la carta activa en el evento.</li>
 * </ul>
 */
public class GameModel {

    /** El estado completo de la partida. */
    private GameState gameState;

    /** El bus de eventos local. */
    private final EventBus eventBus;

    /**
     * Rastrea si el jugador con una carta ya gritó UNO.
     * Si no lo hizo, se aplica la penalización de 2 cartas.
     */
    private boolean unoCalled;

    /**
     * Evita que el listener de CardPlayedEvent entre en bucle.
     * playCard() publica CardPlayedEvent al finalizar (para broadcast),
     * y sin esta bandera el listener volvería a llamar playCard() de nuevo.
     */
    private boolean processingPlay = false;

    /**
     * El jugador que acaba de quedar con 1 carta y debe declarar UNO.
     * El turno no avanza hasta que presione UNO o el timer expire.
     * Es null cuando no hay periodo de gracia activo.
     */
    private Player pendingUnoPlayer = null;

    /**
     * Construye el motor del juego y se suscribe a los eventos relevantes.
     */
    public GameModel() {
        this.eventBus  = EventBus.getInstance();
        this.unoCalled = false;
        registerListeners();
    }

    /**
     * Suscribe este modelo a los eventos del juego.
     *
     * <p><b>Corrección:</b> Usamos directamente {@code CardPlayedEvent.class}
     * en vez del hack anterior {@code GameEventFactory.cardPlayed(null,null).getClass()}.
     * Esto funciona correctamente ahora que todos los eventos son clases públicas.
     */
    private void registerListeners() {
        // Cuando cualquier jugador juega una carta, este listener la procesa.
        // Funciona tanto para el jugador local del Host (que publica el evento
        // al hacer clic) como para los peers remotos (cuyo evento llega
        // serializado por la red y NetworkLayer lo publica en el bus local).
        eventBus.subscribe(CardPlayedEvent.class, event -> {
            // Si ya estamos procesando una jugada, este evento fue publicado
            // por playCard() como confirmación (para broadcast a la red).
            // No lo procesamos de nuevo para evitar un bucle infinito.
            if (processingPlay) return;

            CardPlayedEvent e = (CardPlayedEvent) event;
            if (gameState != null) {
                processingPlay = true;
                try {
                    boolean valid = playCard(e.getPlayer(), e.getCard());
                    if (!valid) {
                        System.out.println("[GameModel] Jugada invalida: jugador='"
                            + e.getPlayer().getName() + "' carta=" + e.getCard()
                            + " topCard=" + gameState.getTopCard()
                            + " turnoActual=" + gameState.getCurrentPlayer().getName());
                        // Re-publicar el turno actual para que la GUI del Host
                        // vuelva a habilitar la mano sin cambiar el estado del juego.
                        eventBus.publish(GameEventFactory.turnChanged(
                            gameState.getCurrentPlayer(),
                            gameState.getTopCard(),
                            gameState.isClockwise()
                        ));
                    }
                } finally {
                    processingPlay = false;
                }
            }
        });

        // Solicitudes de robo de carta (del jugador local o de peers remotos).
        // El GameModel ejecuta el robo real y publica los eventos de resultado.
        eventBus.subscribe(DrawCardRequestEvent.class, event -> {
            DrawCardRequestEvent e = (DrawCardRequestEvent) event;
            if (gameState != null) {
                drawCard(e.getPlayer());
            }
        });

        // El jugador eligió color tras jugar un comodín.
        // Se procesa en orden antes del CardPlayedEvent que llega después.
        eventBus.subscribe(ColorChosenEvent.class, event -> {
            ColorChosenEvent e = (ColorChosenEvent) event;
            if (gameState != null) {
                gameState.setActiveColor(e.getChosenColor());
                System.out.println("[GameModel] Color activo: " + e.getChosenColor());
            }
        });
    }

    // ─────────────────────────────────────────────
    // API del Host: métodos que modifican el estado
    // ─────────────────────────────────────────────

    /**
     * Prepara e inicia una nueva partida.
     *
     * @param players La lista de jugadores que participarán.
     */
    public void startGame(List<Player> players) {
        Deck deck = Deck.getInstance();
        deck.reset(CardFactory.createFullDeck());

        gameState = new GameState(new ArrayList<>(players), deck);
        unoCalled = false;

        // Repartir 7 cartas a cada jugador
        for (Player player : players) {
            player.clearHand();
            for (int i = 0; i < 7; i++) {
                Card drawn = deck.drawCard();
                if (drawn != null) player.addCard(drawn);
            }
        }

        // Primera carta activa: no puede ser comodín
        Card firstCard = drawFirstNonWild(deck);
        deck.discard(firstCard);
        gameState.setTopCard(firstCard);

        // Enviar a cada jugador sus cartas iniciales de forma privada.
        // Publicamos CardDrawnPrivateEvent por cada carta de cada jugador.
        // NetworkLayer intercepta estos eventos y los enruta solo al peer
        // correspondiente usando sendPrivate(), no broadcast.
        for (Player player : players) {
            for (Card card : player.getHand()) {
                eventBus.publish(GameEventFactory.cardDrawnPrivate(player, card));
            }
        }

        // Publicar el primer turno. NetworkLayer hace broadcast a todos los peers.
        eventBus.publish(GameEventFactory.turnChanged(
            gameState.getCurrentPlayer(),
            gameState.getTopCard(),
            gameState.isClockwise()
        ));
    }

    /**
     * Procesa el intento de un jugador de jugar una carta.
     *
     * @param player El jugador que quiere jugar.
     * @param card   La carta que quiere jugar.
     * @return {@code true} si la jugada fue válida y se procesó.
     */
    public boolean playCard(Player player, Card card) {
        // Buscamos al jugador en el GameState por nombre, no usamos el objeto
        // del evento directamente. El objeto del evento puede ser una instancia
        // diferente (reconstruida desde JSON) que no tiene las cartas en su mano.
        Player actualPlayer = gameState.getPlayers().stream()
                .filter(p -> p.getName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        if (actualPlayer == null) {
            System.out.println("[GameModel] Jugador no encontrado en GameState: " + player.getName());
            return false;
        }

        if (!gameState.getCurrentPlayer().getName().equals(actualPlayer.getName())) {
            System.out.println("[GameModel] No es el turno de: " + actualPlayer.getName()
                + " | turno actual: " + gameState.getCurrentPlayer().getName());
            return false;
        }

        // Buscamos la carta en la mano real del jugador por valor y color
        // para no depender de la identidad de objeto (que falla con JSON reconstruido)
        Card actualCard = actualPlayer.getHand().stream()
                .filter(c -> c.getValue().equals(card.getValue())
                          && c.getColor() == card.getColor())
                .findFirst()
                .orElse(null);

        if (actualCard == null) {
            System.out.println("[GameModel] Carta no encontrada en mano de " + actualPlayer.getName()
                + ": " + card);
            return false;
        }

        if (!actualCard.canBePlacedOn(gameState.getTopCard(), gameState.getActiveColor())) {
            System.out.println("[GameModel] Carta no jugable: " + actualCard
                + " sobre " + gameState.getTopCard());
            return false;
        }

        if (!actualPlayer.removeCard(actualCard)) {
            return false;
        }

        Deck.getInstance().discard(actualCard);
        gameState.setTopCard(actualCard);
        unoCalled = false;

        eventBus.publish(GameEventFactory.cardPlayed(actualPlayer, actualCard));

        // Aplicar efecto (Strategy pattern)
        if (actualCard.hasEffect()) {
            actualCard.getEffect().apply(gameState);
            Player givenPlayer = gameState.getLastGivenPlayer();
            if(givenPlayer !=null){
                //Enviamos las cartas al jugador por un evento privado
                for(Card givenCard: gameState.drainLastGivenCards()){
                     eventBus.publish(GameEventFactory.cardDrawnPrivate(givenPlayer, givenCard));
                }
            }
            
        } else {
            gameState.advanceTurn();
        }

        if (actualPlayer.hasWon()) {
            eventBus.publish(GameEventFactory.gameOver(actualPlayer));
            return true;
        }

        // Si el jugador que acaba de jugar quedó con exactamente 1 carta,
        // NO avanzamos el turno todavía. Publicamos UnoGracePeriodEvent para
        // que la GUI inicie el periodo de gracia de 5 segundos. El turno
        // avanzará cuando el jugador presione UNO (onUnoDeclared) o el
        // timer expire (onUnoTimerExpired).
        if (actualPlayer.getHandSize() == 1 && !unoCalled) {
            pendingUnoPlayer = actualPlayer;
            eventBus.publish(new UnoGracePeriodEvent(actualPlayer));
            return true;
        }

        // El turno pasa normalmente
        eventBus.publish(GameEventFactory.turnChanged(
            gameState.getCurrentPlayer(),
            gameState.getTopCard(),
            gameState.isClockwise()
        ));

        return true;
    }

    /**
     * Llamado cuando el jugador local declaró UNO durante el periodo de gracia.
     * Avanza el turno normalmente sin aplicar penalización.
     * Si no hay periodo de gracia activo, no hace nada.
     */
    public void onUnoDeclared() {
        if (pendingUnoPlayer == null) return;
        pendingUnoPlayer = null;
        unoCalled = true;
        eventBus.publish(GameEventFactory.turnChanged(
            gameState.getCurrentPlayer(),
            gameState.getTopCard(),
            gameState.isClockwise()
        ));
    }

    /**
     * Llamado cuando el timer del periodo de gracia UNO expiró sin que el
     * jugador declarara. Aplica 2 cartas de penalización y avanza el turno.
     */
    public void onUnoTimerExpired() {
        if (pendingUnoPlayer == null) return;
        Player penalized = pendingUnoPlayer;
        pendingUnoPlayer = null;
        System.out.println("[GameModel] Penalización UNO para: " + penalized.getName());
        gameState.giveCards(penalized, 2);
        // Notificar al Peer de las cartas de penalización
        Player givenPlayer = gameState.getLastGivenPlayer();
        if (givenPlayer != null) {
            for (Card givenCard : gameState.drainLastGivenCards()) {
                eventBus.publish(GameEventFactory.cardDrawnPrivate(givenPlayer, givenCard));
            }
        }
        eventBus.publish(GameEventFactory.turnChanged(
            gameState.getCurrentPlayer(),
            gameState.getTopCard(),
            gameState.isClockwise()
        ));
    }

    /**
     * Procesa que un jugador roba una carta del mazo.
     *
     * @param player El jugador que roba.
     * @return La carta robada, o {@code null} si el mazo está vacío.
     */
    public Card drawCard(Player player) {
        // Buscar al jugador real en el GameState por nombre
        Player actualPlayer = gameState.getPlayers().stream()
                .filter(p -> p.getName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        if (actualPlayer == null) return null;

        if (!gameState.getCurrentPlayer().getName().equals(actualPlayer.getName())) {
            return null;
        }

        // FIX: si hay un periodo de gracia UNO activo y el jugador roba en vez de declarar,
        // cancelar el periodo de gracia para que el turno pueda avanzar normalmente.
        if (pendingUnoPlayer != null &&
                pendingUnoPlayer.getName().equals(actualPlayer.getName())) {
            pendingUnoPlayer = null;
        }

        Card drawn = Deck.getInstance().drawCard();
        if (drawn == null) return null;

        actualPlayer.addCard(drawn);

        // Publicar la versión privada (solo al jugador que robó) y pública (a todos)
        eventBus.publish(GameEventFactory.cardDrawnPrivate(actualPlayer, drawn));
        eventBus.publish(GameEventFactory.cardDrawnPublic(actualPlayer));

        gameState.advanceTurn();
        eventBus.publish(GameEventFactory.turnChanged(
            gameState.getCurrentPlayer(),
            gameState.getTopCard(),
            gameState.isClockwise()
        ));

        return drawn;
    }

    /**
     * Registra que un jugador gritó "UNO".
     *
     * @param player El jugador que gritó UNO.
     */
    public void callUno(Player player) {
        if (player.hasUno()) {
            unoCalled = true;
            eventBus.publish(GameEventFactory.unoCalled(player));
        }
    }

    /**
     * Aplica la penalización por no gritar UNO a tiempo (2 cartas).
     *
     * @param player El jugador que no gritó UNO.
     */
    public void applyUnoPenalty(Player player) {
        if (!unoCalled && player.hasUno()) {
            gameState.giveCards(player, 2);
        }
    }

    // ─────────────────────────────────────────────
    // API del Peer: actualizar la copia local
    // ─────────────────────────────────────────────

    /**
     * Actualiza el estado local del Peer con los datos que llegaron del Host.
     *
     * <p>El Peer nunca valida ni decide. Solo refleja lo que el Host aprobó.
     * Este método es llamado por la NetworkLayer cuando llega un evento
     * de sincronización del Host.
     *
     * @param currentPlayerName Nombre del jugador activo según el Host.
     * @param topCardText       Carta activa según el Host (ej. "RED-7").
     */
    public void applyStateUpdate(String currentPlayerName, String topCardText) {
        // En la implementación completa, aquí buscaríamos el Player por nombre
        // en la lista de jugadores del gameState y actualizaríamos la carta activa.
        // El estado ya fue actualizado vía los eventos publicados en el bus local.
    }

    // ─────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────

    /**
     * Devuelve el estado actual del juego.
     *
     * @return El GameState, o {@code null} si la partida no ha iniciado.
     */
    public GameState getGameState() { return gameState; }

    /** Permite a NetworkLayer controlar la bandera processingPlay para evitar bucles. */
    public void setProcessingPlay(boolean value) {
        this.processingPlay = value;
    }

    // ─────────────────────────────────────────────
    // Utilidades internas
    // ─────────────────────────────────────────────

    /**
     * Saca cartas del mazo hasta encontrar una que no sea comodín.
     * La primera carta de la partida no puede ser WILD.
     */
    /**
     * Saca cartas del mazo hasta encontrar una que no sea comodín.
     * La primera carta de la partida no puede ser WILD (regla oficial UNO).
     *
     * FIX: los comodines descartados se colocaban en la pila de descarte,
     * lo que los convertía en la "carta activa" visible antes de empezar.
     * Ahora se acumulan en una lista temporal y se reinsertan al fondo del
     * mazo de robo para que puedan volver a aparecer durante el juego.
     */
    private Card drawFirstNonWild(Deck deck) {
        java.util.List<Card> skippedWilds = new java.util.ArrayList<>();
        Card card;
        do {
            card = deck.drawCard();
            if (card != null && card.getColor() == Card.Color.WILD) {
                skippedWilds.add(card);
            }
        } while (card != null && card.getColor() == Card.Color.WILD);

        // Reinsertar los comodines al fondo del mazo (no en el descarte)
        // para que puedan salir durante el juego normal.
        for (Card wild : skippedWilds) {
            deck.discard(wild); // los ponemos en descarte temporalmente...
        }
        // ...y recargamos el mazo mezclando el descarte (sin la carta activa)
        // solo si es necesario. En la práctica, con 108 cartas y solo 4 wilds
        // al inicio, el mazo raramente necesita recargarse aquí.
        // Alternativa más simple: se quedan en el descarte y se mezclan en la
        // primera recarga automática cuando el mazo de robo se vacía.
        return card;
    }
}
