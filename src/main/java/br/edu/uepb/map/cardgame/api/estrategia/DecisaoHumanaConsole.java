package br.edu.uepb.map.cardgame.api.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Solicita a uma pessoa a escolha de uma ação por uma porta de entrada e saída.
 *
 * <p>A estratégia converte ações tipadas em descrições apenas na fronteira de I/O;
 * nenhuma {@code String} de console trafega pelo motor como jogada de domínio.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public final class DecisaoHumanaConsole implements EstrategiaDeDecisao {

    private final EntradaSaida entradaSaida;
    private final Function<? super Jogada, String> descricaoDaJogada;

    /**
     * Cria uma estratégia que descreve cada ação por {@link Object#toString()}.
     *
     * @param entradaSaida porta usada para interagir com a pessoa
     */
    public DecisaoHumanaConsole(EntradaSaida entradaSaida) {
        this(entradaSaida, Object::toString);
    }

    /**
     * @param entradaSaida porta usada para interagir com a pessoa
     * @param descricaoDaJogada conversor de uma ação para texto apresentável
     */
    public DecisaoHumanaConsole(
            EntradaSaida entradaSaida,
            Function<? super Jogada, String> descricaoDaJogada
    ) {
        this.entradaSaida = Objects.requireNonNull(entradaSaida, "entradaSaida");
        this.descricaoDaJogada = Objects.requireNonNull(
                descricaoDaJogada,
                "descricaoDaJogada"
        );
    }

    @Override
    public Jogada decidir(ContextoDeDecisao contexto) {
        List<Jogada> jogadas = Objects.requireNonNull(contexto, "contexto")
                .jogadasPermitidas();
        if (jogadas.isEmpty()) {
            throw new IllegalStateException("não há jogadas permitidas");
        }

        List<String> opcoes = jogadas.stream()
                .map(descricaoDaJogada)
                .map(descricao -> Objects.requireNonNull(
                        descricao,
                        "descrição da jogada"
                ))
                .toList();
        int indice = entradaSaida.solicitarOpcao("Escolha uma jogada:", opcoes);
        if (indice < 0 || indice >= jogadas.size()) {
            throw new IllegalStateException("entrada e saída retornou opção inválida");
        }
        return jogadas.get(indice);
    }
}
