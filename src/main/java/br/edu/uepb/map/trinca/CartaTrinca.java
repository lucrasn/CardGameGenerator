package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Carta;

/** Carta francesa usada pela aplicação Trinca. */
public record CartaTrinca(UUID id, Valor valor, Naipe naipe) implements Carta {

    public CartaTrinca {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(valor, "O valor da carta não pode ser nulo.");
        Objects.requireNonNull(naipe, "O naipe da carta não pode ser nulo.");
    }

    public CartaTrinca(Valor valor, Naipe naipe) {
        this(UUID.randomUUID(), valor, naipe);
    }

    @Override
    public String toString() {
        return valor.simbolo() + " de " + naipe.descricao();
    }
}
