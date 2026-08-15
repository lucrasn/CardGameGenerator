package br.edu.uepb.map.cardgame.api;

import java.util.List;

/**
 * Porta de entrada e saída usada por decisões humanas.
 *
 * <p>O isolamento permite testar estratégias sem acessar diretamente
 * {@link System#in} ou {@link System#out}.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public interface EntradaSaida {

    /**
     * Exibe uma mensagem ao usuário.
     *
     * @param mensagem mensagem não nula
     */
    void exibir(String mensagem);

    /**
     * Solicita a escolha de uma opção.
     *
     * @param mensagem mensagem apresentada antes das opções
     * @param opcoes opções não vazias e sem elementos nulos
     * @return índice baseado em zero de uma opção da lista
     */
    int solicitarOpcao(String mensagem, List<String> opcoes);
}
