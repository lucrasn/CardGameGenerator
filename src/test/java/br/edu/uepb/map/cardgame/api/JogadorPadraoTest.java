package br.edu.uepb.map.cardgame.api;

import org.junit.jupiter.api.Test;

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
        EstrategiaDeDecisao ultimaJogada =
                contexto -> contexto.jogadasPermitidas().get(
                        contexto.jogadasPermitidas().size() - 1
                );
        JogadorPadrao jogador = new JogadorPadrao("Ana", PRIMEIRA_JOGADA);
        var idOriginal = jogador.id();

        jogador.alterarEstrategiaDeDecisao(ultimaJogada);

        assertEquals(idOriginal, jogador.id());
        assertSame(ultimaJogada, jogador.estrategiaDeDecisao());
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
}
