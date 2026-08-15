package br.edu.uepb.map.cardgame.api;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Avaliação imutável produzida quando a regra reconhece o fim da partida.
 *
 * @param vencedores participantes vencedores ou empatados; pode ser vazio
 * @param motivo motivo comum ou específico do jogo
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public record DesfechoDePartida(List<Jogador> vencedores, MotivoDeEncerramento motivo) {

    /**
     * Cria um desfecho por cópia defensiva e valida suas invariantes.
     *
     * @throws NullPointerException se a lista, o motivo, algum vencedor ou sua
     *         identidade forem nulos
     * @throws IllegalArgumentException se uma identidade se repetir, uma vitória não
     *         indicar vencedor ou o motivo representar vitória e empate ao mesmo tempo
     */
    public DesfechoDePartida {
        Objects.requireNonNull(vencedores, "A lista de vencedores não pode ser nula.");
        Objects.requireNonNull(motivo, "O motivo de encerramento não pode ser nulo.");
        vencedores = List.copyOf(vencedores);
        Set<UUID> ids = new HashSet<>();
        for (Jogador jogador : vencedores) {
            Objects.requireNonNull(jogador.id(), "A identidade do vencedor não pode ser nula.");
            if (!ids.add(jogador.id())) {
                throw new IllegalArgumentException("Um vencedor foi informado mais de uma vez.");
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
}
