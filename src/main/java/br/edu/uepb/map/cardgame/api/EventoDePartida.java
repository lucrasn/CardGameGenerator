package br.edu.uepb.map.cardgame.api;

/**
 * Fato imutável ocorrido durante o ciclo de vida de uma partida.
 *
 * <p>Esta interface é um ponto de extensão do framework: os eventos comuns ficam
 * na API e cada jogo cliente pode declarar eventos próprios sem modificar o motor.
 * As implementações não devem expor coleções mutáveis nem informações privadas de
 * um jogador a observadores que não tenham autorização para recebê-las.
 *
 * <p>O contrato não declara operações porque cada tipo de evento carrega dados
 * diferentes. Observadores recebem a abstração comum por meio de
 * {@link PartidaListener} e verificam o tipo concreto que lhes interessa.
 *
 * @author Lívia
 * @version 0.0.1
 */
public interface EventoDePartida {
}
