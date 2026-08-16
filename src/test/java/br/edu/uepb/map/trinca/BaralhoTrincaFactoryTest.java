package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BaralhoTrincaFactoryTest {

    @Test
    void deveCriarDoisBaralhosComIdsUnicos() {
        var baralho = new BaralhoTrincaFactory().criar();

        assertEquals(104, baralho.quantidade());
        assertEquals(104, baralho.cartas().stream().map(CartaTrinca::id).distinct().count());
    }

    @Test
    void deveCriarDuasCopiasDeCadaValorENaipe() {
        var cartas = new BaralhoTrincaFactory().criar().cartas();

        for (Valor valor : Valor.values()) {
            for (Naipe naipe : Naipe.values()) {
                long quantidade = cartas.stream()
                        .filter(carta -> carta.valor() == valor && carta.naipe() == naipe)
                        .count();
                assertEquals(2, quantidade);
            }
        }
    }
}
