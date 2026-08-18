package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class BaralhoBlackjackFactoryTest {

    @Test
    void deveCriarAsCinquentaEDuasCombinacoesSemRepeticao() {
        var baralho = new BaralhoBlackjackFactory().criar();

        assertEquals(52, baralho.quantidade());
        assertEquals(52, new HashSet<>(baralho.cartas().stream()
                .map(carta -> carta.valor().name() + "-" + carta.naipe().name())
                .toList()).size());
        assertEquals(52, new HashSet<>(baralho.cartas().stream()
                .map(CartaBlackjack::id)
                .toList()).size());
    }

    @Test
    void deveCriarBaralhosECartasIndependentes() {
        var fabrica = new BaralhoBlackjackFactory();
        var primeiro = fabrica.criar();
        var segundo = fabrica.criar();

        assertNotSame(primeiro, segundo);
        assertEquals(0, primeiro.cartas().stream()
                .map(CartaBlackjack::id)
                .filter(id -> segundo.cartas().stream()
                        .anyMatch(carta -> carta.id().equals(id)))
                .count());
    }
}
