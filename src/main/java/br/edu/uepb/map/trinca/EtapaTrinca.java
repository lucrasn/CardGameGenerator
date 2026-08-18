package br.edu.uepb.map.trinca;

import br.edu.uepb.map.cardgame.api.EtapaDeTurno;

/**
 * Fases de decisão de um turno da Trinca.
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public enum EtapaTrinca implements EtapaDeTurno {
    /** Etapa em que o jogador escolhe a origem da carta comprada. */
    COMPRA,

    /** Etapa em que o jogador escolhe uma carta da mão para descartar. */
    DESCARTE
}
