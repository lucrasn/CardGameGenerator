package br.edu.uepb.map.cardgame.api.io;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControleEntradaSaidaTest {

    @Test
    void exibeMensagemNoFluxoInjetado() {
        StringWriter saida = new StringWriter();
        ControleEntradaSaida controle = new ControleEntradaSaida(
                new StringReader(""),
                saida
        );

        controle.exibir("Turno iniciado");

        assertTrue(saida.toString().contains("Turno iniciado"));
    }

    @Test
    void repetePerguntaAteReceberOpcaoValida() {
        StringWriter saida = new StringWriter();
        ControleEntradaSaida controle = new ControleEntradaSaida(
                new StringReader("texto\n3\n2\n"),
                saida
        );

        int escolhida = controle.solicitarOpcao(
                "Escolha:",
                List.of("Comprar", "Descartar")
        );

        assertEquals(1, escolhida);
        assertTrue(saida.toString().contains("1 - Comprar"));
        assertTrue(saida.toString().contains("2 - Descartar"));
        assertTrue(saida.toString().contains("Opção inválida"));
    }

    @Test
    void rejeitaListaVaziaEFimPrematuroDaEntrada() {
        ControleEntradaSaida controle = new ControleEntradaSaida(
                new StringReader(""),
                new StringWriter()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controle.solicitarOpcao("Escolha:", List.of())
        );
        assertThrows(
                IllegalStateException.class,
                () -> controle.solicitarOpcao("Escolha:", List.of("Comprar"))
        );
    }
}
