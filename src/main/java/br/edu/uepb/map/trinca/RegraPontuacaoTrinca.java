package br.edu.uepb.map.trinca;

import java.util.LinkedHashMap;
import java.util.Map;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.RegraDePontuacaoStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/**
 * Regra de pontuação da Trinca.
 *
 * <p>Cada vencedor recebe um ponto e os demais participantes recebem zero.</p>
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public final class RegraPontuacaoTrinca
        implements RegraDePontuacaoStrategy<CartaTrinca> {

    /**
     * Calcula a pontuação dos participantes para o desfecho informado.
     *
     * @param contexto visão somente de leitura da partida
     * @param desfecho vencedores e motivo de encerramento
     * @return mapa imutável com a pontuação de todos os participantes
     */
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
