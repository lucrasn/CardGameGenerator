package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.RegraDeVitoriaStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/** Reconhece vitória ao final do turno e empate por esgotamento. */
public final class RegraVitoriaTrinca implements RegraDeVitoriaStrategy<CartaTrinca> {

    private final MesaTrinca mesa;

    RegraVitoriaTrinca(MesaTrinca mesa) {
        this.mesa = Objects.requireNonNull(mesa, "A mesa não pode ser nula.");
    }

    @Override
    public Optional<DesfechoDePartida> avaliar(VisaoDaPartida<CartaTrinca> contexto) {
        if (contexto.numeroDoTurno() > 0
                && CombinacoesTrinca.ehMaoVencedora(contexto.maoDe(contexto.jogadorAtual()))) {
            return Optional.of(new DesfechoDePartida(
                    List.of(contexto.jogadorAtual()), MotivoPadrao.VITORIA));
        }
        if (contexto.numeroDoTurno() > 0
                && contexto.quantidadeNoBaralho() == 0
                && mesa.quantidadeNoDescarte() <= 1) {
            return Optional.of(new DesfechoDePartida(List.of(), MotivoPadrao.EMPATE));
        }
        return Optional.empty();
    }
}
