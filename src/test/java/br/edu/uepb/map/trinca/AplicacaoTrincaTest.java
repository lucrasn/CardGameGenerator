package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

class AplicacaoTrincaTest {

    @Test
    void deveConfigurarQuantidadeCoresFixasEOrdenacaoPadrao() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(4);

        List<Jogador> jogadores = AplicacaoTrinca.configurarJogadores(io);

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
        assertEquals("1 jogador — indisponível (mínimo: 2)",
                io.primeirasOpcoes().getFirst());
        assertEquals("2 jogadores", io.primeirasOpcoes().get(1));
        assertEquals("5 jogadores", io.primeirasOpcoes().getLast());
    }

    @Test
    void deveInterpretarNumeroDigitadoComoQuantidadeRealDeJogadores() {
        StringWriter saida = new StringWriter();
        ControleEntradaSaida io = new ControleEntradaSaida(
                new StringReader("2\n"), saida);

        List<Jogador> jogadores = AplicacaoTrinca.configurarJogadores(io);

        assertEquals(2, jogadores.size());
        assertTrue(saida.toString().contains("2 - 2 jogadores"));
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final Deque<Integer> respostas;
        private List<String> primeirasOpcoes;

        private EntradaSaidaFalsa(Integer... respostas) {
            this.respostas = new ArrayDeque<>(Arrays.asList(respostas));
        }

        @Override
        public void exibir(String mensagem) {
            // A configuração usa apenas perguntas.
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            if (primeirasOpcoes == null) {
                primeirasOpcoes = List.copyOf(opcoes);
            }
            return respostas.removeFirst();
        }

        private List<String> primeirasOpcoes() {
            return primeirasOpcoes;
        }
    }
}
