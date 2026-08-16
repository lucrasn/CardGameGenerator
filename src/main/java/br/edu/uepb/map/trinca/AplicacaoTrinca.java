package br.edu.uepb.map.trinca;

import java.util.List;

import br.edu.uepb.map.cardgame.api.JogadorPadrao;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;
import br.edu.uepb.map.cardgame.api.evento.JogadaRejeitada;
import br.edu.uepb.map.cardgame.api.evento.TurnoIniciado;
import br.edu.uepb.map.cardgame.api.io.ControleEntradaSaida;

/** Ponto de entrada da demonstração humano contra humano em console. */
public final class AplicacaoTrinca {

    private AplicacaoTrinca() {
    }

    public static void main(String[] args) {
        ControleEntradaSaida io = new ControleEntradaSaida();
        DecisaoHumanaTrincaConsole decisao = new DecisaoHumanaTrincaConsole(io);
        JogadorPadrao jogador1 = new JogadorPadrao("Jogador 1", decisao);
        JogadorPadrao jogador2 = new JogadorPadrao("Jogador 2", decisao);

        MotorTrinca motor = MotorTrinca.criar(List.of(jogador1, jogador2));
        motor.adicionarListener(evento -> {
            if (evento instanceof TurnoIniciado turno) {
                io.exibir("\nTurno " + turno.numeroDoTurno() + " — " + turno.jogador().nome());
            } else if (evento instanceof JogadaRejeitada rejeitada) {
                io.exibir("Jogada rejeitada: " + rejeitada.motivo());
            }
        });

        ResultadoDePartida resultado = motor.executar();
        if (resultado.vencedores().isEmpty()) {
            io.exibir("Partida encerrada sem vencedor.");
        } else {
            io.exibir("Vencedor: " + resultado.vencedores().getFirst().nome());
        }
        io.exibir("Placar: " + resultado.placar());
    }
}
