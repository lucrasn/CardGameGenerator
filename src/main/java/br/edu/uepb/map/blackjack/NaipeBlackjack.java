package br.edu.uepb.map.blackjack;

/** Naipes do baralho francês usado pelo Blackjack. */
public enum NaipeBlackjack {

    /** Naipe vermelho de copas. */
    COPAS("♥", "Copas", true),

    /** Naipe vermelho de ouros. */
    OUROS("♦", "Ouros", true),

    /** Naipe preto de paus. */
    PAUS("♣", "Paus", false),

    /** Naipe preto de espadas. */
    ESPADAS("♠", "Espadas", false);

    private final String simbolo;
    private final String descricao;
    private final boolean vermelho;

    NaipeBlackjack(String simbolo, String descricao, boolean vermelho) {
        this.simbolo = simbolo;
        this.descricao = descricao;
        this.vermelho = vermelho;
    }

    /**
     * Devolve o símbolo tradicional do naipe.
     *
     * @return símbolo Unicode do naipe
     */
    public String simbolo() {
        return simbolo;
    }

    /**
     * Devolve o nome do naipe em português.
     *
     * @return descrição do naipe
     */
    public String descricao() {
        return descricao;
    }

    /**
     * Indica se o naipe deve ser apresentado em vermelho.
     *
     * @return {@code true} para copas e ouros
     */
    public boolean vermelho() {
        return vermelho;
    }
}
