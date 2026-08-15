package br.edu.uepb.map.cardgame.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Desfecho de uma partida: quem venceu, com que placar e por qual motivo ela terminou.
 *
 * <p>É o valor devolvido pelo método {@code executar()} do {@link MotorDePartida}.
 * Existir como tipo próprio evita que o jogo cliente tenha de interrogar o estado
 * interno do motor depois da partida para descobrir o que aconteceu.
 *
 * <p><strong>Empate é modelado explicitamente.</strong> {@code vencedores} com dois ou
 * mais elementos representa empate; vazio representa partida encerrada sem vencedor.
 * Um {@code Optional<Jogador>} no lugar da lista obrigaria a mentir sobre empates.
 *
 * <p><strong>Encapsulamento (requisito 7).</strong> Os acessores gerados por um
 * {@code record} devolvem a própria referência do campo — não há como interceptá-los.
 * Por isso a imutabilidade é garantida na entrada, no construtor compacto, com
 * {@link List#copyOf(java.util.Collection)} e {@link Map#copyOf(Map)}: o que é
 * guardado já é imutável, então devolvê-lo diretamente é seguro.
 *
 * @param vencedores jogadores vencedores; vazio se não houve vencedor, dois ou mais se houve empate
 * @param placar pontuação final por jogador
 * @param motivo circunstância que encerrou a partida
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public record ResultadoDePartida(List<Jogador> vencedores,
                                 Map<Jogador, Integer> placar,
                                 MotivoDeEncerramento motivo) {

    /** Circunstância que levou a partida ao fim. */
    public enum MotivoDeEncerramento {

        /** A regra de vitória apontou um único vencedor. */
        VITORIA,

        /** A regra de vitória apontou mais de um vencedor com a mesma condição. */
        EMPATE,

        /** Não havia mais cartas para prosseguir. */
        BARALHO_ESGOTADO,

        /** A partida foi interrompida antes de uma condição de vitória ser atingida. */
        ABANDONO
    }

    /**
     * Construtor compacto: valida os argumentos e congela as coleções recebidas.
     *
     * @throws NullPointerException se qualquer argumento, ou algum elemento das
     *         coleções, for {@code null}
     */
    public ResultadoDePartida {
        Objects.requireNonNull(vencedores, "A lista de vencedores não pode ser nula.");
        Objects.requireNonNull(placar, "O placar não pode ser nulo.");
        Objects.requireNonNull(motivo, "O motivo de encerramento não pode ser nulo.");
        vencedores = List.copyOf(vencedores);
        placar = Map.copyOf(placar);
    }

    /**
     * Indica se a partida terminou empatada.
     *
     * @return {@code true} se há mais de um vencedor
     */
    public boolean houveEmpate() {
        return vencedores.size() > 1;
    }

    /**
     * Vencedor único da partida, quando existe exatamente um.
     *
     * @return o vencedor, ou {@link Optional#empty()} se houve empate ou nenhum vencedor
     */
    public Optional<Jogador> vencedorUnico() {
        return vencedores.size() == 1 ? Optional.of(vencedores.get(0)) : Optional.empty();
    }

    /**
     * Pontuação final de um jogador.
     *
     * @param jogador jogador consultado
     * @return a pontuação registrada, ou {@link Optional#empty()} se o jogador não
     *         consta do placar
     */
    public Optional<Integer> pontuacaoDe(Jogador jogador) {
        return Optional.ofNullable(placar.get(jogador));
    }
}
