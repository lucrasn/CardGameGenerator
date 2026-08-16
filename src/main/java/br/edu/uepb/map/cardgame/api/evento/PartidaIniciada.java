package br.edu.uepb.map.cardgame.api.evento;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento publicado quando a partida entra em andamento.
 *
 * @param jogadores participantes na ordem configurada para os turnos
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
public record PartidaIniciada(List<Jogador> jogadores) implements EventoDePartida {

    /**
     * Cria o evento preservando um snapshot imutável dos participantes.
     *
     * @throws NullPointerException se a lista ou algum jogador forem nulos
     */
    public PartidaIniciada {
        Objects.requireNonNull(jogadores, "A lista de jogadores não pode ser nula.");
        jogadores = List.copyOf(jogadores);
    }
}
