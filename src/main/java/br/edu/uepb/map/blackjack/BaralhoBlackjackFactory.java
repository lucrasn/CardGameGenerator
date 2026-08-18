package br.edu.uepb.map.blackjack;

import java.util.ArrayList;
import java.util.List;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.BaralhoFactory;
import br.edu.uepb.map.cardgame.api.BaralhoPadrao;

/** Factory Method que cria um baralho francês completo, sem curingas. */
public final class BaralhoBlackjackFactory implements BaralhoFactory<CartaBlackjack> {

    /**
     * Cria as 52 combinações distintas de valor e naipe.
     *
     * @return baralho novo, ordenado antes do embaralhamento feito pelo engine
     */
    @Override
    public Baralho<CartaBlackjack> criar() {
        List<CartaBlackjack> cartas = new ArrayList<>(52);
        for (NaipeBlackjack naipe : NaipeBlackjack.values()) {
            for (ValorBlackjack valor : ValorBlackjack.values()) {
                cartas.add(new CartaBlackjack(valor, naipe));
            }
        }
        return new BaralhoPadrao<>(cartas);
    }
}
