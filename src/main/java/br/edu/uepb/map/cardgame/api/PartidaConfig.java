package br.edu.uepb.map.cardgame.api;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Configuração imutável das colaborações necessárias ao ciclo de vida da partida.
 *
 * <p>O Builder evita um construtor posicional longo. Fábrica, distribuição e regras
 * são pontos de extensão obrigatórios: cada jogo informa como criar e distribuir o
 * baralho, validar jogadas, reconhecer a vitória e calcular a pontuação.
 *
 * @param <C> tipo de carta usado pela partida
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public final class PartidaConfig<C extends Carta> {

    private final List<Jogador> jogadores;
    private final BaralhoFactory<C> baralhoFactory;
    private final EstrategiaDeDistribuicao<C> distribuicao;
    private final RegraDeValidacaoStrategy<C> regraDeValidacao;
    private final RegraDeVitoriaStrategy<C> regraDeVitoria;
    private final RegraDePontuacaoStrategy<C> regraDePontuacao;
    private final int primeiroJogador;

    private PartidaConfig(Builder<C> builder) {
        this.jogadores = validarJogadores(builder.jogadores);
        this.baralhoFactory = Objects.requireNonNull(
                builder.baralhoFactory, "A fábrica de baralho é obrigatória.");
        this.distribuicao = Objects.requireNonNull(
                builder.distribuicao, "A estratégia de distribuição é obrigatória.");
        this.regraDeValidacao = Objects.requireNonNull(
                builder.regraDeValidacao, "A regra de validação é obrigatória.");
        this.regraDeVitoria = Objects.requireNonNull(
                builder.regraDeVitoria, "A regra de vitória é obrigatória.");
        this.regraDePontuacao = Objects.requireNonNull(
                builder.regraDePontuacao, "A regra de pontuação é obrigatória.");
        if (builder.primeiroJogador < 0 || builder.primeiroJogador >= jogadores.size()) {
            throw new IllegalArgumentException("O índice do primeiro jogador é inválido: "
                    + builder.primeiroJogador + ".");
        }
        this.primeiroJogador = builder.primeiroJogador;
    }

    private static List<Jogador> validarJogadores(List<Jogador> jogadores) {
        Objects.requireNonNull(jogadores, "A lista de jogadores é obrigatória.");
        List<Jogador> copia = List.copyOf(jogadores);
        if (copia.size() < 2) {
            throw new IllegalArgumentException("Uma partida exige ao menos dois jogadores.");
        }
        Set<UUID> identidades = new HashSet<>();
        for (Jogador jogador : copia) {
            UUID id = Objects.requireNonNull(
                    jogador.id(), "A identidade do jogador não pode ser nula.");
            if (!identidades.add(id)) {
                throw new IllegalArgumentException(
                        "Há jogadores repetidos na configuração: " + id + ".");
            }
        }
        return copia;
    }

    /**
     * Cria um Builder vazio.
     *
     * @param <C> tipo de carta da configuração
     * @return novo Builder
     */
    public static <C extends Carta> Builder<C> builder() {
        return new Builder<>();
    }

    /**
     * Consulta os participantes na ordem configurada.
     *
     * @return participantes em lista imutável
     */
    public List<Jogador> jogadores() {
        return jogadores;
    }

    /**
     * Consulta a fábrica que criará um novo baralho para a execução.
     *
     * @return fábrica de baralho configurada
     */
    public BaralhoFactory<C> baralhoFactory() {
        return baralhoFactory;
    }

    /**
     * Consulta o algoritmo de distribuição inicial.
     *
     * @return estratégia de distribuição configurada
     */
    public EstrategiaDeDistribuicao<C> distribuicao() {
        return distribuicao;
    }

    /**
     * Consulta a regra que validará cada jogada antes de sua aplicação.
     *
     * @return estratégia de validação configurada
     */
    public RegraDeValidacaoStrategy<C> regraDeValidacao() {
        return regraDeValidacao;
    }

    /**
     * Consulta a regra que decidirá quando a partida terminou.
     *
     * @return estratégia de vitória configurada
     */
    public RegraDeVitoriaStrategy<C> regraDeVitoria() {
        return regraDeVitoria;
    }

    /**
     * Consulta a regra que calculará o placar final.
     *
     * @return estratégia de pontuação configurada
     */
    public RegraDePontuacaoStrategy<C> regraDePontuacao() {
        return regraDePontuacao;
    }

    /**
     * Consulta a posição de quem começará a partida.
     *
     * @return índice do primeiro participante na lista
     */
    public int primeiroJogador() {
        return primeiroJogador;
    }

    /**
     * Construtor fluente da configuração imutável.
     *
     * @param <C> tipo de carta usado pela partida configurada
     */
    public static final class Builder<C extends Carta> {

        private List<Jogador> jogadores;
        private BaralhoFactory<C> baralhoFactory;
        private EstrategiaDeDistribuicao<C> distribuicao;
        private RegraDeValidacaoStrategy<C> regraDeValidacao;
        private RegraDeVitoriaStrategy<C> regraDeVitoria;
        private RegraDePontuacaoStrategy<C> regraDePontuacao;
        private int primeiroJogador;

        private Builder() {
        }

        /**
         * Define os participantes e sua ordem de turnos.
         *
         * @param jogadores participantes em ordem de turnos
         * @return este Builder
         */
        public Builder<C> jogadores(List<Jogador> jogadores) {
            this.jogadores = jogadores;
            return this;
        }

        /**
         * Define como o baralho da execução será criado.
         *
         * @param baralhoFactory fábrica da composição de cartas
         * @return este Builder
         */
        public Builder<C> baralhoFactory(BaralhoFactory<C> baralhoFactory) {
            this.baralhoFactory = baralhoFactory;
            return this;
        }

        /**
         * Define como as cartas iniciais serão entregues.
         *
         * @param distribuicao estratégia de distribuição inicial
         * @return este Builder
         */
        public Builder<C> distribuicao(EstrategiaDeDistribuicao<C> distribuicao) {
            this.distribuicao = distribuicao;
            return this;
        }

        /**
         * Define como as jogadas serão validadas.
         *
         * @param regraDeValidacao estratégia de validação de jogadas
         * @return este Builder
         */
        public Builder<C> regraDeValidacao(
                RegraDeValidacaoStrategy<C> regraDeValidacao) {
            this.regraDeValidacao = regraDeValidacao;
            return this;
        }

        /**
         * Define como o encerramento da partida será reconhecido.
         *
         * @param regraDeVitoria estratégia de vitória
         * @return este Builder
         */
        public Builder<C> regraDeVitoria(RegraDeVitoriaStrategy<C> regraDeVitoria) {
            this.regraDeVitoria = regraDeVitoria;
            return this;
        }

        /**
         * Define como o placar final será calculado.
         *
         * @param regraDePontuacao estratégia de pontuação
         * @return este Builder
         */
        public Builder<C> regraDePontuacao(
                RegraDePontuacaoStrategy<C> regraDePontuacao) {
            this.regraDePontuacao = regraDePontuacao;
            return this;
        }

        /**
         * Define quem começa a partida.
         *
         * @param primeiroJogador índice inicial, zero por padrão
         * @return este Builder
         */
        public Builder<C> primeiroJogador(int primeiroJogador) {
            this.primeiroJogador = primeiroJogador;
            return this;
        }

        /**
         * Valida e cria a configuração.
         *
         * @return configuração imutável
         * @throws NullPointerException se jogadores, fábrica, distribuição, regras,
         *         algum jogador ou alguma identidade forem nulos
         * @throws IllegalArgumentException se houver menos de dois jogadores,
         *         identidades repetidas ou índice inicial fora da lista
         */
        public PartidaConfig<C> build() {
            return new PartidaConfig<>(this);
        }
    }
}
