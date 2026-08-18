package br.edu.uepb.map.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;

class AplicacaoBlackjackTest {

    @Test
    void deveCriarUmaPessoaEACasaComStrategiesDistintas() {
        List<Jogador> participantes =
                AplicacaoBlackjack.criarParticipantes(new EntradaSaidaFalsa());

        assertEquals(List.of("Você", "Casa"),
                participantes.stream().map(Jogador::nome).toList());
        assertInstanceOf(DecisaoHumanaBlackjackConsole.class,
                participantes.getFirst().estrategiaDeDecisao());
        assertInstanceOf(EstrategiaCasaBlackjack.class,
                participantes.getLast().estrategiaDeDecisao());
    }

    @Test
    void deveAcumularVitoriasEntreRodadasIndependentes() {
        List<Jogador> participantes =
                AplicacaoBlackjack.criarParticipantes(new EntradaSaidaFalsa());
        Jogador jogador = participantes.getFirst();
        Jogador casa = participantes.getLast();
        Map<UUID, Integer> acumulado = new LinkedHashMap<>();
        acumulado.put(jogador.id(), 0);
        acumulado.put(casa.id(), 0);
        ResultadoDePartida vitoriaDoJogador = new ResultadoDePartida(
                List.of(jogador), Map.of(jogador, 1, casa, 0), MotivoPadrao.VITORIA);
        ResultadoDePartida vitoriaDaCasa = new ResultadoDePartida(
                List.of(casa), Map.of(jogador, 0, casa, 1), MotivoPadrao.VITORIA);

        AplicacaoBlackjack.acumularPontuacao(
                acumulado, vitoriaDoJogador, participantes);
        AplicacaoBlackjack.acumularPontuacao(
                acumulado, vitoriaDoJogador, participantes);
        AplicacaoBlackjack.acumularPontuacao(
                acumulado, vitoriaDaCasa, participantes);

        assertEquals(2, acumulado.get(jogador.id()));
        assertEquals(1, acumulado.get(casa.id()));
    }

    @Test
    void deveOferecerNovaRodadaSemTornarPlacarInutil() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0, 1);

        assertTrue(AplicacaoBlackjack.desejaJogarNovamente(io));
        assertFalse(AplicacaoBlackjack.desejaJogarNovamente(io));
        assertEquals(List.of(
                "Sim, embaralhar novamente", "Não, encerrar"), io.ultimasOpcoes);
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final ArrayDeque<Integer> respostas;
        private List<String> ultimasOpcoes = List.of();

        private EntradaSaidaFalsa(Integer... respostas) {
            this.respostas = new ArrayDeque<>(List.of(respostas));
        }

        @Override
        public void exibir(String mensagem) {
            // Não é necessário registrar mensagens nestes testes.
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            ultimasOpcoes = List.copyOf(opcoes);
            return respostas.removeFirst();
        }
    }
}
