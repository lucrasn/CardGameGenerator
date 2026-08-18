package br.edu.uepb.map.blackjack;

/** Valores das cartas francesas e sua contribuição máxima no Blackjack. */
public enum ValorBlackjack {

    /** Ás, inicialmente contado como onze e rebaixado para um quando necessário. */
    AS(11, "A"),
    /** Carta de valor dois. */
    DOIS(2, "2"),
    /** Carta de valor três. */
    TRES(3, "3"),
    /** Carta de valor quatro. */
    QUATRO(4, "4"),
    /** Carta de valor cinco. */
    CINCO(5, "5"),
    /** Carta de valor seis. */
    SEIS(6, "6"),
    /** Carta de valor sete. */
    SETE(7, "7"),
    /** Carta de valor oito. */
    OITO(8, "8"),
    /** Carta de valor nove. */
    NOVE(9, "9"),
    /** Carta de valor dez. */
    DEZ(10, "10"),
    /** Valete, que vale dez pontos. */
    VALETE(10, "J"),
    /** Dama, que vale dez pontos. */
    DAMA(10, "Q"),
    /** Rei, que vale dez pontos. */
    REI(10, "K");

    private final int pontosMaximos;
    private final String simbolo;

    ValorBlackjack(int pontosMaximos, String simbolo) {
        this.pontosMaximos = pontosMaximos;
        this.simbolo = simbolo;
    }

    /**
     * Consulta o valor usado antes do ajuste dos ases.
     *
     * @return valor entre dois e onze, ou dez para figuras
     */
    public int pontosMaximos() {
        return pontosMaximos;
    }

    /**
     * Devolve a representação curta impressa na carta.
     *
     * @return símbolo do valor
     */
    public String simbolo() {
        return simbolo;
    }

    /**
     * Indica se este valor representa um Ás.
     *
     * @return {@code true} somente para {@link #AS}
     */
    public boolean ehAs() {
        return this == AS;
    }
}
