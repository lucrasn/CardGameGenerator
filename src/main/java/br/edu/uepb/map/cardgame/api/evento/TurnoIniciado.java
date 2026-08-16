package br.edu.uepb.map.cardgame.api.evento;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento publicado imediatamente antes de o participante executar seu turno.
 *
 * @param numeroDoTurno número lógico do turno, começando em um
 * @param jogador participante que possui a vez
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
public record TurnoIniciado(long numeroDoTurno, Jogador jogador)
        implements EventoDePartida {

    /**
     * Valida os dados obrigatórios do turno.
     *
     * @throws IllegalArgumentException se o número do turno for menor que um
     * @throws NullPointerException se o jogador for nulo
     */
    public TurnoIniciado {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(jogador, "O jogador do turno não pode ser nulo.");
    }
}
