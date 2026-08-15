package br.edu.uepb.map.cardgame.core.estrategia;

import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.EtapaDeTurno;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextoDeDecisaoPadraoTest {

    @Test
    void protegeAListaDeJogadasPermitidas() {
        Jogada comprar = new Acao("comprar");
        List<Jogada> origem = new ArrayList<>(List.of(comprar));
        ContextoDeDecisaoPadrao contexto = new ContextoDeDecisaoPadrao(
                EtapaTeste.DECISAO,
                origem
        );

        origem.add(new Acao("descartar"));

        assertEquals(List.of(comprar), contexto.jogadasPermitidas());
        assertThrows(
                UnsupportedOperationException.class,
                () -> contexto.jogadasPermitidas().add(new Acao("outra"))
        );
    }

    @Test
    void rejeitaListaNulaOuComElementoNulo() {
        assertThrows(
                NullPointerException.class,
                () -> new ContextoDeDecisaoPadrao(null, List.of(new Acao("comprar")))
        );
        assertThrows(
                NullPointerException.class,
                () -> new ContextoDeDecisaoPadrao(EtapaTeste.DECISAO, null)
        );
        assertThrows(
                NullPointerException.class,
                () -> new ContextoDeDecisaoPadrao(
                        EtapaTeste.DECISAO,
                        java.util.Arrays.asList(new Acao("comprar"), null)
                )
        );
    }

    private record Acao(String descricao) implements Jogada {
    }

    private enum EtapaTeste implements EtapaDeTurno {
        DECISAO
    }
}
