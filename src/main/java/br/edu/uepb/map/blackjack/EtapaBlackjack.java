package br.edu.uepb.map.blackjack;

import br.edu.uepb.map.cardgame.api.EtapaDeTurno;

/** Fase única em que um participante decide pedir uma carta ou parar. */
public enum EtapaBlackjack implements EtapaDeTurno {
    /** Momento de escolher entre pedir e parar. */
    DECISAO
}
