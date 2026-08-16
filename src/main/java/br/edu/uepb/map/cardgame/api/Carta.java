package br.edu.uepb.map.cardgame.api;

import java.util.UUID;

/**
 * Representa uma carta individual manipulada pelo framework.
 *
 * <p>O contrato não define atributos visuais como naipe, valor, cor ou símbolo,
 * pois eles pertencem aos jogos concretos. Cada implementação deve, entretanto,
 * fornecer uma identidade única e estável. Isso permite distinguir, por exemplo,
 * dois setes de copas provenientes de baralhos franceses diferentes.
 *
 * <p>O identificador não deve mudar durante o ciclo de vida da carta e não deve
 * ser reutilizado por outra carta da mesma partida.
 *
 * @author Júlio
 */
public interface Carta {

    /**
     * Devolve a identidade única e imutável desta carta.
     *
     * @return identificador não nulo da carta
     */
    UUID id();
}
