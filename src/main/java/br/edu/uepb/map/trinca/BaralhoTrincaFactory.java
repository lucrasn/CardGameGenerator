package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.List;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.BaralhoFactory;
import br.edu.uepb.map.cardgame.api.BaralhoPadrao;

/** Cria os dois baralhos franceses (104 cartas), sem curingas. */
public final class BaralhoTrincaFactory implements BaralhoFactory<CartaTrinca> {

    @Override
    public Baralho<CartaTrinca> criar() {
        List<CartaTrinca> cartas = new ArrayList<>(104);
        for (int copia = 0; copia < 2; copia++) {
            for (Naipe naipe : Naipe.values()) {
                for (Valor valor : Valor.values()) {
                    cartas.add(new CartaTrinca(valor, naipe));
                }
            }
        }
        return new BaralhoPadrao<>(cartas);
    }
}
