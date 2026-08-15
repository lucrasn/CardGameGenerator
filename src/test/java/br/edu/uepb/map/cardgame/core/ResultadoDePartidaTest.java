package br.edu.uepb.map.cardgame.core;

import static br.edu.uepb.map.cardgame.core.ResultadoDePartida.MotivoDeEncerramento.EMPATE;
import static br.edu.uepb.map.cardgame.core.ResultadoDePartida.MotivoDeEncerramento.VITORIA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.core.apoio.JogadorFalso;

@DisplayName("ResultadoDePartida — desfecho da partida")
class ResultadoDePartidaTest {

    private Jogador ana;
    private Jogador bruno;

    @BeforeEach
    void criarJogadores() {
        ana = new JogadorFalso("Ana");
        bruno = new JogadorFalso("Bruno");
    }

    @Nested
    @DisplayName("Vencedor e empate")
    class VencedorEEmpate {

        @Test
        @DisplayName("um único vencedor é reconhecido")
        void vencedorUnico() {
            var resultado = new ResultadoDePartida(
                    List.of(ana), Map.of(ana, 10, bruno, 4), VITORIA);

            assertFalse(resultado.houveEmpate());
            assertTrue(resultado.vencedorUnico().isPresent());
            assertSame(ana, resultado.vencedorUnico().orElseThrow());
        }

        @Test
        @DisplayName("dois vencedores caracterizam empate e não há vencedor único")
        void empate() {
            var resultado = new ResultadoDePartida(
                    List.of(ana, bruno), Map.of(ana, 7, bruno, 7), EMPATE);

            assertTrue(resultado.houveEmpate());
            assertTrue(resultado.vencedorUnico().isEmpty());
        }

        @Test
        @DisplayName("partida sem vencedor não tem vencedor único nem empate")
        void semVencedor() {
            var resultado = new ResultadoDePartida(
                    List.of(), Map.of(ana, 0, bruno, 0),
                    ResultadoDePartida.MotivoDeEncerramento.BARALHO_ESGOTADO);

            assertFalse(resultado.houveEmpate());
            assertTrue(resultado.vencedorUnico().isEmpty());
        }
    }

    @Nested
    @DisplayName("Placar")
    class Placar {

        @Test
        @DisplayName("a pontuação de um jogador presente é devolvida")
        void pontuacaoPresente() {
            var resultado = new ResultadoDePartida(
                    List.of(ana), Map.of(ana, 21, bruno, 18), VITORIA);

            assertEquals(21, resultado.pontuacaoDe(ana).orElseThrow());
            assertEquals(18, resultado.pontuacaoDe(bruno).orElseThrow());
        }

        @Test
        @DisplayName("jogador ausente do placar devolve vazio em vez de null")
        void pontuacaoAusente() {
            var resultado = new ResultadoDePartida(List.of(ana), Map.of(ana, 21), VITORIA);
            assertTrue(resultado.pontuacaoDe(bruno).isEmpty());
        }
    }

    @Nested
    @DisplayName("Imutabilidade (requisito 7)")
    class Imutabilidade {

        @Test
        @DisplayName("as coleções devolvidas não podem ser modificadas")
        void colecoesDevolvidasSaoImutaveis() {
            var resultado = new ResultadoDePartida(
                    List.of(ana), Map.of(ana, 10), VITORIA);

            assertThrows(UnsupportedOperationException.class,
                    () -> resultado.vencedores().add(bruno));
            assertThrows(UnsupportedOperationException.class,
                    () -> resultado.placar().put(bruno, 99));
        }

        @Test
        @DisplayName("alterar as coleções originais depois não afeta o resultado")
        void copiaDefensivaNaEntrada() {
            List<Jogador> vencedores = new ArrayList<>(List.of(ana));
            Map<Jogador, Integer> placar = new HashMap<>(Map.of(ana, 10));

            var resultado = new ResultadoDePartida(vencedores, placar, VITORIA);

            vencedores.add(bruno);
            placar.put(bruno, 99);

            assertEquals(1, resultado.vencedores().size());
            assertEquals(1, resultado.placar().size());
            assertFalse(resultado.houveEmpate());
        }
    }

    @Nested
    @DisplayName("Validação")
    class Validacao {

        @Test
        @DisplayName("nenhum componente pode ser nulo")
        void componentesNulos() {
            assertThrows(NullPointerException.class,
                    () -> new ResultadoDePartida(null, Map.of(), VITORIA));
            assertThrows(NullPointerException.class,
                    () -> new ResultadoDePartida(List.of(), null, VITORIA));
            assertThrows(NullPointerException.class,
                    () -> new ResultadoDePartida(List.of(), Map.of(), null));
        }
    }
}
