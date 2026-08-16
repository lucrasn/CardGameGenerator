package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;

class MotorTrincaTest {

    private static final EstrategiaDeDecisao PRIMEIRA_OPCAO =
            contexto -> contexto.jogadasPermitidas().getFirst();

    @Test
    void deveAceitarDeDoisACincoJogadores() {
        assertDoesNotThrow(() -> MotorTrinca.criar(jogadores(2)));
        assertDoesNotThrow(() -> MotorTrinca.criar(jogadores(5)));
    }

    @Test
    void deveRejeitarQuantidadeIncompativelComOBaralho() {
        assertThrows(IllegalArgumentException.class, () -> MotorTrinca.criar(jogadores(1)));
        assertThrows(IllegalArgumentException.class, () -> MotorTrinca.criar(jogadores(6)));
    }

    private static List<Jogador> jogadores(int quantidade) {
        return IntStream.rangeClosed(1, quantidade)
                .mapToObj(indice -> (Jogador) new JogadorPadrao(
                        "Jogador " + indice, PRIMEIRA_OPCAO))
                .toList();
    }
}
