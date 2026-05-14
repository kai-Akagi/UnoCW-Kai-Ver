/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

import Dominio.Card;
import Dominio.Card.Color;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * Fábrica de cartas del juego UNO.
 *
 * <p>Esta clase es responsable de crear el mazo completo de 108 cartas de UNO.
 * Nadie más en el sistema crea cartas directamente; toda carta nace aquí.
 *
 * <p><b>Patrón aplicado: Factory</b><br>
 * La idea del patrón Factory es tener un lugar centralizado para crear objetos
 * complejos. ¿Por qué es útil aquí?
 * <ul>
 *   <li>Las 108 cartas tienen reglas precisas de composición (cuántas de cada tipo).
 *       Si esa lógica estuviera dispersa, sería fácil cometer errores.</li>
 *   <li>Si las reglas del mazo cambian (ej. versión especial de UNO), solo
 *       modificamos esta clase, no el resto del juego.</li>
 *   <li>El código que usa las cartas no necesita saber cómo se crean.</li>
 * </ul>
 *
 * <p><b>Composición del mazo UNO (108 cartas):</b>
 * <ul>
 *   <li>4 colores × 1 carta del "0" = 4 cartas</li>
 *   <li>4 colores × 2 cartas de cada número 1–9 = 72 cartas</li>
 *   <li>4 colores × 2 SKIP = 8 cartas</li>
 *   <li>4 colores × 2 REVERSE = 8 cartas</li>
 *   <li>4 colores × 2 DRAW TWO = 8 cartas</li>
 *   <li>4 WILD = 4 cartas</li>
 *   <li>4 WILD DRAW FOUR = 4 cartas</li>
 *   <li><b>Total: 108 cartas</b></li>
 * </ul>
 */
public class CardFactory {
 
    /**
     * Constructor privado: esta clase no se puede instanciar.
     * Su único método público es estático.
     */
    private CardFactory() {}
 
    /**
     * Crea y devuelve el mazo completo de 108 cartas de UNO, ya mezclado.
     *
     * <p>Mezclar el mazo aquí y no en el {@link Dominio.Deck} es una
     * decisión consciente: la fábrica entrega el producto listo para usarse.
     *
     * @return Lista con las 108 cartas del juego en orden aleatorio.
     */
    public static List<Card> createFullDeck() {
        List<Card> deck = new ArrayList<>();
 
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
 
        for (Color color : colors) {
            // Una sola carta del "0" por color
            deck.add(new Card(color, "0", null));
 
            // Dos cartas de cada número del 1 al 9
            for (int num = 1; num <= 9; num++) {
                deck.add(new Card(color, String.valueOf(num), null));
                deck.add(new Card(color, String.valueOf(num), null));
            }
 
            // Dos SKIP por color
            deck.add(new Card(color, "SKIP", CardEffectFactory.createSkip()));
            deck.add(new Card(color, "SKIP", CardEffectFactory.createSkip()));
 
            // Dos REVERSE por color
            deck.add(new Card(color, "REVERSE", CardEffectFactory.createReverse()));
            deck.add(new Card(color, "REVERSE", CardEffectFactory.createReverse()));
 
            // Dos DRAW TWO por color
            deck.add(new Card(color, "DRAW_TWO", CardEffectFactory.createDrawTwo()));
            deck.add(new Card(color, "DRAW_TWO", CardEffectFactory.createDrawTwo()));
        }
 
        // 4 WILD (comodines simples, sin color fijo)
        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Color.WILD, "WILD", CardEffectFactory.createWild()));
        }
 
        // 4 WILD DRAW FOUR (comodines +4)
        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Color.WILD, "WILD_DRAW_FOUR", CardEffectFactory.createWildDrawFour()));
        }
 
        // Mezclamos el mazo antes de entregarlo
        Collections.shuffle(deck);
 
        return deck;
    }
}

