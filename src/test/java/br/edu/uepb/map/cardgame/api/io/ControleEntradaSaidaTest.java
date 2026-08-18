package br.edu.uepb.map.cardgame.api.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void solicitaInteiroSemRenderizarOpcoesERepeteAteEntrarNoIntervalo() {
        StringWriter saida = new StringWriter();
        ControleEntradaSaida controle = new ControleEntradaSaida(
                new StringReader("texto\n1\n6\n4\n"),
                saida
        );

        int quantidade = controle.solicitarInteiro(
                "Digite a quantidade:", 2, 5);

        assertEquals(4, quantidade);
        assertTrue(saida.toString().contains("Digite a quantidade:"));
        assertTrue(saida.toString().contains("Valor inválido"));
        assertFalse(saida.toString().contains("1 -"));
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
        assertThrows(
                IllegalArgumentException.class,
                () -> controle.solicitarInteiro("Quantidade:", 5, 2)
        );
    }

    @Test
    void rejeitaArgumentosNulos() {
        assertThrows(
                NullPointerException.class,
                () -> new ControleEntradaSaida(null, new StringWriter())
        );
        assertThrows(
                NullPointerException.class,
                () -> new ControleEntradaSaida(new StringReader(""), null)
        );

        ControleEntradaSaida controle = new ControleEntradaSaida(
                new StringReader("1\n"),
                new StringWriter()
        );
        assertThrows(NullPointerException.class, () -> controle.exibir(null));
        assertThrows(
                NullPointerException.class,
                () -> controle.solicitarOpcao(null, List.of("Comprar"))
        );
        assertThrows(
                NullPointerException.class,
                () -> controle.solicitarOpcao("Escolha:", null)
        );
        assertThrows(
                NullPointerException.class,
                () -> controle.solicitarOpcao(
                        "Escolha:",
                        java.util.Arrays.asList("Comprar", null)
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> controle.solicitarInteiro(null, 2, 5)
        );
    }

    @Test
    void converteFalhaDeLeituraEmExcecaoNaoVerificada() {
        Reader entradaComFalha = new Reader() {
            @Override
            public int read(char[] buffer, int inicio, int quantidade)
                    throws IOException {
                throw new IOException("falha simulada");
            }

            @Override
            public void close() {
                // O controle não assume a propriedade do fluxo recebido.
            }
        };
        ControleEntradaSaida controle = new ControleEntradaSaida(
                entradaComFalha,
                new StringWriter()
        );

        assertThrows(
                UncheckedIOException.class,
                () -> controle.solicitarOpcao("Escolha:", List.of("Comprar"))
        );
    }
}
