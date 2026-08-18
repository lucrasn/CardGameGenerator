package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogada;

/**
 * Ação de comprar o topo visível da pilha de descarte.
 *
 * @param cartaId identificador da carta escolhida
 * @param carta carta visível no topo do descarte
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public record ComprarDoDescarte(UUID cartaId, CartaTrinca carta) implements Jogada {

    /**
     * Valida a correspondência entre a carta e seu identificador.
     *
     * @throws NullPointerException se algum componente for nulo
     * @throws IllegalArgumentException se o identificador não pertencer à carta
     */
    public ComprarDoDescarte {
        Objects.requireNonNull(cartaId, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        if (!cartaId.equals(carta.id())) {
            throw new IllegalArgumentException("O ID deve identificar a carta informada.");
        }
    }

    /**
     * Cria a ação a partir da carta disponível no descarte.
     *
     * @param carta carta escolhida
     * @throws NullPointerException se a carta for nula
     */
    public ComprarDoDescarte(CartaTrinca carta) {
        this(Objects.requireNonNull(carta, "A carta não pode ser nula.").id(), carta);
    }

    /**
     * Retorna o nome da ação.
     *
     * @return descrição da compra no descarte
     */
    @Override
    public String toString() {
        return "Comprar do descarte";
    }
}
