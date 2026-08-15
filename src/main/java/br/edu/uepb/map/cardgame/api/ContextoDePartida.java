package br.edu.uepb.map.cardgame.api;

import java.util.Collection;
import java.util.UUID;

/**
 * Porta de operações controladas oferecida ao motor concreto de um jogo.
 *
 * <p>O contexto não permite avançar turnos nem finalizar a partida. Essas
 * responsabilidades permanecem no Template Method do engine. Ele também não
 * expõe as implementações mutáveis de baralho e mão.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public interface ContextoDePartida<C extends Carta> extends VisaoDaPartida<C> {

    /**
     * Compra uma carta do baralho compartilhado.
     *
     * @return carta retirada
     */
    C comprarDoBaralho();

    /**
     * Acrescenta uma carta a uma mão controlada pelo engine.
     *
     * @param jogador dono da mão
     * @param carta carta proveniente do baralho ou de uma zona do jogo cliente
     */
    void adicionarNaMao(Jogador jogador, C carta);

    /**
     * Retira uma carta de uma mão controlada.
     *
     * @param jogador dono da mão
     * @param cartaId identidade da carta
     * @return carta retirada
     */
    C removerDaMao(Jogador jogador, UUID cartaId);

    /**
     * Acrescenta ao baralho cartas que não estejam no baralho nem em mãos.
     *
     * @param cartas cartas provenientes de uma zona mantida pelo jogo cliente
     */
    void adicionarAoBaralho(Collection<? extends C> cartas);

    /** Embaralha o baralho compartilhado. */
    void embaralharBaralho();
}
