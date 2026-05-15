/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

/**
 * Evento publicado cuando un jugador intenta una jugada inválida. El
 * GameController lo usa para rehabilitar la mano sin necesitar saber el motivo
 * específico del rechazo.
 *
 * @author Héctor Javier Alonso Zaragoza
 * @author Alejandro Rodríguez Lugo
 * @author Katia Ximena Navarez Espinoza
 * @author Luis Carlos Manjarrez Gonzalez
 */
public class InvalidPlayEvent extends GameEvent {

    /**
     * Constructor para InvalidPlayEvent.
     */
    public InvalidPlayEvent() {
        super();
    }
}
