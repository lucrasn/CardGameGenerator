package br.edu.uepb.map.trinca;

import java.util.ArrayList;
import java.util.List;

import br.edu.uepb.map.cardgame.api.Baralho;
import br.edu.uepb.map.cardgame.api.BaralhoFactory;
import br.edu.uepb.map.cardgame.api.BaralhoPadrao;

/**
 * Fábrica do baralho utilizado pela aplicação Trinca.
 *
 * <p>Cada criação produz as 52 combinações entre os valores e os naipes
 * franceses, sem curingas.</p>
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public final class BaralhoTrincaFactory implements BaralhoFactory<CartaTrinca> {

    /**
     * Cria um novo baralho completo para uma partida de Trinca.
     *
     * @return baralho com 52 cartas distintas
     */
    @Override
    public Baralho<CartaTrinca> criar() {
        List<CartaTrinca> cartas = new ArrayList<>(52);
        for (Naipe naipe : Naipe.values()) {
            for (Valor valor : Valor.values()) {
                cartas.add(new CartaTrinca(valor, naipe));
            }
        }
        return new BaralhoPadrao<>(cartas);
    }
}
