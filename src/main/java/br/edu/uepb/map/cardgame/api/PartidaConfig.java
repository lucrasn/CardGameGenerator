package br.edu.uepb.map.cardgame.api;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Configuração imutável das colaborações necessárias ao ciclo de vida da partida.
 *
 * <p>O Builder evita um construtor posicional longo. Fábrica e distribuição são
 * pontos de extensão fornecidos pela Trilha B. Regras e eventos não aparecem neste
 * contrato enquanto as interfaces correspondentes da Trilha D ainda não definem
 * operações; isso preserva a propriedade dos contratos entre as trilhas.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public final class PartidaConfig<C extends Carta> {

    private final List<Jogador> jogadores;
    private final BaralhoFactory<C> baralhoFactory;
    private final EstrategiaDeDistribuicao<C> distribuicao;
    private final int primeiroJogador;

    private PartidaConfig(Builder<C> builder) {
        this.jogadores = validarJogadores(builder.jogadores);
        this.baralhoFactory = Objects.requireNonNull(
                builder.baralhoFactory, "A fábrica de baralho é obrigatória.");
        this.distribuicao = Objects.requireNonNull(
                builder.distribuicao, "A estratégia de distribuição é obrigatória.");
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

    /** @return participantes em lista imutável */
    public List<Jogador> jogadores() {
        return jogadores;
    }

    /** @return fábrica de baralho configurada */
    public BaralhoFactory<C> baralhoFactory() {
        return baralhoFactory;
    }

    /** @return estratégia de distribuição configurada */
    public EstrategiaDeDistribuicao<C> distribuicao() {
        return distribuicao;
    }

    /** @return índice do primeiro participante na lista */
    public int primeiroJogador() {
        return primeiroJogador;
    }

    /** Construtor fluente da configuração imutável. */
    public static final class Builder<C extends Carta> {

        private List<Jogador> jogadores;
        private BaralhoFactory<C> baralhoFactory;
        private EstrategiaDeDistribuicao<C> distribuicao;
        private int primeiroJogador;

        private Builder() {
        }

        /**
         * @param jogadores participantes em ordem de turnos
         * @return este Builder
         */
        public Builder<C> jogadores(List<Jogador> jogadores) {
            this.jogadores = jogadores;
            return this;
        }

        /**
         * @param baralhoFactory fábrica da composição de cartas
         * @return este Builder
         */
        public Builder<C> baralhoFactory(BaralhoFactory<C> baralhoFactory) {
            this.baralhoFactory = baralhoFactory;
            return this;
        }

        /**
         * @param distribuicao estratégia de distribuição inicial
         * @return este Builder
         */
        public Builder<C> distribuicao(EstrategiaDeDistribuicao<C> distribuicao) {
            this.distribuicao = distribuicao;
            return this;
        }

        /**
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
         */
        public PartidaConfig<C> build() {
            return new PartidaConfig<>(this);
        }
    }
}
