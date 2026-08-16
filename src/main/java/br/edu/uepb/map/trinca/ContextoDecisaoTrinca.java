package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.Jogador;

/** Visão imutável oferecida à pessoa ou bot que decide a jogada. */
public record ContextoDecisaoTrinca(
        EtapaTrinca etapa,
        List<Jogada> jogadasPermitidas,
        Jogador jogador,
        List<CartaTrinca> mao,
        Optional<CartaTrinca> topoDoDescarte
) implements ContextoDeDecisao {

    public ContextoDecisaoTrinca {
        Objects.requireNonNull(etapa, "A etapa não pode ser nula.");
        jogadasPermitidas = List.copyOf(jogadasPermitidas);
        Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        mao = List.copyOf(mao);
        Objects.requireNonNull(topoDoDescarte, "O topo do descarte não pode ser nulo.");
    }
}
