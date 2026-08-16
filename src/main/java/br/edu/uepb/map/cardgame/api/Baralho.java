package br.edu.uepb.map.cardgame.api;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

/**
 * Sequência ordenada e encapsulada de cartas disponíveis para compra.
 *
 * <p>A primeira posição de {@link #cartas()} representa o topo. Nenhuma operação
 * expõe a coleção mutável interna.
 *
 * @param <C> tipo de carta armazenado
 * @author Júlio
 */
public interface Baralho<C extends Carta> {

    /**
     * @return quantidade atual de cartas
     */
    int quantidade();

    /**
     * @return {@code true} quando não há cartas disponíveis
     */
    default boolean estaVazio() {
        return quantidade() == 0;
    }

    /**
     * Consulta a carta do topo sem removê-la.
     *
     * @return topo do baralho ou vazio quando não houver carta
     */
    Optional<C> topo();

    /**
     * Remove e devolve a carta do topo.
     *
     * @return carta comprada
     * @throws BaralhoVazioException se não houver carta disponível
     */
    C comprar();

    /**
     * Coloca uma carta no topo.
     *
     * @param carta carta não nula e ainda ausente do baralho
     */
    void colocarNoTopo(C carta);

    /**
     * Coloca uma carta na base.
     *
     * @param carta carta não nula e ainda ausente do baralho
     */
    void colocarNaBase(C carta);

    /**
     * Embaralha usando o gerador aleatório padrão da plataforma.
     */
    default void embaralhar() {
        embaralhar(RandomGenerator.getDefault());
    }

    /**
     * Embaralha usando o gerador informado, permitindo testes determinísticos.
     *
     * @param gerador gerador aleatório não nulo
     */
    void embaralhar(RandomGenerator gerador);

    /**
     * Devolve um snapshot imutável, ordenado do topo para a base.
     *
     * <p>Alterações posteriores no baralho não modificam o snapshot devolvido.
     *
     * @return cópia imutável das cartas
     */
    List<C> cartas();
}
