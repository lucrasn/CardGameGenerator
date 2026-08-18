package br.edu.uepb.map.trinca;

import br.edu.uepb.map.cardgame.api.Jogada;

/**
 * Ação sem estado que representa a compra da carta do topo do monte.
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public enum ComprarDoMonte implements Jogada {
    /** Instância única da ação de compra no monte. */
    INSTANCIA;

    /**
     * Retorna o nome da ação.
     *
     * @return descrição da compra no monte
     */
    @Override
    public String toString() {
        return "Comprar do monte";
    }
}
