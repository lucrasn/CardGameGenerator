package br.edu.uepb.map.cardgame.api;

import java.util.Map;

/**
 * Estratégia que calcula a pontuação final dos participantes.
 *
 * <p>A pontuação é mantida separada da validação e da vitória para que cada regra
 * possa variar de forma independente. A implementação deve apenas observar a
 * partida e devolver um novo mapa, sem modificar o estado recebido.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lívia
 * @version 0.0.1
 */
@FunctionalInterface
public interface RegraDePontuacaoStrategy<C extends Carta> {

    /**
     * Calcula o placar associado ao desfecho reconhecido.
     *
     * @param contexto visão somente leitura imediatamente antes da finalização
     * @param desfecho desfecho já reconhecido pela regra de vitória
     * @return mapa contendo exatamente todos os participantes e suas pontuações
     */
    Map<Jogador, Integer> calcular(
            VisaoDaPartida<C> contexto, DesfechoDePartida desfecho);
}
