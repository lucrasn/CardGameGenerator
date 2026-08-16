package br.edu.uepb.map.cardgame.api.evento;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.ResultadoDoTurno;

/**
 * Evento publicado depois que um turno válido foi concluído.
 *
 * @param numeroDoTurno número lógico do turno concluído
 * @param jogador participante que concluiu o turno
 * @param resultado diretiva produzida pelo turno antes de ser aplicada pelo motor
 * @author Lívia
 * @version 0.0.1
 */
public record TurnoEncerrado(
        long numeroDoTurno, Jogador jogador, ResultadoDoTurno resultado)
        implements EventoDePartida {

    /**
     * Valida os dados obrigatórios do turno concluído.
     *
     * @throws IllegalArgumentException se o número do turno for menor que um
     * @throws NullPointerException se o jogador ou o resultado forem nulos
     */
    public TurnoEncerrado {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(jogador, "O jogador do turno não pode ser nulo.");
        Objects.requireNonNull(resultado, "O resultado do turno não pode ser nulo.");
    }
}
