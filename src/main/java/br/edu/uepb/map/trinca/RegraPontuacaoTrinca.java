package br.edu.uepb.map.trinca;

import java.util.LinkedHashMap;
import java.util.Map;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.RegraDePontuacaoStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/** Concede um ponto a cada vencedor e zero aos demais participantes. */
public final class RegraPontuacaoTrinca
        implements RegraDePontuacaoStrategy<CartaTrinca> {

    @Override
    public Map<Jogador, Integer> calcular(
            VisaoDaPartida<CartaTrinca> contexto, DesfechoDePartida desfecho) {
        Map<Jogador, Integer> placar = new LinkedHashMap<>();
        for (Jogador jogador : contexto.jogadores()) {
            boolean venceu = desfecho.vencedores().stream()
                    .anyMatch(vencedor -> vencedor.id().equals(jogador.id()));
            placar.put(jogador, venceu ? 1 : 0);
        }
        return Map.copyOf(placar);
    }
}
