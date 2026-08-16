package br.edu.uepb.map.cardgame.api.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EstadoDePartidaInvalidoException")
class EstadoDePartidaInvalidoExceptionTest {

    @Test
    @DisplayName("usa uma mensagem padrão clara")
    void usaMensagemPadrao() {
        var excecao = new EstadoDePartidaInvalidoException();

        assertEquals(
                "O estado atual da partida não permite esta operação.",
                excecao.getMessage());
    }

    @Test
    @DisplayName("aceita uma mensagem que explica a operação incompatível")
    void aceitaMensagemEspecifica() {
        var excecao = new EstadoDePartidaInvalidoException(
                "Não é possível realizar uma jogada após o encerramento da partida.");

        assertEquals(
                "Não é possível realizar uma jogada após o encerramento da partida.",
                excecao.getMessage());
    }

    @Test
    @DisplayName("faz parte da hierarquia de exceções da partida")
    void pertenceAHierarquiaDaPartida() {
        assertTrue(new EstadoDePartidaInvalidoException() instanceof PartidaException);
    }
}
