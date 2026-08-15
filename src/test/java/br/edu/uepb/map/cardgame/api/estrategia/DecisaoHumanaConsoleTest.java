package br.edu.uepb.map.cardgame.api.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EtapaDeTurno;
import br.edu.uepb.map.cardgame.api.Jogada;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisaoHumanaConsoleTest {

    @Test
    void converteOpcoesDeConsoleEmJogadaTipada() {
        Jogada comprar = new Acao("comprar");
        Jogada descartar = new Acao("descartar");
        EntradaSaidaFalsa entradaSaida = new EntradaSaidaFalsa(1);
        ContextoDeDecisao contexto = contextoCom(comprar, descartar);
        DecisaoHumanaConsole estrategia = new DecisaoHumanaConsole(
                entradaSaida,
                jogada -> ((Acao) jogada).descricao().toUpperCase()
        );

        Jogada escolhida = estrategia.decidir(contexto);

        assertSame(descartar, escolhida);
        assertEquals("Escolha uma jogada:", entradaSaida.mensagem);
        assertEquals(List.of("COMPRAR", "DESCARTAR"), entradaSaida.opcoes);
    }

    @Test
    void rejeitaAusenciaDeAcoesOuIndiceInvalidoDaPorta() {
        DecisaoHumanaConsole estrategia = new DecisaoHumanaConsole(
                new EntradaSaidaFalsa(0)
        );
        assertThrows(
                IllegalStateException.class,
                () -> estrategia.decidir(contextoCom())
        );

        DecisaoHumanaConsole portaIncorreta = new DecisaoHumanaConsole(
                new EntradaSaidaFalsa(2)
        );
        assertThrows(
                IllegalStateException.class,
                () -> portaIncorreta.decidir(contextoCom(new Acao("comprar")))
        );
    }

    private static ContextoDeDecisao contextoCom(Jogada... jogadas) {
        return new ContextoFalso(List.of(jogadas));
    }

    private record Acao(String descricao) implements Jogada {
    }

    private record ContextoFalso(List<Jogada> jogadasPermitidas)
            implements ContextoDeDecisao {

        @Override
        public EtapaDeTurno etapa() {
            return EtapaTeste.DECISAO;
        }
    }

    private enum EtapaTeste implements EtapaDeTurno {
        DECISAO
    }

    private static final class EntradaSaidaFalsa implements EntradaSaida {

        private final int resposta;
        private String mensagem;
        private List<String> opcoes;

        private EntradaSaidaFalsa(int resposta) {
            this.resposta = resposta;
        }

        @Override
        public void exibir(String mensagem) {
            this.mensagem = mensagem;
        }

        @Override
        public int solicitarOpcao(String mensagem, List<String> opcoes) {
            this.mensagem = mensagem;
            this.opcoes = List.copyOf(opcoes);
            return resposta;
        }
    }
}
