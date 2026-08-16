package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogada;

/** Ação de comprar o topo visível da pilha de descarte. */
public record ComprarDoDescarte(UUID cartaId, CartaTrinca carta) implements Jogada {

    public ComprarDoDescarte {
        Objects.requireNonNull(cartaId, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        if (!cartaId.equals(carta.id())) {
            throw new IllegalArgumentException("O ID deve identificar a carta informada.");
        }
    }

    public ComprarDoDescarte(CartaTrinca carta) {
        this(Objects.requireNonNull(carta, "A carta não pode ser nula.").id(), carta);
    }

    @Override
    public String toString() {
        return "Comprar do descarte (" + carta + ")";
    }
}
