package Red;

import Eventos.TurnChangedEvent;
import Eventos.UnoCalledEvent;
import Eventos.PlayerReadyEvent;
import Eventos.PlayerDisconnectedEvent;
import Eventos.PlayerJoinedEvent;
import Eventos.DrawCardRequestEvent;
import Eventos.GameOverEvent;
import Eventos.ColorChosenEvent;
import Eventos.CardPlayedEvent;
import Eventos.CardDrawnPublicEvent;
import Eventos.CardDrawnPrivateEvent;
import Eventos.GameStartedEvent;
import Eventos.GameEvent;
import Dominio.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte eventos del juego a texto (JSON simple) y viceversa.
 *
 * <p>
 * Cuando dos jugadores se comunican por socket, no pueden enviarse objetos Java
 * directamente. Lo que viaja por la red es texto plano. Esta clase hace la
 * traducción en ambas direcciones:
 * <ul>
 * <li><b>Serializar:</b> evento Java → texto JSON para enviar.</li>
 * <li><b>Deserializar:</b> texto JSON recibido → evento Java.</li>
 * </ul>
 *
 * <p>
 * <b>¿Por qué JSON construido a mano y no una librería?</b><br>
 * El proyecto no admite frameworks ni librerías externas. Construimos el JSON
 * manualmente con {@link StringBuilder}. Para un protocolo acotado como este
 * (pocos tipos de mensajes con campos fijos), esta solución es más que
 * suficiente y evita dependencias innecesarias.
 *
 * <p>
 * <b>Formato del protocolo:</b> Cada mensaje es una línea de texto que termina
 * en {@code \n}. Esto permite al receptor leer línea por línea y saber cuándo
 * termina un mensaje completo.
 *
 * <p>
 * Ejemplo de mensaje serializado:
 * <pre>
 *   {"type":"CARD_PLAYED","player":"Kai","card":"RED-7"}
 * </pre>
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class MessageSerializer {

    // Tipos de mensaje del protocolo
    public static final String TYPE_CARD_PLAYED = "CARD_PLAYED";
    public static final String TYPE_DRAW_REQUEST = "DRAW_REQUEST";
    public static final String TYPE_CARD_DRAWN_PUBLIC = "CARD_DRAWN_PUBLIC";
    public static final String TYPE_CARD_DRAWN_PRIVATE = "CARD_DRAWN_PRIVATE";
    public static final String TYPE_TURN_CHANGED = "TURN_CHANGED";
    public static final String TYPE_PLAYER_JOINED = "PLAYER_JOINED";
    public static final String TYPE_PLAYER_LEFT = "PLAYER_LEFT";
    public static final String TYPE_PLAYER_READY = "PLAYER_READY";
    public static final String TYPE_GAME_STARTED = "GAME_STARTED";
    public static final String TYPE_GAME_OVER = "GAME_OVER";
    public static final String TYPE_UNO_CALLED = "UNO_CALLED";
    public static final String TYPE_UNO_PENALTY = "UNO_PENALTY";
    public static final String TYPE_UNO_GRACE = "UNO_GRACE";
    public static final String TYPE_COLOR_CHOSEN = "COLOR_CHOSEN";
    public static final String TYPE_LOBBY_STATE = "LOBBY_STATE";
    public static final String TYPE_START_REQUESTED = "START_REQUESTED";
    public static final String TYPE_CARD_REJECTED = "CARD_REJECTED";

    /**
     * Constructor privado: clase de utilidad, no se instancia.
     */
    private MessageSerializer() {
    }

    // ─────────────────────────────────────────────
    // Serialización (evento → texto)
    // ─────────────────────────────────────────────
    /**
     * Convierte un evento Java en una cadena de texto JSON lista para enviar.
     *
     * @param event El evento a serializar.
     * @return El texto JSON que representa el evento, terminado en nueva línea.
     */
    public static String serialize(GameEvent event) {
        // Usamos el tipo del evento para saber cómo serializarlo
        if (event instanceof DrawCardRequestEvent) {
            return serializeDrawRequest((DrawCardRequestEvent) event);
        }
        if (event instanceof CardPlayedEvent) {
            return serializeCardPlayed((CardPlayedEvent) event);
        }
        if (event instanceof CardDrawnPublicEvent) {
            return serializeCardDrawnPublic((CardDrawnPublicEvent) event);
        }
        if (event instanceof CardDrawnPrivateEvent) {
            return serializeCardDrawnPrivate((CardDrawnPrivateEvent) event);
        }
        if (event instanceof TurnChangedEvent) {
            return serializeTurnChanged((TurnChangedEvent) event);
        }
        if (event instanceof PlayerJoinedEvent) {
            return serializePlayerJoined((PlayerJoinedEvent) event);
        }
        if (event instanceof PlayerReadyEvent) {
            return serializePlayerReady((PlayerReadyEvent) event);
        }
        if (event instanceof PlayerDisconnectedEvent) {
            return serializePlayerLeft((PlayerDisconnectedEvent) event);
        }
        if (event instanceof GameStartedEvent) {
            return json(TYPE_GAME_STARTED, new HashMap<>()) + "\n";
        }
        if (event instanceof GameOverEvent) {
            return serializeGameOver((GameOverEvent) event);
        }
        if (event instanceof UnoCalledEvent) {
            return serializeUnoCalled((UnoCalledEvent) event);
        }
        if (event instanceof ColorChosenEvent) {
            return serializeColorChosen((ColorChosenEvent) event);
        }
        if (event instanceof Eventos.StartRequestedEvent) {
            Eventos.StartRequestedEvent e = (Eventos.StartRequestedEvent) event;
            Map<String, String> fields = new HashMap<>();
            fields.put("player", e.getRequesterName());
            return json(TYPE_START_REQUESTED, fields) + "\n";
        }
        return "";
    }

    private static String serializeDrawRequest(DrawCardRequestEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        return json(TYPE_DRAW_REQUEST, fields) + "\n";
    }

    private static String serializeCardPlayed(CardPlayedEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        fields.put("card", e.getCard().toString());
        return json(TYPE_CARD_PLAYED, fields) + "\n";
    }

    private static String serializeCardDrawnPublic(CardDrawnPublicEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        return json(TYPE_CARD_DRAWN_PUBLIC, fields) + "\n";
    }

    private static String serializeCardDrawnPrivate(CardDrawnPrivateEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        fields.put("card", e.getCard().toString());
        return json(TYPE_CARD_DRAWN_PRIVATE, fields) + "\n";
    }

    private static String serializeTurnChanged(TurnChangedEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("currentPlayer", e.getCurrentPlayer().getName());
        fields.put("topCard", e.getTopCard().toString());
        fields.put("clockwise", String.valueOf(e.isClockwise()));
        return json(TYPE_TURN_CHANGED, fields) + "\n";
    }

    private static String serializePlayerJoined(PlayerJoinedEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        fields.put("avatarId", e.getPlayer().getAvatarId());
        fields.put("isHost", String.valueOf(e.getPlayer().isHost()));
        // Incluir el estado "Listo" para que el Peer vea correctamente
        // si el Host ya está listo cuando llega sendCurrentLobbyState()
        fields.put("ready", String.valueOf(e.getPlayer().isReady()));
        return json(TYPE_PLAYER_JOINED, fields) + "\n";
    }

    private static String serializePlayerReady(PlayerReadyEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayerName());
        fields.put("ready", String.valueOf(e.isReady()));
        return json(TYPE_PLAYER_READY, fields) + "\n";
    }

    private static String serializePlayerLeft(PlayerDisconnectedEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayerName());
        return json(TYPE_PLAYER_LEFT, fields) + "\n";
    }

    private static String serializeGameOver(GameOverEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("winner", e.getWinner().getName());
        return json(TYPE_GAME_OVER, fields) + "\n";
    }

    /**
     * Serializa el evento de penalización UNO (el timer de gracia expiró).
     */
    public static String serializeUnoPenalty(Eventos.UnoPenaltyEvent e) {
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("player", e.getPlayer().getName());
        return json(TYPE_UNO_PENALTY, fields) + "\n";
    }

    /**
     * Serializa el evento de inicio del periodo de gracia UNO para broadcast.
     * Así los demás jugadores pueden ver visualmente que alguien está en
     * gracia.
     */
    public static String serializeUnoGrace(Eventos.UnoGracePeriodEvent e) {
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("player", e.getPlayer().getName());
        return json(TYPE_UNO_GRACE, fields) + "\n";
    }

    private static String serializeUnoCalled(UnoCalledEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        return json(TYPE_UNO_CALLED, fields) + "\n";
    }

    private static String serializeColorChosen(ColorChosenEvent e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("player", e.getPlayer().getName());
        fields.put("color", e.getChosenColor().name());
        return json(TYPE_COLOR_CHOSEN, fields) + "\n";
    }

    // ─────────────────────────────────────────────
    // Deserialización (texto → mapa de campos)
    // ─────────────────────────────────────────────
    /**
     * Convierte una línea de texto JSON en un mapa de campos clave-valor.
     *
     * <p>
     * El resultado es un mapa simple donde cada clave es el nombre del campo y
     * el valor es su contenido como texto. El componente que recibe el mapa
     * decide cómo interpretarlo según el campo "type".
     *
     * <p>
     * Ejemplo: {@code {"type":"TURN_CHANGED","currentPlayer":"Kai"}} produce:
     * {@code {type=TURN_CHANGED, currentPlayer=Kai}}
     *
     * @param message La línea de texto recibida por el socket.
     * @return Mapa con los campos del mensaje, o mapa vacío si el formato es
     * inválido.
     */
    public static Map<String, String> deserialize(String message) {
        Map<String, String> fields = new HashMap<>();
        if (message == null || message.isBlank()) {
            return fields;
        }

        // Eliminar llaves, saltos de linea y espacios extremos
        String content = message.trim();
        if (content.startsWith("{")) {
            content = content.substring(1);
        }
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }

        // Parseamos el JSON manualmente caracter a caracter.
        // Evitamos regex de lookahead que causan StackOverflowError con cadenas largas.
        boolean inQuotes = false;
        int tokenStart = 0;
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(content.substring(tokenStart, i).trim());
                tokenStart = i + 1;
            }
        }
        if (tokenStart < content.length()) {
            tokens.add(content.substring(tokenStart).trim());
        }

        for (String token : tokens) {
            int colonIdx = token.indexOf(':');
            if (colonIdx < 0) {
                continue;
            }
            String key = stripQuotes(token.substring(0, colonIdx).trim());
            String value = stripQuotes(token.substring(colonIdx + 1).trim());
            fields.put(key, value);
        }

        return fields;
    }

    /**
     * Elimina las comillas que envuelven un valor JSON. Por ejemplo: '"hello"'
     * se convierte en 'hello'.
     *
     * @param s El texto posiblemente envuelto en comillas.
     * @return El texto sin comillas externas.
     */
    private static String stripQuotes(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Extrae el tipo de mensaje de un mapa deserializado.
     *
     * @param fields El mapa producido por {@link #deserialize(String)}.
     * @return El tipo de mensaje (ej. "CARD_PLAYED"), o cadena vacía si no
     * existe.
     */
    public static String getType(Map<String, String> fields) {
        return fields.getOrDefault("type", "");
    }

    // ─────────────────────────────────────────────
    // Utilidades para construir JSON
    // ─────────────────────────────────────────────
    /**
     * Re-serializa un mensaje a partir de su tipo y mapa de campos ya
     * deserializado. Útil cuando el Host necesita reenviar un mensaje recibido
     * sin modificarlo.
     *
     * @param type El tipo de mensaje (ej. "PLAYER_READY").
     * @param fields Los campos ya deserializados del mensaje original.
     * @return El texto JSON reconstruido, terminado en nueva línea.
     */
    public static String serializeFields(String type, Map<String, String> fields) {
        Map<String, String> copy = new HashMap<>(fields);
        copy.remove("type"); // el método json() ya agrega el type
        return json(type, copy) + "\n";
    }

    /**
     * Construye una cadena JSON simple con un tipo y campos adicionales.
     *
     * <p>
     * Ejemplo: json("CARD_PLAYED", {player: "Kai", card: "RED-7"}) produce:
     * {"type":"CARD_PLAYED","player":"Kai","card":"RED-7"}
     *
     * @param type El valor del campo "type".
     * @param fields Campos adicionales clave-valor.
     * @return La cadena JSON resultante.
     */
    private static String json(String type, Map<String, String> fields) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":\"").append(type).append("\"");
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append(",\"").append(entry.getKey())
                    .append("\":\"").append(entry.getValue()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
