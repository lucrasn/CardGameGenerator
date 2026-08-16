package br.edu.uepb.map.cardgame.api;

/**
 * Etapa tipada de um turno de jogo.
 *
 * <p><strong>Hot-spot.</strong> A interface é intencionalmente aberta para diferentes
 * tipos de clientes definirem suas próprias fases sem alterar o framework.
 *
 * <p><strong>Fronteira de responsabilidade.</strong> Uma etapa descreve a fase
 * interna do turno de um jogador; ela não escolhe o jogador atual nem controla a
 * rotação, responsabilidades do gerenciador de turnos interno.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public interface EtapaDeTurno {
}
