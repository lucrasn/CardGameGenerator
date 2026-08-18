package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class PontuacaoDaMaoBlackjackTest {

    @Test
    void deveReconhecerBlackjackNatural() {
        PontuacaoDaMaoBlackjack pontos = PontuacaoDaMaoBlackjack.calcular(List.of(
                carta(ValorBlackjack.AS), carta(ValorBlackjack.REI)));

        assertEquals(21, pontos.total());
        assertTrue(pontos.suave());
        assertTrue(pontos.blackjackNatural());
        assertFalse(pontos.estourou());
    }

    @Test
    void deveRebaixarAsDeOnzeParaUmQuandoNecessario() {
        PontuacaoDaMaoBlackjack pontos = PontuacaoDaMaoBlackjack.calcular(List.of(
                carta(ValorBlackjack.AS),
                carta(ValorBlackjack.SEIS),
                carta(ValorBlackjack.DEZ)));

        assertEquals(17, pontos.total());
        assertFalse(pontos.suave());
        assertFalse(pontos.blackjackNatural());
    }

    @Test
    void deveTratarVariosAsesSemEstourarPrematuramente() {
        PontuacaoDaMaoBlackjack pontos = PontuacaoDaMaoBlackjack.calcular(List.of(
                carta(ValorBlackjack.AS),
                carta(ValorBlackjack.AS),
                carta(ValorBlackjack.NOVE),
                carta(ValorBlackjack.REI)));

        assertEquals(21, pontos.total());
        assertFalse(pontos.suave());
        assertFalse(pontos.blackjackNatural());
        assertTrue(pontos.atingiuVinteEUm());
    }

    @Test
    void deveDistinguirVinteEUmComTresCartasDeBlackjackNatural() {
        PontuacaoDaMaoBlackjack pontos = PontuacaoDaMaoBlackjack.calcular(List.of(
                carta(ValorBlackjack.SETE),
                carta(ValorBlackjack.SETE),
                carta(ValorBlackjack.SETE)));

        assertEquals(21, pontos.total());
        assertFalse(pontos.blackjackNatural());
    }

    @Test
    void deveReconhecerEstouroSemAsDisponivelParaAjuste() {
        PontuacaoDaMaoBlackjack pontos = PontuacaoDaMaoBlackjack.calcular(List.of(
                carta(ValorBlackjack.REI),
                carta(ValorBlackjack.DAMA),
                carta(ValorBlackjack.DOIS)));

        assertEquals(22, pontos.total());
        assertTrue(pontos.estourou());
        assertFalse(pontos.suave());
    }

    private static CartaBlackjack carta(ValorBlackjack valor) {
        return new CartaBlackjack(valor, NaipeBlackjack.ESPADAS);
    }
}
