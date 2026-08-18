package br.edu.uepb.map.blackjack;

import java.util.LinkedHashMap;
import java.util.Map;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.RegraDePontuacaoStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/** Strategy que concede um ponto ao vencedor e nenhum ponto em empates. */
final class RegraPontuacaoBlackjack
        implements RegraDePontuacaoStrategy<CartaBlackjack> {

    @Override
    public Map<Jogador, Integer> calcular(
            VisaoDaPartida<CartaBlackjack> contexto, DesfechoDePartida desfecho) {
        Map<Jogador, Integer> placar = new LinkedHashMap<>();
        for (Jogador participante : contexto.jogadores()) {
            boolean venceu = !desfecho.motivo().ehEmpate()
                    && desfecho.vencedores().stream()
                    .anyMatch(vencedor -> vencedor.id().equals(participante.id()));
            placar.put(participante, venceu ? 1 : 0);
        }
        return Map.copyOf(placar);
    }
}
