package br.edu.uepb.map.trinca;

/**
 * Naipes de um baralho francês e suas representações visuais.
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public enum Naipe {
    /** Naipe de copas. */
    COPAS("♥", "Copas"),
    /** Naipe de ouros. */
    OUROS("♦", "Ouros"),
    /** Naipe de paus. */
    PAUS("♣", "Paus"),
    /** Naipe de espadas. */
    ESPADAS("♠", "Espadas");

    private final String simbolo;
    private final String descricao;

    Naipe(String simbolo, String descricao) {
        this.simbolo = simbolo;
        this.descricao = descricao;
    }

    /**
     * Retorna o símbolo Unicode do naipe.
     *
     * @return símbolo do naipe
     */
    public String simbolo() {
        return simbolo;
    }

    /**
     * Retorna o nome do naipe em português.
     *
     * @return descrição textual do naipe
     */
    public String descricao() {
        return descricao;
    }
}
