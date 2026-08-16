package br.edu.uepb.map.cardgame.api.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Escolhe a ação com a maior avaliação imediata.
 *
 * <p>O jogo cliente fornece a função de avaliação porque somente ele conhece o
 * significado de suas ações. Em caso de empate, a primeira ação de maior valor é
 * preservada.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public final class DecisaoGulosa implements EstrategiaDeDecisao {

    private final ToIntFunction<? super Jogada> avaliador;

    /**
     * Cria uma estratégia com a função de avaliação fornecida pelo cliente.
     *
     * @param avaliador função que atribui um valor imediato a cada ação
     * @throws NullPointerException se o avaliador for nulo
     */
    public DecisaoGulosa(ToIntFunction<? super Jogada> avaliador) {
        this.avaliador = Objects.requireNonNull(avaliador, "avaliador");
    }

    @Override
    public Jogada decidir(ContextoDeDecisao contexto) {
        List<Jogada> jogadas = Objects.requireNonNull(contexto, "contexto")
                .jogadasPermitidas();
        if (jogadas.isEmpty()) {
            throw new IllegalStateException("não há jogadas permitidas");
        }

        Jogada melhor = jogadas.get(0);
        int melhorValor = avaliador.applyAsInt(melhor);
        for (int indice = 1; indice < jogadas.size(); indice++) {
            Jogada candidata = jogadas.get(indice);
            int valor = avaliador.applyAsInt(candidata);
            if (valor > melhorValor) {
                melhor = candidata;
                melhorValor = valor;
            }
        }
        return melhor;
    }
}
