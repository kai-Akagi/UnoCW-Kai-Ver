/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

/**
 * Evento publicado en el bus local cuando el Host rechaza la conexión del Peer.
 *
 * <p>
 * Esto ocurre cuando el nombre o avatar enviado ya está en uso en la sala. El
 * {@code RegisterController} se suscribe temporalmente a este evento para
 * mostrar el error en la pantalla de registro, sin navegar a la sala de espera.
 *
 * <p>
 * Extiende {@link GameEvent} (clase abstracta) para heredar el timestamp
 * automático que registra cuándo ocurrió el evento.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class NetworkPlayerRejectedEvent extends GameEvent {

    /**
     * El motivo del rechazo enviado por el Host.
     */
    private final String reason;

    /**
     * Constructor para NetworkPlayerRejectedEvent.
     *
     * @param reason Descripción del motivo del rechazo para mostrar al usuario.
     */
    public NetworkPlayerRejectedEvent(String reason) {
        super(); // registra el timestamp automáticamente
        this.reason = reason;
    }

    /**
     * Devuelve el motivo por el que el Host rechazó la conexión.
     *
     * @return Texto explicativo para mostrar al usuario en la pantalla de
     * registro.
     */
    public String getReason() {
        return reason;
    }
}
