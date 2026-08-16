package br.edu.uepb.map.cardgame.api;

import java.util.UUID;

/**
 * Identidade de um participante de uma partida.
 *
 * <p>Mãos, pontuação e estado da mesa pertencem à partida e aos seus contextos. A
 * forma de decidir é fornecida por composição, permitindo substituir o comportamento
 * sem criar uma nova hierarquia de jogadores.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public interface Jogador {

    /**
     * Identificador estável desta instância de jogador.
     *
     * @return identificador do jogador
     */
    UUID id();

    /**
     * Nome usado para identificar o jogador para as pessoas participantes.
     *
     * @return nome não vazio
     */
    String nome();

    /**
     * Estratégia usada para escolher a próxima ação.
     *
     * @return estratégia atual, nunca {@code null}
     */
    EstrategiaDeDecisao estrategiaDeDecisao();
}
