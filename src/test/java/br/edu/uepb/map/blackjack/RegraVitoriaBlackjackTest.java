package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

class RegraVitoriaBlackjackTest {

    private Jogador jogador;
    private Jogador casa;
    private MesaBlackjack mesa;
    private RegraVitoriaBlackjack regra;

    @BeforeEach
    void preparar() {
        jogador = new JogadorPadrao("Você", contexto -> AcaoBlackjack.PARAR);
        casa = new JogadorPadrao("Casa", contexto -> AcaoBlackjack.PARAR);
        mesa = new MesaBlackjack(jogador, casa);
        regra = new RegraVitoriaBlackjack(mesa);
    }

    @Test
    void deveEncerrarImediatamenteComBlackjackNaturalDoJogador() {
        DesfechoDePartida desfecho = regra.avaliar(visao(
                0,
                List.of(carta(ValorBlackjack.AS), carta(ValorBlackjack.REI)),
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.OITO))))
                .orElseThrow();

        assertEquals(List.of(jogador), desfecho.vencedores());
        assertEquals(MotivoBlackjack.BLACKJACK_DO_JOGADOR, desfecho.motivo());
    }

    @Test
    void deveReconhecerEmpateEntreDoisBlackjacksNaturais() {
        DesfechoDePartida desfecho = regra.avaliar(visao(
                0,
                List.of(carta(ValorBlackjack.AS), carta(ValorBlackjack.REI)),
                List.of(carta(ValorBlackjack.AS), carta(ValorBlackjack.DAMA))))
                .orElseThrow();

        assertEquals(List.of(jogador, casa), desfecho.vencedores());
        assertEquals(MotivoBlackjack.BLACKJACKS_IGUAIS, desfecho.motivo());
        assertTrue(desfecho.motivo().ehEmpate());
    }

    @Test
    void deveDarVitoriaACasaQuandoJogadorEstourar() {
        DesfechoDePartida desfecho = regra.avaliar(visao(
                1,
                List.of(carta(ValorBlackjack.REI), carta(ValorBlackjack.DAMA),
                        carta(ValorBlackjack.DOIS)),
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.OITO))))
                .orElseThrow();

        assertEquals(List.of(casa), desfecho.vencedores());
        assertEquals(MotivoBlackjack.JOGADOR_ESTOUROU, desfecho.motivo());
    }

    @Test
    void deveEsperarOsDoisPararemAntesDeCompararPontuacoes() {
        mesa.registrarParada(jogador);
        var contexto = visao(
                1,
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.OITO)),
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.SETE)));

        assertTrue(regra.avaliar(contexto).isEmpty());

        mesa.registrarParada(casa);
        DesfechoDePartida desfecho = regra.avaliar(contexto).orElseThrow();
        assertEquals(List.of(jogador), desfecho.vencedores());
        assertEquals(MotivoBlackjack.MAIOR_PONTUACAO, desfecho.motivo());
    }

    @Test
    void deveReconhecerEmpatePorPontuacoesIguais() {
        mesa.registrarParada(jogador);
        mesa.registrarParada(casa);

        DesfechoDePartida desfecho = regra.avaliar(visao(
                3,
                List.of(carta(ValorBlackjack.DEZ), carta(ValorBlackjack.OITO)),
                List.of(carta(ValorBlackjack.REI), carta(ValorBlackjack.OITO))))
                .orElseThrow();

        assertEquals(List.of(jogador, casa), desfecho.vencedores());
        assertEquals(MotivoBlackjack.PONTUACOES_IGUAIS, desfecho.motivo());
    }

    private VisaoDaPartida<CartaBlackjack> visao(
            long turno,
            List<CartaBlackjack> maoDoJogador,
            List<CartaBlackjack> maoDaCasa) {
        return new VisaoFalsa(
                List.of(jogador, casa),
                jogador,
                Map.of(jogador, maoDoJogador, casa, maoDaCasa),
                turno);
    }

    private static CartaBlackjack carta(ValorBlackjack valor) {
        return new CartaBlackjack(valor, NaipeBlackjack.OUROS);
    }

    private record VisaoFalsa(
            List<Jogador> jogadores,
            Jogador jogadorAtual,
            Map<Jogador, List<CartaBlackjack>> maos,
            long numeroDoTurno) implements VisaoDaPartida<CartaBlackjack> {

        @Override
        public EstadoPartida estado() {
            return EstadoPartida.EM_ANDAMENTO;
        }

        @Override
        public List<CartaBlackjack> maoDe(Jogador participante) {
            return Optional.ofNullable(maos.get(participante)).orElseThrow();
        }

        @Override
        public int quantidadeNoBaralho() {
            return 48;
        }
    }
}
