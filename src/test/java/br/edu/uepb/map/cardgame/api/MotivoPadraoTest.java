package br.edu.uepb.map.cardgame.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MotivoPadrao — motivos comuns a diferentes jogos")
class MotivoPadraoTest {

    @Test
    @DisplayName("apenas VITORIA classifica como vitória")
    void somenteVitoria() {
        assertTrue(MotivoPadrao.VITORIA.ehVitoria());
        assertFalse(MotivoPadrao.EMPATE.ehVitoria());
        assertFalse(MotivoPadrao.ESGOTAMENTO.ehVitoria());
        assertFalse(MotivoPadrao.ABANDONO.ehVitoria());
    }

    @Test
    @DisplayName("apenas EMPATE classifica como empate")
    void somenteEmpate() {
        assertTrue(MotivoPadrao.EMPATE.ehEmpate());
        assertFalse(MotivoPadrao.VITORIA.ehEmpate());
        assertFalse(MotivoPadrao.ESGOTAMENTO.ehEmpate());
        assertFalse(MotivoPadrao.ABANDONO.ehEmpate());
    }

    @Test
    @DisplayName("nenhum motivo é vitória e empate ao mesmo tempo")
    void semAmbiguidade() {
        for (MotivoPadrao motivo : MotivoPadrao.values()) {
            assertFalse(motivo.ehVitoria() && motivo.ehEmpate(),
                    motivo + " não pode ser vitória e empate simultaneamente.");
        }
    }

    @Test
    @DisplayName("todas as constantes implementam o contrato de extensão")
    void implementamOContrato() {
        for (MotivoPadrao motivo : MotivoPadrao.values()) {
            assertTrue(motivo instanceof MotivoDeEncerramento);
        }
    }
}
