package br.edu.uepb.map.cardgame.api;

import java.util.Objects;

/**
 * Dados imutáveis oferecidos a uma regra de validação.
 *
 * <p>O contexto reúne a ação pretendida e uma visão somente leitura da partida. Ele
 * não oferece operações de mutação, garantindo que a validação aconteça antes da
 * aplicação da jogada.
 *
 * @param <C> tipo de carta usado pela partida
 * @param partida visão somente leitura do estado atual
 * @param jogada ação que será validada
 * @author Lívia
 * @version 0.0.1
 */
public record ContextoDeValidacao<C extends Carta>(
        VisaoDaPartida<C> partida, Jogada jogada) {

    /**
     * Valida os componentes obrigatórios do contexto.
     *
     * @throws NullPointerException se a partida ou a jogada forem nulas
     */
    public ContextoDeValidacao {
        Objects.requireNonNull(partida, "A visão da partida não pode ser nula.");
        Objects.requireNonNull(jogada, "A jogada não pode ser nula.");
    }
}
