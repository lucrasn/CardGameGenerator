package br.edu.uepb.map.cardgame.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Desfecho e placar imutáveis devolvidos pelo engine.
 *
 * @param vencedores participantes reconhecidos pela regra
 * @param placar pontuação final por participante
 * @param motivo motivo comum ou específico do jogo
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public record ResultadoDePartida(List<Jogador> vencedores,
                                 Map<Jogador, Integer> placar,
                                 MotivoDeEncerramento motivo) {

    /** Faz cópias defensivas e valida as invariantes do resultado. */
    public ResultadoDePartida {
        Objects.requireNonNull(vencedores, "A lista de vencedores não pode ser nula.");
        Objects.requireNonNull(placar, "O placar não pode ser nulo.");
        Objects.requireNonNull(motivo, "O motivo de encerramento não pode ser nulo.");

        Set<UUID> idsDoPlacar = new HashSet<>();
        for (Map.Entry<Jogador, Integer> entrada : placar.entrySet()) {
            Jogador jogador = Objects.requireNonNull(
                    entrada.getKey(), "O placar não pode ter jogador nulo.");
            Objects.requireNonNull(
                    entrada.getValue(), "O placar não pode ter pontuação nula.");
            UUID id = Objects.requireNonNull(
                    jogador.id(), "Um jogador do placar não pode ter id nulo.");
            if (!idsDoPlacar.add(id)) {
                throw new IllegalArgumentException(
                        "O placar não pode repetir a identidade de um jogador.");
            }
        }
        vencedores = List.copyOf(vencedores);
        placar = Collections.unmodifiableMap(new LinkedHashMap<>(placar));

        Set<UUID> idsDosVencedores = new HashSet<>();
        for (Jogador vencedor : vencedores) {
            UUID id = Objects.requireNonNull(
                    vencedor.id(), "Um vencedor não pode ter id nulo.");
            if (!idsDosVencedores.add(id)) {
                throw new IllegalArgumentException("Um vencedor foi informado mais de uma vez.");
            }
            if (!idsDoPlacar.contains(id)) {
                throw new IllegalArgumentException("Todo vencedor precisa constar do placar.");
            }
        }
        boolean vitoria = motivo.ehVitoria();
        boolean empate = motivo.ehEmpate();
        if (vitoria && empate) {
            throw new IllegalArgumentException(
                    "Um motivo não pode representar vitória e empate simultaneamente.");
        }
        if (vitoria && vencedores.isEmpty()) {
            throw new IllegalArgumentException("Uma vitória precisa indicar ao menos um vencedor.");
        }
    }

    /** @return {@code true} somente quando o motivo declara empate */
    public boolean houveEmpate() {
        return motivo.ehEmpate();
    }

    /**
     * @return vencedor único quando há exatamente um e o motivo não é empate
     */
    public Optional<Jogador> vencedorUnico() {
        return !houveEmpate() && vencedores.size() == 1
                ? Optional.of(vencedores.get(0))
                : Optional.empty();
    }

    /**
     * Consulta a pontuação pela identidade do jogador.
     *
     * @param jogador participante consultado
     * @return pontuação registrada, se existir
     */
    public Optional<Integer> pontuacaoDe(Jogador jogador) {
        Objects.requireNonNull(jogador, "O jogador consultado não pode ser nulo.");
        UUID jogadorId = Objects.requireNonNull(
                jogador.id(), "A identidade do jogador consultado não pode ser nula.");
        return placar.entrySet().stream()
                .filter(entrada -> entrada.getKey().id().equals(jogadorId))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
