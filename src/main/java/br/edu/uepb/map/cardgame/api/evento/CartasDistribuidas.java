package br.edu.uepb.map.cardgame.api.evento;

import br.edu.uepb.map.cardgame.api.EventoDePartida;

/**
 * Evento publicado depois que a distribuição inicial de cartas foi concluída.
 *
 * <p>O evento divulga somente a quantidade restante no baralho. Ele não expõe as
 * cartas ou as mãos particulares dos jogadores.
 *
 * @param cartasRestantesNoBaralho quantidade de cartas disponíveis após a distribuição
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
public record CartasDistribuidas(int cartasRestantesNoBaralho)
        implements EventoDePartida {

    /**
     * Valida a quantidade pública do baralho.
     *
     * @throws IllegalArgumentException se a quantidade for negativa
     */
    public CartasDistribuidas {
        if (cartasRestantesNoBaralho < 0) {
            throw new IllegalArgumentException(
                    "A quantidade restante no baralho não pode ser negativa.");
        }
    }
}
