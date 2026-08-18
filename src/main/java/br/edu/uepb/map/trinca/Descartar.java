package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Jogada;

/**
 * Ação de retirar uma carta da mão e colocá-la no descarte.
 *
 * @param cartaId identificador da carta escolhida
 * @param carta carta que será descartada
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public record Descartar(UUID cartaId, CartaTrinca carta) implements Jogada {

    /**
     * Valida a correspondência entre a carta e seu identificador.
     *
     * @throws NullPointerException se algum componente for nulo
     * @throws IllegalArgumentException se o identificador não pertencer à carta
     */
    public Descartar {
        Objects.requireNonNull(cartaId, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        if (!cartaId.equals(carta.id())) {
            throw new IllegalArgumentException("O ID deve identificar a carta informada.");
        }
    }

    /**
     * Cria a ação a partir da carta selecionada.
     *
     * @param carta carta que será descartada
     * @throws NullPointerException se a carta for nula
     */
    public Descartar(CartaTrinca carta) {
        this(Objects.requireNonNull(carta, "A carta não pode ser nula.").id(), carta);
    }

    /**
     * Retorna a descrição da ação e da carta selecionada.
     *
     * @return descrição do descarte
     */
    @Override
    public String toString() {
        return "Descartar " + carta;
    }
}
