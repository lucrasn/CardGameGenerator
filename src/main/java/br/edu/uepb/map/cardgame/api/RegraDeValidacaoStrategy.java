package br.edu.uepb.map.cardgame.api;

import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;

/**
 * Estratégia que valida uma jogada antes de ela alterar o estado da partida.
 *
 * <p>Cada jogo fornece sua própria implementação, permitindo substituir as regras
 * sem modificar o motor. Uma validação aprovada termina normalmente; uma validação
 * rejeitada lança {@link JogadaInvalidaException}.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
@FunctionalInterface
public interface RegraDeValidacaoStrategy<C extends Carta> {

    /**
     * Verifica se a jogada pode ser aplicada ao estado observado.
     *
     * <p>A implementação não deve modificar a partida. Toda validação deve acontecer
     * antes das mutações que concretizam a jogada.
     *
     * @param contexto jogada e visão somente leitura da partida
     * @throws JogadaInvalidaException se a jogada violar uma regra do jogo
     */
    void validar(ContextoDeValidacao<C> contexto);
}
