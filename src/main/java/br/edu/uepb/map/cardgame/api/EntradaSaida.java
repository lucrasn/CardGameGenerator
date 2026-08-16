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
 */
public interface EntradaSaida {

    /**
     * Exibe uma mensagem ao usuário.
     *
     * @param mensagem mensagem não nula
     * @throws NullPointerException se a mensagem for nula
     */
    void exibir(String mensagem);

    /**
     * Solicita a escolha de uma opção.
     *
     * @param mensagem mensagem apresentada antes das opções
     * @param opcoes opções não vazias e sem elementos nulos
     * @return índice baseado em zero de uma opção da lista
     * @throws NullPointerException se a mensagem, a lista ou algum elemento for nulo
     * @throws IllegalArgumentException se a lista estiver vazia
     */
    int solicitarOpcao(String mensagem, List<String> opcoes);
}
