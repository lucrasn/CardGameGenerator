package br.edu.uepb.map.trinca;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

class RegraVitoriaTrincaTest {

    @Test
    void naoDeveEncerrarPorEmpateQuandoOMonteEstiverVazio() {
        Jogador jogador = new JogadorPadrao(
                "Ana", contexto -> contexto.jogadasPermitidas().getFirst());
        CartaTrinca cartaSolta = new CartaTrinca(Valor.AS, Naipe.COPAS);
        VisaoDaPartida<CartaTrinca> partida = new VisaoFalsa(
                jogador, List.of(cartaSolta), 0, 10);

        assertTrue(new RegraVitoriaTrinca().avaliar(partida).isEmpty());
    }

    private record VisaoFalsa(
            Jogador jogador,
            List<CartaTrinca> mao,
            int quantidadeNoBaralho,
            long numeroDoTurno) implements VisaoDaPartida<CartaTrinca> {

        @Override
        public EstadoPartida estado() {
            return EstadoPartida.EM_ANDAMENTO;
        }

        @Override
        public List<Jogador> jogadores() {
            return List.of(jogador);
        }

        @Override
        public Jogador jogadorAtual() {
            return jogador;
        }

        @Override
        public List<CartaTrinca> maoDe(Jogador participante) {
            return mao;
        }
    }
}
