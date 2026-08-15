package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.apoio.JogadorDeTeste;

@DisplayName("ResultadoDePartida — semântica e imutabilidade")
class ResultadoDePartidaTest {

    private enum MotivoEspecifico implements MotivoDeEncerramento {
        EMPATE_TECNICO;

        @Override
        public boolean ehEmpate() {
            return true;
        }
    }

    private final Jogador ana = new JogadorDeTeste("Ana");
    private final Jogador bruno = new JogadorDeTeste("Bruno");

    @Test
    @DisplayName("empate depende do motivo explícito, não da quantidade de vencedores")
    void empateEhExplicito() {
        ResultadoDePartida empateSemVencedor = new ResultadoDePartida(
                List.of(), Map.of(ana, 0, bruno, 0), MotivoPadrao.EMPATE);
        ResultadoDePartida covitoria = new ResultadoDePartida(
                List.of(ana, bruno), Map.of(ana, 1, bruno, 1), MotivoPadrao.VITORIA);
        ResultadoDePartida empateEspecifico = new ResultadoDePartida(
                List.of(), Map.of(ana, 0, bruno, 0), MotivoEspecifico.EMPATE_TECNICO);

        assertTrue(empateSemVencedor.houveEmpate());
        assertTrue(empateEspecifico.houveEmpate());
        assertFalse(covitoria.houveEmpate());
    }

    @Test
    @DisplayName("reconhece vencedor único e consulta pontuação por identidade")
    void vencedorUnico() {
        ResultadoDePartida resultado = new ResultadoDePartida(
                List.of(ana), Map.of(ana, 1, bruno, 0), MotivoPadrao.VITORIA);

        assertSame(ana, resultado.vencedorUnico().orElseThrow());
        Jogador mesmaIdentidade = new JogadorDeTeste(ana.id(), "Outra representação");
        assertFalse(ana.equals(mesmaIdentidade));
        assertFalse(mesmaIdentidade.equals(ana));
        assertEquals(1, resultado.pontuacaoDe(mesmaIdentidade).orElseThrow());
    }

    @Test
    @DisplayName("copia defensivamente vencedores e placar")
    void copiaColecoes() {
        List<Jogador> vencedores = new ArrayList<>(List.of(ana));
        Map<Jogador, Integer> placar = new HashMap<>(Map.of(ana, 1, bruno, 0));
        ResultadoDePartida resultado = new ResultadoDePartida(
                vencedores, placar, MotivoPadrao.VITORIA);

        vencedores.clear();
        placar.clear();

        assertTrue(resultado.vencedorUnico().isPresent());
        assertThrows(UnsupportedOperationException.class,
                () -> resultado.placar().put(ana, 9));
    }

    @Test
    @DisplayName("não há vencedor único quando houve empate ou co-vitória")
    void semVencedorUnico() {
        ResultadoDePartida empate = new ResultadoDePartida(
                List.of(ana), Map.of(ana, 0, bruno, 0), MotivoPadrao.EMPATE);
        ResultadoDePartida covitoria = new ResultadoDePartida(
                List.of(ana, bruno), Map.of(ana, 1, bruno, 1), MotivoPadrao.VITORIA);
        ResultadoDePartida semNinguem = new ResultadoDePartida(
                List.of(), Map.of(ana, 0, bruno, 0), MotivoPadrao.ESGOTAMENTO);

        assertTrue(empate.vencedorUnico().isEmpty());
        assertTrue(covitoria.vencedorUnico().isEmpty());
        assertTrue(semNinguem.vencedorUnico().isEmpty());
    }

    @Test
    @DisplayName("jogador ausente do placar devolve vazio em vez de null")
    void pontuacaoAusente() {
        ResultadoDePartida resultado = new ResultadoDePartida(
                List.of(ana), Map.of(ana, 1), MotivoPadrao.VITORIA);

        assertTrue(resultado.pontuacaoDe(bruno).isEmpty());
        assertThrows(NullPointerException.class, () -> resultado.pontuacaoDe(null));
    }

    @Test
    @DisplayName("a lista de vencedores devolvida não pode ser modificada")
    void vencedoresImutaveis() {
        ResultadoDePartida resultado = new ResultadoDePartida(
                List.of(ana), Map.of(ana, 1, bruno, 0), MotivoPadrao.VITORIA);

        assertThrows(UnsupportedOperationException.class,
                () -> resultado.vencedores().add(bruno));
    }

    @Test
    @DisplayName("nenhum componente pode ser nulo")
    void componentesNulos() {
        assertThrows(NullPointerException.class,
                () -> new ResultadoDePartida(null, Map.of(), MotivoPadrao.EMPATE));
        assertThrows(NullPointerException.class,
                () -> new ResultadoDePartida(List.of(), null, MotivoPadrao.EMPATE));
        assertThrows(NullPointerException.class,
                () -> new ResultadoDePartida(List.of(), Map.of(), null));
    }

    @Test
    @DisplayName("rejeita vitória vazia, vencedor repetido ou ausente do placar")
    void validaInvariantes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoDePartida(List.of(), Map.of(), MotivoPadrao.VITORIA));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoDePartida(
                        List.of(ana, ana), Map.of(ana, 1), MotivoPadrao.VITORIA));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoDePartida(
                        List.of(ana), Map.of(bruno, 0), MotivoPadrao.VITORIA));

        UUID identidadeRepetida = UUID.randomUUID();
        Jogador primeiro = new JogadorDeTeste(identidadeRepetida, "Primeiro");
        Jogador segundo = new JogadorDeTeste(identidadeRepetida, "Segundo");
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoDePartida(
                        List.of(), Map.of(primeiro, 1, segundo, 2), MotivoPadrao.EMPATE));
    }
}
