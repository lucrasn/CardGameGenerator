package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.MotivoPadrao;
import br.edu.uepb.map.cardgame.api.RegraDeVitoriaStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/**
 * Regra que reconhece a vitória ao final de um turno da Trinca.
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public final class RegraVitoriaTrinca implements RegraDeVitoriaStrategy<CartaTrinca> {

    /**
     * Avalia se a mão do jogador atual pode ser totalmente dividida em combinações.
     *
     * @param contexto visão somente de leitura da partida
     * @return desfecho de vitória quando a mão estiver completa; vazio caso contrário
     */
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
