package br.edu.uepb.map.cardgame.api;

/**
 * Define como as cartas iniciais são entregues aos participantes.
 *
 * <p>Implementações podem variar quantidade, ordem e destinatários sem alterar o
 * motor da partida.
 *
 * @param <C> tipo de carta distribuído
 * @author Júlio
 */
@FunctionalInterface
public interface EstrategiaDeDistribuicao<C extends Carta> {

    /**
     * Executa a distribuição por meio das operações controladas do contexto.
     *
     * @param contexto contexto não nulo da distribuição
     */
    void distribuir(ContextoDeDistribuicao<C> contexto);
}
