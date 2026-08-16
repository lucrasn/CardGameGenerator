package br.edu.uepb.map.trinca;

import br.edu.uepb.map.cardgame.api.Jogada;

/** Ação de comprar a carta do topo do monte. */
public enum ComprarDoMonte implements Jogada {
    INSTANCIA;

    @Override
    public String toString() {
        return "Comprar do monte";
    }
}
