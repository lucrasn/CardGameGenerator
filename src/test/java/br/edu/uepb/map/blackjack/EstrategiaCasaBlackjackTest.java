package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;

class EstrategiaCasaBlackjackTest {

    private final EstrategiaCasaBlackjack estrategia = new EstrategiaCasaBlackjack();
    private final Jogador jogador = new JogadorPadrao("Você", contexto -> AcaoBlackjack.PARAR);
    private final Jogador casa = new JogadorPadrao("Casa", estrategia);

    @Test
    void devePedirComTotalAbaixoDeDezessete() {
        ContextoDecisaoBlackjack contexto = contextoDaCasa(
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.SEIS)),
                List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR));

        assertEquals(AcaoBlackjack.PEDIR, estrategia.decidir(contexto));
    }

    @Test
    void devePararEmDezesseteInclusiveQuandoAMaoForSuave() {
        ContextoDecisaoBlackjack contexto = contextoDaCasa(
                List.of(carta(ValorBlackjack.AS), carta(ValorBlackjack.SEIS)),
                List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR));

        assertEquals(AcaoBlackjack.PARAR, estrategia.decidir(contexto));
    }

    @Test
    void devePararSeOMonteAcabarMesmoAbaixoDeDezessete() {
        ContextoDecisaoBlackjack contexto = contextoDaCasa(
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.CINCO)),
                List.of(AcaoBlackjack.PARAR));

        assertEquals(AcaoBlackjack.PARAR, estrategia.decidir(contexto));
    }

    @Test
    void deveRejeitarContextoComPapelDeJogador() {
        List<CartaBlackjack> mao = List.of(
                carta(ValorBlackjack.DEZ), carta(ValorBlackjack.CINCO));
        var contexto = new ContextoDecisaoBlackjack(
                EtapaBlackjack.DECISAO,
                List.of(AcaoBlackjack.PEDIR, AcaoBlackjack.PARAR),
                jogador,
                PapelBlackjack.JOGADOR,
                1,
                mao,
                PontuacaoDaMaoBlackjack.calcular(mao),
                casa,
                List.of(carta(ValorBlackjack.SETE)),
                1,
                Optional.empty(),
                48);

        assertThrows(IllegalArgumentException.class, () -> estrategia.decidir(contexto));
    }

    private ContextoDecisaoBlackjack contextoDaCasa(
            List<CartaBlackjack> mao, List<? extends Jogada> acoes) {
        return new ContextoDecisaoBlackjack(
                EtapaBlackjack.DECISAO,
                List.copyOf(acoes),
                casa,
                PapelBlackjack.CASA,
                2,
                mao,
                PontuacaoDaMaoBlackjack.calcular(mao),
                jogador,
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.OITO)),
                0,
                Optional.empty(),
                48);
    }

    private static CartaBlackjack carta(ValorBlackjack valor) {
        return new CartaBlackjack(valor, NaipeBlackjack.PAUS);
    }
}
