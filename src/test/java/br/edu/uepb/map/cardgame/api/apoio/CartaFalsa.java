package br.edu.uepb.map.cardgame.api.apoio;

import java.util.UUID;

import br.edu.uepb.map.cardgame.api.Carta;

/** Carta mínima usada para testar os componentes genéricos da API. */
public record CartaFalsa(UUID id, String rotulo) implements Carta {

    /** Cria uma identidade determinística e legível para os testes. */
    public static CartaFalsa comNumero(long numero) {
        return new CartaFalsa(new UUID(0, numero), "carta-" + numero);
    }
}
