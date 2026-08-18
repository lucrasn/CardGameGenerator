package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

class AplicacaoTrincaTest {

    @Test
    void deveConfigurarQuantidadeCoresFixasEOrdenacaoPadrao() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa();

        List<Jogador> jogadores = AplicacaoTrinca.configurarJogadores(io, 5);

        assertEquals(5, jogadores.size());
        assertEquals(List.of(
                "Jogador 1", "Jogador 2", "Jogador 3", "Jogador 4", "Jogador 5"),
                jogadores.stream().map(Jogador::nome).toList());
        List<DecisaoHumanaTrincaConsole> decisoes = jogadores.stream()
                .map(Jogador::estrategiaDeDecisao)
                .map(DecisaoHumanaTrincaConsole.class::cast)
                .toList();
        assertEquals(List.of(
                OrdenacaoDaMao.POR_VALOR,
                OrdenacaoDaMao.POR_VALOR,
                OrdenacaoDaMao.POR_VALOR,
                OrdenacaoDaMao.POR_VALOR,
                OrdenacaoDaMao.POR_VALOR),
                decisoes.stream().map(DecisaoHumanaTrincaConsole::ordenacao).toList());
        assertEquals(CorTerminal.coresDeJogador(),
                decisoes.stream().map(DecisaoHumanaTrincaConsole::cor).toList());
        assertEquals(5, CorTerminal.coresDeJogador().size());
        assertTrue(CorTerminal.coresDeJogador().stream()
                .noneMatch(cor -> cor == CorTerminal.VERMELHO));
    }

    @Test
    void deveInterpretarNumeroDigitadoComoQuantidadeRealDeJogadores() {
        StringWriter saida = new StringWriter();
        ControleEntradaSaida io = new ControleEntradaSaida(
                new StringReader("2\n"), saida);

        List<Jogador> jogadores = AplicacaoTrinca.configurarJogadores(io);

        assertEquals(2, jogadores.size());
        assertTrue(saida.toString().contains(
                "Digite a quantidade de jogadores (mínimo 2 e máximo 5):"));
        assertFalse(saida.toString().contains("1 -"));
        assertFalse(saida.toString().contains("2 -"));
    }

    @Test
    void deveAcumularPlacarEOferecerUmaNovaRodada() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(0, 1);
        List<Jogador> jogadores = AplicacaoTrinca.configurarJogadores(io, 2);
        Jogador primeiro = jogadores.get(0);
        Jogador segundo = jogadores.get(1);
        Map<UUID, Integer> placarAcumulado = new LinkedHashMap<>();
        placarAcumulado.put(primeiro.id(), 0);
        placarAcumulado.put(segundo.id(), 0);
        ResultadoDePartida primeiraVitoria = new ResultadoDePartida(
                List.of(primeiro),
                Map.of(primeiro, 1, segundo, 0),
                MotivoPadrao.VITORIA);
        ResultadoDePartida segundaVitoria = new ResultadoDePartida(
                List.of(segundo),
                Map.of(primeiro, 0, segundo, 1),
                MotivoPadrao.VITORIA);

        AplicacaoTrinca.acumularPontuacao(
                placarAcumulado, primeiraVitoria, jogadores);
        AplicacaoTrinca.acumularPontuacao(
                placarAcumulado, primeiraVitoria, jogadores);
        AplicacaoTrinca.acumularPontuacao(
                placarAcumulado, segundaVitoria, jogadores);

        assertEquals(2, placarAcumulado.get(primeiro.id()));
        assertEquals(1, placarAcumulado.get(segundo.id()));
        assertTrue(AplicacaoTrinca.desejaJogarNovamente(io));
        assertFalse(AplicacaoTrinca.desejaJogarNovamente(io));
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final Deque<Integer> respostas;

        private EntradaSaidaFalsa(Integer... respostas) {
            this.respostas = new ArrayDeque<>(Arrays.asList(respostas));
        }

        @Override
        public void exibir(String mensagem) {
            // A configuração usa apenas perguntas.
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            return respostas.removeFirst();
        }
    }
}
