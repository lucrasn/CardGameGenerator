package br.edu.uepb.map.cardgame.api;

/**
 * Observador interessado nos eventos publicados por uma partida.
 *
 * <p>Implementações podem atualizar o console, registrar um histórico ou alimentar
 * uma interface gráfica sem criar dependência dessas tecnologias dentro do motor.
 *
 * @author Lívia
 * @version 0.0.1
 * @since 2026-08-15
 */
@FunctionalInterface
public interface PartidaListener {

    /**
     * Reage a um fato ocorrido durante a partida.
     *
     * @param evento evento não nulo publicado pelo motor
     */
    void aoOcorrer(EventoDePartida evento);
}
