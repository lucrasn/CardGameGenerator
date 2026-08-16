package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;

class DecisaoHumanaTrincaConsoleTest {

    @Test
    void deveMostrarMaoComNaipesEDevolverEscolha() {
        EntradaSaidaFalsa io = new EntradaSaidaFalsa(1);
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(io);
        JogadorPadrao jogador = new JogadorPadrao("Ana", decisao);
        CartaTrinca sete = new CartaTrinca(Valor.SETE, Naipe.COPAS);
        CartaTrinca rei = new CartaTrinca(Valor.REI, Naipe.ESPADAS);
        Jogada monte = ComprarDoMonte.INSTANCIA;
        Jogada descarte = new ComprarDoDescarte(rei);
        var contexto = new ContextoDecisaoTrinca(
                EtapaTrinca.COMPRA,
                List.of(monte, descarte),
                jogador,
                List.of(sete),
                Optional.of(rei));

        Jogada escolhida = decisao.decidir(contexto);

        assertSame(descarte, escolhida);
        assertEquals(List.of(
                "\033[2J\033[H",
                "\033[2J\033[H",
                "Sua mão: 7 de Copas",
                "Topo do descarte: K de Espadas"), io.mensagens);
        assertEquals("Escolha de onde comprar:", io.pergunta);
        assertEquals(List.of(
                "Comprar do monte",
                "Comprar do descarte (K de Espadas)"), io.opcoes);
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {
        private final int resposta;
        private final List<String> mensagens = new ArrayList<>();
        private String pergunta;
        private List<String> opcoes;

        private EntradaSaidaFalsa(int resposta) {
            this.resposta = resposta;
        }

        @Override
        public void exibir(String mensagem) {
            mensagens.add(mensagem);
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            this.pergunta = mensagem;
            this.opcoes = List.copyOf(opcoes);
            return resposta;
        }
    }
}
