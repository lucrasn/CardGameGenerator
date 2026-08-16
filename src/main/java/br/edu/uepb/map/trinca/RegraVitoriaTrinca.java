package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.RegraDeVitoriaStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/** Reconhece a vitória de quem completar a mão ao final do turno. */
public final class RegraVitoriaTrinca implements RegraDeVitoriaStrategy<CartaTrinca> {

    @Override
    public Optional<DesfechoDePartida> avaliar(VisaoDaPartida<CartaTrinca> contexto) {
        if (contexto.numeroDoTurno() > 0
                && CombinacoesTrinca.ehMaoVencedora(contexto.maoDe(contexto.jogadorAtual()))) {
            return Optional.of(new DesfechoDePartida(
                    List.of(contexto.jogadorAtual()), MotivoPadrao.VITORIA));
        }
        return Optional.empty();
    }
}
