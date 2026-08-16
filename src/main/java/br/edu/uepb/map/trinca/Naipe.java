package br.edu.uepb.map.trinca;

/** Naipes de um baralho francês. */
public enum Naipe {
    COPAS("♥", "Copas"),
    OUROS("♦", "Ouros"),
    PAUS("♣", "Paus"),
    ESPADAS("♠", "Espadas");

    private final String simbolo;
    private final String descricao;

    Naipe(String simbolo, String descricao) {
        this.simbolo = simbolo;
        this.descricao = descricao;
    }

    public String simbolo() {
        return simbolo;
    }

    public String descricao() {
        return descricao;
    }
}
