package br.edu.uepb.map.cardgame.api.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Escolhe aleatoriamente uma das ações permitidas.
 *
 * <p>A fonte de aleatoriedade pode ser injetada, mantendo a estratégia reproduzível
 * nos testes sem acoplar o domínio a estado global.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public final class DecisaoAleatoria implements EstrategiaDeDecisao {

    private final Random aleatorio;

    /**
     * Cria a estratégia com uma fonte de aleatoriedade padrão.
     */
    public DecisaoAleatoria() {
        this(new Random());
    }

    /**
     * Cria a estratégia com uma fonte injetada, útil para testes reproduzíveis.
     *
     * @param aleatorio fonte de aleatoriedade
     * @throws NullPointerException se a fonte for nula
     */
    public DecisaoAleatoria(Random aleatorio) {
        this.aleatorio = Objects.requireNonNull(aleatorio, "aleatorio");
    }

    @Override
    public Jogada decidir(ContextoDeDecisao contexto) {
        List<Jogada> jogadas = jogadasDisponiveis(contexto);
        return jogadas.get(aleatorio.nextInt(jogadas.size()));
    }

    private static List<Jogada> jogadasDisponiveis(ContextoDeDecisao contexto) {
        List<Jogada> jogadas = Objects.requireNonNull(contexto, "contexto")
                .jogadasPermitidas();
        if (jogadas.isEmpty()) {
            throw new IllegalStateException("não há jogadas permitidas");
        }
        return jogadas;
    }
}
