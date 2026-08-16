package br.edu.uepb.map.cardgame.api;

import java.util.List;

/**
 * Porta controlada usada por estratégias para distribuir cartas.
 *
 * <p>O contexto não expõe o baralho nem as mãos mutáveis. A estratégia escolhe
 * quantidade, ordem e destinatários chamando somente
 * {@link #entregarProximaCarta(Jogador)}.
 *
 * @param <C> tipo de carta distribuído
 * @author Júlio
 */
public interface ContextoDeDistribuicao<C extends Carta> {

    /**
     * Devolve a ordem configurada dos jogadores em snapshot imutável.
     *
     * @return jogadores da partida
     */
    List<Jogador> jogadores();

    /**
     * @return quantidade de cartas ainda disponível para distribuição
     */
    int cartasDisponiveis();

    /**
     * Compra a carta do topo e a entrega à mão do jogador informado.
     *
     * @param jogador jogador pertencente ao contexto
     * @throws IllegalArgumentException se o jogador não pertencer ao contexto
     * @throws br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException se o baralho estiver vazio
     */
    void entregarProximaCarta(Jogador jogador);
}
