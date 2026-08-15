package br.edu.uepb.map.cardgame.api;

/**
 * Estratégia que escolhe uma ação a partir de uma visão controlada da partida.
 *
 * <p>Implementações podem representar uma pessoa, um bot ou um agente automático.
 * Elas não devem depender de detalhes internos do {@code core}.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
@FunctionalInterface
public interface EstrategiaDeDecisao {

    /**
     * Escolhe uma das ações permitidas pelo contexto.
     *
     * @param contexto visão somente leitura disponível para a decisão
     * @return ação escolhida, nunca {@code null}
     */
    Jogada decidir(ContextoDeDecisao contexto);
}
