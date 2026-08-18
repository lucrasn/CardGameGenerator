package br.edu.uepb.map.blackjack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogador;

/** Estado específico da rodada que não pertence ao agregado genérico do engine. */
final class MesaBlackjack {

    private final Jogador jogador;
    private final Jogador casa;
    private final Set<UUID> participantesQuePararam = new HashSet<>();
    private final Map<UUID, CartaBlackjack> ultimasCartasCompradas = new HashMap<>();
    private boolean maoDaCasaRevelada;

    MesaBlackjack(Jogador jogador, Jogador casa) {
        this.jogador = Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        this.casa = Objects.requireNonNull(casa, "A casa não pode ser nula.");
        Objects.requireNonNull(jogador.id(), "O ID do jogador não pode ser nulo.");
        Objects.requireNonNull(casa.id(), "O ID da casa não pode ser nulo.");
        Objects.requireNonNull(
                jogador.estrategiaDeDecisao(), "A Strategy do jogador não pode ser nula.");
        Objects.requireNonNull(
                casa.estrategiaDeDecisao(), "A Strategy da casa não pode ser nula.");
        if (jogador.id().equals(casa.id())) {
            throw new IllegalArgumentException("Jogador e casa devem ser distintos.");
        }
    }

    Jogador jogador() {
        return jogador;
    }

    Jogador casa() {
        return casa;
    }

    PapelBlackjack papelDe(Jogador participante) {
        exigirParticipante(participante);
        return participante.id().equals(casa.id())
                ? PapelBlackjack.CASA
                : PapelBlackjack.JOGADOR;
    }

    Jogador oponenteDe(Jogador participante) {
        return papelDe(participante) == PapelBlackjack.CASA ? jogador : casa;
    }

    void registrarParada(Jogador participante) {
        exigirParticipante(participante);
        participantesQuePararam.add(participante.id());
    }

    boolean parou(Jogador participante) {
        exigirParticipante(participante);
        return participantesQuePararam.contains(participante.id());
    }

    boolean todosPararam() {
        return parou(jogador) && parou(casa);
    }

    void registrarCompra(Jogador participante, CartaBlackjack carta) {
        exigirParticipante(participante);
        ultimasCartasCompradas.put(
                participante.id(), Objects.requireNonNull(carta, "A carta não pode ser nula."));
    }

    Optional<CartaBlackjack> ultimaCartaCompradaPor(Jogador participante) {
        exigirParticipante(participante);
        return Optional.ofNullable(ultimasCartasCompradas.get(participante.id()));
    }

    boolean revelarMaoDaCasa() {
        if (maoDaCasaRevelada) {
            return false;
        }
        maoDaCasaRevelada = true;
        return true;
    }

    boolean maoDaCasaRevelada() {
        return maoDaCasaRevelada;
    }

    private void exigirParticipante(Jogador participante) {
        Objects.requireNonNull(participante, "O participante não pode ser nulo.");
        UUID id = Objects.requireNonNull(
                participante.id(), "O ID do participante não pode ser nulo.");
        if (!id.equals(jogador.id()) && !id.equals(casa.id())) {
            throw new IllegalArgumentException("O participante não pertence ao Blackjack.");
        }
    }
}
