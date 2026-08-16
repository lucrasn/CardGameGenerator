package br.edu.uepb.map.cardgame.api;

import java.util.Optional;

/**
 * Estratégia que avalia se o estado atual encerra a partida.
 *
 * <p>A regra apenas observa o estado e produz um desfecho quando sua condição de
 * encerramento é satisfeita. Ela não calcula pontuação nem modifica a partida.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lívia
 * @version 0.0.1
 */
@FunctionalInterface
public interface RegraDeVitoriaStrategy<C extends Carta> {

    /**
     * Avalia a condição de encerramento do jogo.
     *
     * @param contexto visão somente leitura da partida
     * @return desfecho quando a partida terminou; vazio quando deve continuar
     */
    Optional<DesfechoDePartida> avaliar(VisaoDaPartida<C> contexto);
}
