package br.edu.uepb.map.trinca;

import java.util.Objects;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Carta;

/**
 * Carta francesa usada pela aplicação Trinca.
 *
 * @param id identificador único da carta
 * @param valor valor da carta
 * @param naipe naipe da carta
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public record CartaTrinca(UUID id, Valor valor, Naipe naipe) implements Carta {

    /**
     * Valida os componentes de uma carta.
     *
     * @throws NullPointerException se algum componente for nulo
     */
    public CartaTrinca {
        Objects.requireNonNull(id, "O ID da carta não pode ser nulo.");
        Objects.requireNonNull(valor, "O valor da carta não pode ser nulo.");
        Objects.requireNonNull(naipe, "O naipe da carta não pode ser nulo.");
    }

    /**
     * Cria uma carta com identificador gerado automaticamente.
     *
     * @param valor valor da carta
     * @param naipe naipe da carta
     * @throws NullPointerException se o valor ou o naipe for nulo
     */
    public CartaTrinca(Valor valor, Naipe naipe) {
        this(UUID.randomUUID(), valor, naipe);
    }

    /**
     * Retorna a descrição textual da carta.
     *
     * @return valor e naipe da carta em formato legível
     */
    @Override
    public String toString() {
        return valor.simbolo() + " de " + naipe.descricao();
    }
}
