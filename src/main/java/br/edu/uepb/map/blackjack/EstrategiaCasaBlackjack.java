package br.edu.uepb.map.blackjack;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EstrategiaDeDecisao;
import br.edu.uepb.map.cardgame.api.Jogada;

/** Strategy automática da casa: pede abaixo de 17 e para a partir de 17. */
public final class EstrategiaCasaBlackjack implements EstrategiaDeDecisao {

    /** Limite fixo a partir do qual a casa deve parar. */
    public static final int LIMITE_PARA_PARAR = 17;

    /**
     * Escolhe deterministicamente a ação prescrita para a casa.
     *
     * @param contextoBase contexto especializado do Blackjack
     * @return {@link AcaoBlackjack#PEDIR} abaixo de 17 quando disponível;
     *         {@link AcaoBlackjack#PARAR} nos demais casos
     * @throws NullPointerException se o contexto for nulo
     * @throws IllegalArgumentException se o contexto não for do Blackjack ou não
     *         representar a casa
     * @throws IllegalStateException se a ação necessária não estiver disponível
     */
    @Override
    public Jogada decidir(ContextoDeDecisao contextoBase) {
        Objects.requireNonNull(contextoBase, "O contexto não pode ser nulo.");
        if (!(contextoBase instanceof ContextoDecisaoBlackjack contexto)) {
            throw new IllegalArgumentException("A casa exige um contexto do Blackjack.");
        }
        if (contexto.papel() != PapelBlackjack.CASA) {
            throw new IllegalArgumentException("Esta Strategy só pode representar a casa.");
        }
        AcaoBlackjack desejada = contexto.pontuacao().total() < LIMITE_PARA_PARAR
                && contexto.jogadasPermitidas().contains(AcaoBlackjack.PEDIR)
                ? AcaoBlackjack.PEDIR
                : AcaoBlackjack.PARAR;
        if (!contexto.jogadasPermitidas().contains(desejada)) {
            throw new IllegalStateException("A ação obrigatória da casa não está disponível.");
        }
        return desejada;
    }
}
