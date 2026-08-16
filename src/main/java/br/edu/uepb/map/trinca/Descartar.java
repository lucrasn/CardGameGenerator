package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogada;

/** Ação de retirar uma carta da mão e colocá-la no descarte. */
public record Descartar(UUID cartaId, CartaTrinca carta) implements Jogada {

    public Descartar {
        Objects.requireNonNull(cartaId, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        if (!cartaId.equals(carta.id())) {
            throw new IllegalArgumentException("O ID deve identificar a carta informada.");
        }
    }

    public Descartar(CartaTrinca carta) {
        this(Objects.requireNonNull(carta, "A carta não pode ser nula.").id(), carta);
    }

    @Override
    public String toString() {
        return "Descartar " + carta;
    }
}
