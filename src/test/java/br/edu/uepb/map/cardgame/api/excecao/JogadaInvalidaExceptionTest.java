package br.edu.uepb.map.cardgame.api.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JogadaInvalidaException")
class JogadaInvalidaExceptionTest {

    @Test
    @DisplayName("usa uma mensagem padrão clara")
    void usaMensagemPadrao() {
        var excecao = new JogadaInvalidaException();

        assertEquals("A jogada informada é inválida.", excecao.getMessage());
    }

    @Test
    @DisplayName("aceita uma mensagem que explica a regra violada")
    void aceitaMensagemEspecifica() {
        var excecao = new JogadaInvalidaException(
                "A carta escolhida não pertence à mão do jogador.");

        assertEquals(
                "A carta escolhida não pertence à mão do jogador.",
                excecao.getMessage());
    }

    @Test
    @DisplayName("faz parte da hierarquia de exceções da partida")
    void pertenceAHierarquiaDaPartida() {
        assertTrue(new JogadaInvalidaException() instanceof PartidaException);
    }
}
