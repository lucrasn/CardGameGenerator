package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ResultadoDoTurno — diretiva declarativa devolvida pelo jogo")
class ResultadoDoTurnoTest {

    @Nested
    @DisplayName("Fábricas nomeadas")
    class Fabricas {

        @Test
        @DisplayName("avancar não repete, não inverte e não pula")
        void avancar() {
            ResultadoDoTurno diretiva = ResultadoDoTurno.avancar();

            assertFalse(diretiva.repetirJogador());
            assertFalse(diretiva.inverterSentido());
            assertEquals(0, diretiva.jogadoresAPular());
        }

        @Test
        @DisplayName("repetir mantém o jogador sem pular ninguém")
        void repetir() {
            ResultadoDoTurno diretiva = ResultadoDoTurno.repetir();

            assertTrue(diretiva.repetirJogador());
            assertEquals(0, diretiva.jogadoresAPular());
        }

        @Test
        @DisplayName("inverter troca o sentido e avança")
        void inverter() {
            ResultadoDoTurno diretiva = ResultadoDoTurno.inverter();

            assertTrue(diretiva.inverterSentido());
            assertFalse(diretiva.repetirJogador());
        }

        @Test
        @DisplayName("pular registra a quantidade e avança")
        void pular() {
            ResultadoDoTurno diretiva = ResultadoDoTurno.pular(2);

            assertEquals(2, diretiva.jogadoresAPular());
            assertFalse(diretiva.repetirJogador());
            assertFalse(diretiva.inverterSentido());
        }
    }

    @Nested
    @DisplayName("Combinações incoerentes")
    class Validacao {

        @Test
        @DisplayName("pular quantidade negativa é rejeitado")
        void puloNegativo() {
            assertThrows(IllegalArgumentException.class, () -> ResultadoDoTurno.pular(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> new ResultadoDoTurno(false, false, -3));
        }

        @Test
        @DisplayName("repetir o jogador e pular participantes são mutuamente exclusivos")
        void repetirEPular() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ResultadoDoTurno(true, false, 1));
        }

        @Test
        @DisplayName("repetir combinado com inverter é permitido")
        void repetirEInverter() {
            ResultadoDoTurno diretiva = new ResultadoDoTurno(true, true, 0);

            assertTrue(diretiva.repetirJogador());
            assertTrue(diretiva.inverterSentido());
        }
    }
}
