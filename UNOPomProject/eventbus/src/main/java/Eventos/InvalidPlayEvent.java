/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Eventos;

import Eventos.GameEvent;
import Dominio.Card;
import Dominio.Player;

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
    
    
    

