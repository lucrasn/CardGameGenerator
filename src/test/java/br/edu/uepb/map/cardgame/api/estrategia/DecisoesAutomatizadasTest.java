package br.edu.uepb.map.cardgame.api.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EtapaDeTurno;
import br.edu.uepb.map.cardgame.api.Jogada;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisoesAutomatizadasTest {

    @Test
    void aleatoriaUsaAListaDeAcoesPermitidas() {
        Jogada primeira = new Acao("primeira", 1);
        Jogada segunda = new Acao("segunda", 2);
        ContextoDeDecisao contexto = contextoCom(primeira, segunda);
        Random segundaPosicao = new Random() {
            @Override
            public int nextInt(int limite) {
                return 1;
            }
        };

        Jogada escolhida = new DecisaoAleatoria(segundaPosicao).decidir(contexto);

        assertSame(segunda, escolhida);
    }

    @Test
    void gulosaEscolheMaiorValorImediato() {
        Acao primeira = new Acao("primeira", 3);
        Acao melhor = new Acao("melhor", 10);
        Acao ultima = new Acao("ultima", 2);
        ContextoDeDecisao contexto = contextoCom(primeira, melhor, ultima);
        DecisaoGulosa estrategia = new DecisaoGulosa(
                jogada -> ((Acao) jogada).valor()
        );

        assertSame(melhor, estrategia.decidir(contexto));
    }

    @Test
    void gulosaPreservaPrimeiraAcaoEmEmpate() {
        Acao primeira = new Acao("primeira", 10);
        Acao segunda = new Acao("segunda", 10);
        ContextoDeDecisao contexto = contextoCom(primeira, segunda);
        DecisaoGulosa estrategia = new DecisaoGulosa(
                jogada -> ((Acao) jogada).valor()
        );

        assertSame(primeira, estrategia.decidir(contexto));
    }

    @Test
    void decisoesAutomatizadasRejeitamContextoSemAcoes() {
        ContextoDeDecisao vazio = contextoCom();

        assertThrows(
                IllegalStateException.class,
                () -> new DecisaoAleatoria(new Random(1)).decidir(vazio)
        );
        assertThrows(
                IllegalStateException.class,
                () -> new DecisaoGulosa(jogada -> 0).decidir(vazio)
        );
    }

    private record Acao(String descricao, int valor) implements Jogada {
    }

    private static ContextoDeDecisao contextoCom(Jogada... jogadas) {
        return new ContextoFalso(List.of(jogadas));
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
}
