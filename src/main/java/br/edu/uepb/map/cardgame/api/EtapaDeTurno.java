package br.edu.uepb.map.cardgame.api;

/**
 * Etapa tipada de um turno de jogo.
 *
 * <p>A interface é intencionalmente aberta para diferentes tipos de clientes
 * definirem suas próprias fases sem alterar o framework.
 *
 * <p><strong>Fronteira de responsabilidade.</strong> Uma etapa descreve a fase
 * interna do turno de um jogador; ela não escolhe o jogador atual nem controla a
 * rotação, responsabilidades do gerenciador de turnos interno.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public interface EtapaDeTurno {
}
