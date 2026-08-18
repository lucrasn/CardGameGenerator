package br.edu.uepb.map.blackjack;

import br.edu.uepb.map.cardgame.api.Jogada;

/** Ações possíveis durante a decisão de um participante no Blackjack. */
public enum AcaoBlackjack implements Jogada {

    /** Compra uma carta e conserva a vez do participante. */
    PEDIR("Pedir uma carta"),

    /** Encerra as compras do participante e passa a vez. */
    PARAR("Parar com esta mão");

    private final String descricao;

    AcaoBlackjack(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Devolve o texto apresentado nas interfaces do cliente.
     *
     * @return descrição da ação
     */
    public String descricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
