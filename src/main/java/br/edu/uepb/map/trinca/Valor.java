package br.edu.uepb.map.trinca;

/** Valores das cartas francesas, em ordem crescente. */
public enum Valor {
    AS(1, "A"), DOIS(2, "2"), TRES(3, "3"), QUATRO(4, "4"),
    CINCO(5, "5"), SEIS(6, "6"), SETE(7, "7"), OITO(8, "8"),
    NOVE(9, "9"), DEZ(10, "10"), VALETE(11, "J"), DAMA(12, "Q"), REI(13, "K");

    private final int ordem;
    private final String simbolo;

    Valor(int ordem, String simbolo) {
        this.ordem = ordem;
        this.simbolo = simbolo;
    }

    public int ordem() {
        return ordem;
    }

    public String simbolo() {
        return simbolo;
    }
}
