package br.edu.uepb.map.cardgame.api;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.excecao.BaralhoVazioException;

/**
 * Distribui uma carta por jogador a cada volta, preservando a ordem configurada.
 *
 * @param <C> tipo de carta distribuído
 * @author Júlio
 */
public final class DistribuicaoAlternada<C extends Carta>
        implements EstrategiaDeDistribuicao<C> {

    private final int cartasPorJogador;

    /**
     * @param cartasPorJogador quantidade positiva entregue a cada jogador
     * @throws IllegalArgumentException se a quantidade não for positiva
     */
    public DistribuicaoAlternada(int cartasPorJogador) {
        if (cartasPorJogador <= 0) {
            throw new IllegalArgumentException("A quantidade por jogador deve ser positiva.");
        }
        this.cartasPorJogador = cartasPorJogador;
    }

    /**
     * @return quantidade configurada para cada jogador
     */
    public int cartasPorJogador() {
        return cartasPorJogador;
    }

    @Override
    public void distribuir(ContextoDeDistribuicao<C> contexto) {
        Objects.requireNonNull(contexto, "O contexto de distribuição não pode ser nulo.");
        List<Jogador> jogadores = contexto.jogadores();
        long totalNecessario = (long) jogadores.size() * cartasPorJogador;

        if (contexto.cartasDisponiveis() < totalNecessario) {
            throw new BaralhoVazioException(
                    "A distribuição exige " + totalNecessario + " cartas, mas há apenas "
                            + contexto.cartasDisponiveis() + ".");
        }

        for (int rodada = 0; rodada < cartasPorJogador; rodada++) {
            for (Jogador jogador : jogadores) {
                contexto.entregarProximaCarta(jogador);
            }
        }
    }
}
