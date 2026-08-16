package br.edu.uepb.map.cardgame.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação reutilizável de {@link Jogador} baseada em composição.
 *
 * <p>A classe representa apenas identidade e decisão. Mãos e pontuação são mantidas
 * pelo estado da partida, permitindo que um mesmo jogador possua diferentes
 * quantidades de mãos em jogos distintos.
 *
 * <p><strong>Componente reutilizável.</strong> Humano, bot e casa podem usar esta
 * mesma implementação; o comportamento variável fica em
 * {@link EstrategiaDeDecisao}. Dessa forma, o framework reutiliza a identidade do
 * participante sem criar subclasses por perfil.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public final class JogadorPadrao implements Jogador {

    private final UUID id;
    private final String nome;
    private EstrategiaDeDecisao estrategia;

    /**
     * Cria um jogador com identificador exclusivo.
     *
     * @param nome nome não vazio
     * @param estrategia estratégia inicial
     * @throws NullPointerException se algum argumento for nulo
     * @throws IllegalArgumentException se o nome estiver vazio
     */
    public JogadorPadrao(String nome, EstrategiaDeDecisao estrategia) {
        this.id = UUID.randomUUID();
        this.nome = validarNome(nome);
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String nome() {
        return nome;
    }

    @Override
    public EstrategiaDeDecisao estrategiaDeDecisao() {
        return estrategia;
    }

    /**
     * Substitui o comportamento de decisão sem alterar a identidade do jogador.
     *
     * @param novaEstrategia nova estratégia
     * @throws NullPointerException se a estratégia for nula
     */
    public void alterarEstrategiaDeDecisao(EstrategiaDeDecisao novaEstrategia) {
        estrategia = Objects.requireNonNull(novaEstrategia, "novaEstrategia");
    }

    @Override
    public String toString() {
        return nome;
    }

    private static String validarNome(String nome) {
        String nomeNormalizado = Objects.requireNonNull(nome, "nome").strip();
        if (nomeNormalizado.isEmpty()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        return nomeNormalizado;
    }
}
