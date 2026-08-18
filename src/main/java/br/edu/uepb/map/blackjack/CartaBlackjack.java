package br.edu.uepb.map.blackjack;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Carta;

/**
 * Carta francesa do cliente Blackjack.
 *
 * @param id identidade única e estável
 * @param valor valor facial da carta
 * @param naipe naipe da carta
 */
public record CartaBlackjack(UUID id, ValorBlackjack valor, NaipeBlackjack naipe)
        implements Carta {

    /**
     * Valida os componentes obrigatórios da carta.
     *
     * @throws NullPointerException se algum componente for nulo
     */
    public CartaBlackjack {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(valor, "O valor da carta não pode ser nulo.");
        Objects.requireNonNull(naipe, "O naipe da carta não pode ser nulo.");
    }

    /**
     * Cria uma carta com identidade nova.
     *
     * @param valor valor facial
     * @param naipe naipe
     */
    public CartaBlackjack(ValorBlackjack valor, NaipeBlackjack naipe) {
        this(UUID.randomUUID(), valor, naipe);
    }

    @Override
    public String toString() {
        return valor.simbolo() + " de " + naipe.descricao();
    }
}
