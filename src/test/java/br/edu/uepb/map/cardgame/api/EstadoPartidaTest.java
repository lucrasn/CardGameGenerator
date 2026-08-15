package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EstadoPartida — máquina de estados")
class EstadoPartidaTest {

    @Nested
    @DisplayName("Transições legais")
    class TransicoesLegais {

        @Test
        @DisplayName("o caminho feliz percorre os quatro estados em ordem")
        void caminhoCompleto() {
            assertTrue(EstadoPartida.CONFIGURADA.podeTransitarPara(EstadoPartida.PREPARANDO));
            assertTrue(EstadoPartida.PREPARANDO.podeTransitarPara(EstadoPartida.EM_ANDAMENTO));
            assertTrue(EstadoPartida.EM_ANDAMENTO.podeTransitarPara(EstadoPartida.FINALIZADA));
        }

        @Test
        @DisplayName("nenhum estado transita para si mesmo")
        void semAutoTransicao() {
            for (EstadoPartida estado : EstadoPartida.values()) {
                assertFalse(estado.podeTransitarPara(estado),
                        estado + " não deveria transitar para si mesmo.");
            }
        }

        @Test
        @DisplayName("pular etapas é recusado")
        void naoPulaEtapas() {
            assertFalse(EstadoPartida.CONFIGURADA.podeTransitarPara(EstadoPartida.EM_ANDAMENTO));
            assertFalse(EstadoPartida.CONFIGURADA.podeTransitarPara(EstadoPartida.FINALIZADA));
            assertFalse(EstadoPartida.PREPARANDO.podeTransitarPara(EstadoPartida.FINALIZADA));
        }

        @Test
        @DisplayName("voltar atrás é recusado")
        void naoRetrocede() {
            assertFalse(EstadoPartida.PREPARANDO.podeTransitarPara(EstadoPartida.CONFIGURADA));
            assertFalse(EstadoPartida.EM_ANDAMENTO.podeTransitarPara(EstadoPartida.PREPARANDO));
            assertFalse(EstadoPartida.FINALIZADA.podeTransitarPara(EstadoPartida.EM_ANDAMENTO));
        }
    }

    @Nested
    @DisplayName("Estado terminal")
    class Terminal {

        @Test
        @DisplayName("apenas FINALIZADA é terminal")
        void somenteFinalizada() {
            assertTrue(EstadoPartida.FINALIZADA.ehTerminal());
            assertFalse(EstadoPartida.CONFIGURADA.ehTerminal());
            assertFalse(EstadoPartida.PREPARANDO.ehTerminal());
            assertFalse(EstadoPartida.EM_ANDAMENTO.ehTerminal());
        }

        @Test
        @DisplayName("FINALIZADA não transita para nenhum estado")
        void finalizadaNaoTransita() {
            for (EstadoPartida destino : EstadoPartida.values()) {
                assertFalse(EstadoPartida.FINALIZADA.podeTransitarPara(destino));
            }
            assertEquals(Set.of(), EstadoPartida.FINALIZADA.destinosLegais());
        }
    }

    @Nested
    @DisplayName("Integridade da tabela")
    class IntegridadeDaTabela {

        @Test
        @DisplayName("nenhum estado ficou de fora da tabela de transições")
        void todosOsEstadosTemEntrada() {
            for (EstadoPartida estado : EstadoPartida.values()) {
                // Um estado ausente da tabela lançaria NullPointerException aqui.
                assertTrue(estado.destinosLegais().size() <= EstadoPartida.values().length);
            }
        }

        @Test
        @DisplayName("os destinos legais são imutáveis (requisito 7)")
        void destinosSaoImutaveis() {
            assertThrows(UnsupportedOperationException.class,
                    () -> EstadoPartida.CONFIGURADA.destinosLegais()
                            .add(EstadoPartida.FINALIZADA));
        }

        @Test
        @DisplayName("destino nulo é rejeitado em vez de devolver false silenciosamente")
        void destinoNulo() {
            assertThrows(NullPointerException.class,
                    () -> EstadoPartida.CONFIGURADA.podeTransitarPara(null));
        }
    }
}
