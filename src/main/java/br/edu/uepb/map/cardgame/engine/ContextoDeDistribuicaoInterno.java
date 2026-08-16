package br.edu.uepb.map.cardgame.engine;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.Carta;
import br.edu.uepb.map.cardgame.api.ContextoDeDistribuicao;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Adaptador interno entre a Strategy de distribuição e o estado da partida.
 *
 * <p>A estratégia recebe somente a porta estreita definida pela Trilha B; não vê o
 * baralho nem as mãos mutáveis mantidas pelo engine.
 *
 * @param <C> tipo de carta distribuído
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
final class ContextoDeDistribuicaoInterno<C extends Carta>
        implements ContextoDeDistribuicao<C> {

    private final PartidaEmExecucao<C> partida;

    /**
     * Cria o adaptador para uma execução em andamento.
     *
     * @param partida execução que receberá as cartas distribuídas
     * @throws NullPointerException se {@code partida} for nula
     */
    ContextoDeDistribuicaoInterno(PartidaEmExecucao<C> partida) {
        this.partida = Objects.requireNonNull(partida, "A partida não pode ser nula.");
    }

    @Override
    public List<Jogador> jogadores() {
        return partida.jogadores();
    }

    @Override
    public int cartasDisponiveis() {
        return partida.quantidadeNoBaralho();
    }

    @Override
    public void entregarProximaCarta(Jogador jogador) {
        partida.entregarProximaCarta(jogador);
    }
}
