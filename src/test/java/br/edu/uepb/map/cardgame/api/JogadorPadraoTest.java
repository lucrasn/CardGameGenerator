package br.edu.uepb.map.cardgame.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JogadorPadraoTest {

    private static final EstrategiaDeDecisao PRIMEIRA_JOGADA =
            contexto -> contexto.jogadasPermitidas().get(0);

    @Test
    void guardaIdentidadeNomeEstrategia() {
        JogadorPadrao jogador = new JogadorPadrao("  Ana  ", PRIMEIRA_JOGADA);

        assertEquals("Ana", jogador.nome());
        assertSame(PRIMEIRA_JOGADA, jogador.estrategiaDeDecisao());
        assertEquals("Ana", jogador.toString());
    }

    @Test
    void geraIdentificadoresDistintosParaJogadoresComMesmoNome() {
        JogadorPadrao primeiro = new JogadorPadrao("Ana", PRIMEIRA_JOGADA);
        JogadorPadrao segundo = new JogadorPadrao("Ana", PRIMEIRA_JOGADA);

        assertNotEquals(primeiro.id(), segundo.id());
    }

    @Test
    void permiteTrocarEstrategiaSemTrocarIdentidade() {
        Jogada primeira = new Acao("primeira");
        Jogada ultima = new Acao("ultima");
        ContextoDeDecisao contexto = new ContextoDeDecisaoPadrao(
                EtapaTeste.DECISAO,
                List.of(primeira, ultima)
        );
        EstrategiaDeDecisao ultimaJogada =
                visao -> visao.jogadasPermitidas().get(
                        visao.jogadasPermitidas().size() - 1
                );
        JogadorPadrao jogador = new JogadorPadrao("Ana", PRIMEIRA_JOGADA);
        var idOriginal = jogador.id();

        assertSame(primeira, jogador.estrategiaDeDecisao().decidir(contexto));

        jogador.alterarEstrategiaDeDecisao(ultimaJogada);

        assertEquals(idOriginal, jogador.id());
        assertSame(ultimaJogada, jogador.estrategiaDeDecisao());
        assertSame(ultima, jogador.estrategiaDeDecisao().decidir(contexto));
    }

    @Test
    void rejeitaNomeNuloVazioOuEmBranco() {
        assertThrows(
                NullPointerException.class,
                () -> new JogadorPadrao(null, PRIMEIRA_JOGADA)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new JogadorPadrao("", PRIMEIRA_JOGADA)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new JogadorPadrao("   ", PRIMEIRA_JOGADA)
        );
    }

    @Test
    void rejeitaEstrategiasNulas() {
        assertThrows(NullPointerException.class, () -> new JogadorPadrao("Ana", null));

        JogadorPadrao jogador = new JogadorPadrao("Ana", PRIMEIRA_JOGADA);
        assertThrows(
                NullPointerException.class,
                () -> jogador.alterarEstrategiaDeDecisao(null)
        );
    }

    private record Acao(String descricao) implements Jogada {
    }

    private enum EtapaTeste implements EtapaDeTurno {
        DECISAO
    }
}
