/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.events;

import uno.model.Card;
import uno.model.Player;

/**
 * Evento publicado cuando un jugador intenta una jugada inválida.
     * El GameController lo usa para rehabilitar la mano sin necesitar
     * saber el motivo específico del rechazo.
     
 * @author Elite
 */
public class InvalidPlayEvent extends GameEvent {
    

    public InvalidPlayEvent() {
        super();
    }
}
    
    
    

