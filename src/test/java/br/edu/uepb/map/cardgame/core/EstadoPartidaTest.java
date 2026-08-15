package br.edu.uepb.map.cardgame.core;

import static br.edu.uepb.map.cardgame.core.EstadoPartida.AGUARDANDO_JOGADORES;
import static br.edu.uepb.map.cardgame.core.EstadoPartida.DISTRIBUINDO_CARTAS;
import static br.edu.uepb.map.cardgame.core.EstadoPartida.FINALIZADO;
import static br.edu.uepb.map.cardgame.core.EstadoPartida.TURNO_EM_ANDAMENTO;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("EstadoPartida — máquina de estados da partida")
class EstadoPartidaTest {

    @Nested
    @DisplayName("Transições legais")
    class TransicoesLegais {

        @Test
        @DisplayName("o caminho feliz da partida é permitido do início ao fim")
        void caminhoFelizEhPermitido() {
            assertTrue(AGUARDANDO_JOGADORES.podeTransitarPara(DISTRIBUINDO_CARTAS));
            assertTrue(DISTRIBUINDO_CARTAS.podeTransitarPara(TURNO_EM_ANDAMENTO));
            assertTrue(TURNO_EM_ANDAMENTO.podeTransitarPara(FINALIZADO));
        }

        @Test
        @DisplayName("o laço de turnos permanece no mesmo estado a cada novo turno")
        void turnoTransitaParaSiMesmo() {
            assertTrue(TURNO_EM_ANDAMENTO.podeTransitarPara(TURNO_EM_ANDAMENTO));
        }
    }

    @Nested
    @DisplayName("Transições ilegais")
    class TransicoesIlegais {

        @Test
        @DisplayName("não se distribuem cartas pulando a preparação da partida")
        void naoPulaEtapas() {
            assertFalse(AGUARDANDO_JOGADORES.podeTransitarPara(TURNO_EM_ANDAMENTO));
            assertFalse(AGUARDANDO_JOGADORES.podeTransitarPara(FINALIZADO));
            assertFalse(DISTRIBUINDO_CARTAS.podeTransitarPara(FINALIZADO));
        }

        @Test
        @DisplayName("a partida não volta a um estado anterior")
        void naoRetrocede() {
            assertFalse(DISTRIBUINDO_CARTAS.podeTransitarPara(AGUARDANDO_JOGADORES));
            assertFalse(TURNO_EM_ANDAMENTO.podeTransitarPara(DISTRIBUINDO_CARTAS));
            assertFalse(FINALIZADO.podeTransitarPara(TURNO_EM_ANDAMENTO));
        }

        @ParameterizedTest
        @EnumSource(EstadoPartida.class)
        @DisplayName("FINALIZADO é terminal: não transita para nenhum estado, nem para si")
        void finalizadoEhTerminal(EstadoPartida destino) {
            assertFalse(FINALIZADO.podeTransitarPara(destino));
        }
    }

    @Nested
    @DisplayName("Invariantes do enum")
    class Invariantes {

        @ParameterizedTest
        @EnumSource(EstadoPartida.class)
        @DisplayName("todo estado tem tabela de transições — nenhum ficou de fora")
        void todoEstadoTemTabela(EstadoPartida estado) {
            // Falharia com NullPointerException se um estado novo fosse acrescentado
            // ao enum sem entrada correspondente na tabela.
            assertTrue(estado.destinosLegais() != null);
        }

        @ParameterizedTest
        @EnumSource(EstadoPartida.class)
        @DisplayName("os destinos legais são imutáveis (requisito 7)")
        void destinosSaoImutaveis(EstadoPartida estado) {
            assertThrows(UnsupportedOperationException.class,
                    () -> estado.destinosLegais().add(FINALIZADO));
        }

        @Test
        @DisplayName("apenas FINALIZADO é terminal")
        void apenasFinalizadoEhTerminal() {
            assertTrue(FINALIZADO.ehTerminal());
            assertFalse(AGUARDANDO_JOGADORES.ehTerminal());
            assertFalse(DISTRIBUINDO_CARTAS.ehTerminal());
            assertFalse(TURNO_EM_ANDAMENTO.ehTerminal());
        }

        @Test
        @DisplayName("consultar transição para null é erro de programação")
        void destinoNuloEhRejeitado() {
            assertThrows(NullPointerException.class,
                    () -> AGUARDANDO_JOGADORES.podeTransitarPara(null));
        }
    }
}
