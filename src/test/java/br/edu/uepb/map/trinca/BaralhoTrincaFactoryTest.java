package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BaralhoTrincaFactoryTest {

    @Test
    void deveCriarUmBaralhoComIdsUnicos() {
        var baralho = new BaralhoTrincaFactory().criar();

        assertEquals(52, baralho.quantidade());
        assertEquals(52, baralho.cartas().stream().map(CartaTrinca::id).distinct().count());
    }

    @Test
    void deveCriarUmaCartaDeCadaValorENaipe() {
        var cartas = new BaralhoTrincaFactory().criar().cartas();

        for (Valor valor : Valor.values()) {
            for (Naipe naipe : Naipe.values()) {
                long quantidade = cartas.stream()
                        .filter(carta -> carta.valor() == valor && carta.naipe() == naipe)
                        .count();
                assertEquals(1, quantidade);
            }
        }
    }
}
