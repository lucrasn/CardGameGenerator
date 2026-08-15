package br.edu.uepb.map.cardgame.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coleção controlada de cartas pertencentes a uma mão.
 *
 * @param <C> tipo de carta da mão
 * @author Júlio
 * @since 1.0
 */
public interface MaoDeCartas<C extends Carta> {

    /**
     * @return quantidade atual de cartas
     */
    int quantidade();

    /**
     * @return {@code true} quando a mão não possui cartas
     */
    default boolean estaVazia() {
        return quantidade() == 0;
    }

    /**
     * Adiciona uma carta à mão.
     *
     * @param carta carta não nula e ainda ausente da mão
     */
    void adicionar(C carta);

    /**
     * Procura uma carta por sua identidade.
     *
     * @param id identificador não nulo
     * @return carta correspondente ou vazio
     */
    Optional<C> buscar(UUID id);

    /**
     * Verifica se a identidade informada pertence à mão.
     *
     * @param id identificador não nulo
     * @return {@code true} se a carta estiver presente
     */
    default boolean contem(UUID id) {
        return buscar(id).isPresent();
    }

    /**
     * Verifica se a carta informada pertence à mão, comparando sua identidade.
     *
     * @param carta carta não nula
     * @return {@code true} se a carta estiver presente
     */
    default boolean contem(C carta) {
        return contem(carta.id());
    }

    /**
     * Remove uma carta por sua identidade.
     *
     * @param id identificador não nulo
     * @return carta removida
     * @throws java.util.NoSuchElementException se a carta não pertencer à mão
     */
    C remover(UUID id);

    /**
     * Remove a carta informada usando sua identidade.
     *
     * @param carta carta não nula
     * @return instância removida da mão
     * @throws java.util.NoSuchElementException se a carta não pertencer à mão
     */
    default C remover(C carta) {
        return remover(carta.id());
    }

    /**
     * Devolve um snapshot imutável na ordem atual da mão.
     *
     * @return cópia imutável das cartas
     */
    List<C> cartas();
}
