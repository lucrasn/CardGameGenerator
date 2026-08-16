package br.edu.uepb.map.cardgame.api.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BaralhoVazioException")
class BaralhoVazioExceptionTest {

    @Test
    @DisplayName("usa uma mensagem padrão clara")
    void usaMensagemPadrao() {
        var excecao = new BaralhoVazioException();

        assertEquals("Não há cartas disponíveis no baralho.", excecao.getMessage());
    }

    @Test
    @DisplayName("aceita uma mensagem específica")
    void aceitaMensagemEspecifica() {
        var excecao = new BaralhoVazioException("O monte de compra está vazio.");

        assertEquals("O monte de compra está vazio.", excecao.getMessage());
    }

    @Test
    @DisplayName("faz parte da hierarquia de exceções da partida")
    void pertenceAHierarquiaDaPartida() {
        assertTrue(new BaralhoVazioException() instanceof PartidaException);
    }
}
