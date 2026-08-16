package br.edu.uepb.map.cardgame.api;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;

/**
 * Porta de operações controladas oferecida ao motor concreto de um jogo.
 *
 * <p>O contexto não permite avançar turnos nem finalizar a partida. Essas
 * responsabilidades permanecem no Template Method do engine. Ele também não
 * expõe as implementações mutáveis de baralho e mão.
 *
 * @param <C> tipo de carta usado pela partida
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
public interface ContextoDePartida<C extends Carta> extends VisaoDaPartida<C> {

    /**
     * Compra uma carta do baralho compartilhado.
     *
     * @return carta retirada
     * @throws BaralhoVazioException se não houver carta disponível
     * @throws EstadoDePartidaInvalidoException se a partida ainda não puder ou já
     *         não puder mais ser modificada
     */
    C comprarDoBaralho();

    /**
     * Acrescenta uma carta a uma mão controlada pelo engine.
     *
     * @param jogador dono da mão
     * @param carta carta proveniente do baralho ou de uma zona do jogo cliente
     * @throws NullPointerException se o jogador, sua identidade, a carta ou sua
     *         identidade forem nulos
     * @throws IllegalArgumentException se o jogador não participar da partida ou a
     *         carta já estiver no baralho ou em alguma mão
     * @throws EstadoDePartidaInvalidoException se a partida não estiver em preparação
     *         ou em andamento
     */
    void adicionarNaMao(Jogador jogador, C carta);

    /**
     * Retira uma carta de uma mão controlada.
     *
     * @param jogador dono da mão
     * @param cartaId identidade da carta
     * @return carta retirada
     * @throws NullPointerException se o jogador, sua identidade ou a identidade da
     *         carta forem nulos
     * @throws IllegalArgumentException se o jogador não pertencer à partida
     * @throws NoSuchElementException se a carta não estiver na mão indicada
     * @throws EstadoDePartidaInvalidoException se a partida não estiver em preparação
     *         ou em andamento
     */
    C removerDaMao(Jogador jogador, UUID cartaId);

    /**
     * Acrescenta ao baralho cartas que não estejam no baralho nem em mãos.
     *
     * @param cartas cartas provenientes de uma zona mantida pelo jogo cliente
     * @throws NullPointerException se a coleção, alguma carta ou identidade forem nulas
     * @throws IllegalArgumentException se houver identidade repetida na coleção ou se
     *         alguma carta já estiver no baralho ou em uma mão
     * @throws EstadoDePartidaInvalidoException se a partida não estiver em preparação
     *         ou em andamento
     */
    void adicionarAoBaralho(Collection<? extends C> cartas);

    /**
     * Embaralha o baralho compartilhado.
     *
     * @throws EstadoDePartidaInvalidoException se a partida não estiver em preparação
     *         ou em andamento
     */
    void embaralharBaralho();
}
