package br.edu.uepb.map.cardgame.api.evento;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento publicado quando uma jogada é recusada e o turno pode ser tentado novamente.
 *
 * <p>O evento informa somente o motivo destinado ao cliente. Ele não expõe a
 * exceção nem dados privados da mão do participante.
 *
 * @param numeroDoTurno número lógico do turno em que ocorreu a rejeição
 * @param jogador participante cuja jogada foi rejeitada
 * @param motivo explicação não vazia da regra violada
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
public record JogadaRejeitada(long numeroDoTurno, Jogador jogador, String motivo)
        implements EventoDePartida {

    /**
     * Valida e normaliza os dados da rejeição.
     *
     * @throws IllegalArgumentException se o número for menor que um ou o motivo for vazio
     * @throws NullPointerException se o jogador ou o motivo forem nulos
     */
    public JogadaRejeitada {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(jogador, "O jogador da jogada não pode ser nulo.");
        motivo = Objects.requireNonNull(motivo, "O motivo não pode ser nulo.").strip();
        if (motivo.isEmpty()) {
            throw new IllegalArgumentException("O motivo da rejeição não pode ser vazio.");
        }
    }
}
