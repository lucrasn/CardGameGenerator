package br.edu.uepb.map.cardgame.api.evento;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.ResultadoDePartida;

/**
 * Evento publicado depois que o resultado final e o estado encerrado já existem.
 *
 * @param resultado resultado imutável produzido pelo motor
 * @author Lívia
 * @version 0.0.1
 */
public record PartidaFinalizada(ResultadoDePartida resultado)
        implements EventoDePartida {

    /**
     * Valida o resultado obrigatório.
     *
     * @throws NullPointerException se o resultado for nulo
     */
    public PartidaFinalizada {
        Objects.requireNonNull(resultado, "O resultado da partida não pode ser nulo.");
    }
}
