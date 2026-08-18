package br.edu.uepb.map.blackjack.evento;

import java.util.Objects;

import br.edu.uepb.map.blackjack.PapelBlackjack;
import br.edu.uepb.map.blackjack.PontuacaoDaMaoBlackjack;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento emitido quando um participante decide não receber mais cartas.
 *
 * @param numeroDoTurno número lógico do turno
 * @param participante participante que parou
 * @param papel papel desempenhado na rodada
 * @param pontuacao pontuação conservada ao parar
 */
public record ParticipanteParouBlackjack(
        long numeroDoTurno,
        Jogador participante,
        PapelBlackjack papel,
        PontuacaoDaMaoBlackjack pontuacao
) implements EventoDePartida {

    /**
     * Valida os dados obrigatórios do evento.
     *
     * @throws NullPointerException se algum componente for nulo
     * @throws IllegalArgumentException se o número do turno não for positivo
     */
    public ParticipanteParouBlackjack {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(participante, "O participante não pode ser nulo.");
        Objects.requireNonNull(participante.id(), "O ID do participante não pode ser nulo.");
        Objects.requireNonNull(papel, "O papel não pode ser nulo.");
        Objects.requireNonNull(pontuacao, "A pontuação não pode ser nula.");
    }
}
