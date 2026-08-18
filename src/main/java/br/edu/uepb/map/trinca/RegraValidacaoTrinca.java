package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.ContextoDeValidacao;
import br.edu.uepb.map.cardgame.api.Jogada;
import br.edu.uepb.map.cardgame.api.RegraDeValidacaoStrategy;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;

/**
 * Regra que valida as ações tipadas da Trinca antes de cada mutação.
 *
 * <p>A validação considera a quantidade de cartas da mão e a disponibilidade
 * das cartas no monte, no descarte e na mão do jogador atual.</p>
 *
 * @author Raffael Wagner Rolim Siqueira
 * @version 0.0.1
 */
public final class RegraValidacaoTrinca
        implements RegraDeValidacaoStrategy<CartaTrinca> {

    private final MesaTrinca mesa;

    /**
     * Cria a regra vinculada às zonas específicas da partida.
     *
     * @param mesa mesa que mantém a pilha de descarte
     * @throws NullPointerException se a mesa for nula
     */
    RegraValidacaoTrinca(MesaTrinca mesa) {
        this.mesa = Objects.requireNonNull(mesa, "A mesa não pode ser nula.");
    }

    /**
     * Valida uma compra no monte, uma compra no descarte ou um descarte da mão.
     *
     * @param contexto contexto da jogada que será validada
     * @throws JogadaInvalidaException se a ação violar as regras da Trinca
     */
    @Override
    public void validar(ContextoDeValidacao<CartaTrinca> contexto) {
        Jogada jogada = contexto.jogada();
        List<CartaTrinca> mao = contexto.partida().maoDe(contexto.partida().jogadorAtual());

        if (jogada == ComprarDoMonte.INSTANCIA) {
            if (mao.size() != 9) {
                rejeitar("A compra exige uma mão com nove cartas.");
            }
            if (contexto.partida().quantidadeNoBaralho() == 0
                    && mesa.quantidadeNoDescarte() <= 1) {
                rejeitar("Não há cartas disponíveis para comprar ou reciclar.");
            }
            return;
        }

        if (jogada instanceof ComprarDoDescarte compra) {
            if (mao.size() != 9) {
                rejeitar("A compra exige uma mão com nove cartas.");
            }
            boolean ehTopo = mesa.topoDoDescarte()
                    .map(carta -> carta.id().equals(compra.cartaId()))
                    .orElse(false);
            if (!ehTopo) {
                rejeitar("A carta escolhida não é o topo do descarte.");
            }
            return;
        }

        if (jogada instanceof Descartar descarte) {
            if (mao.size() != 10) {
                rejeitar("O descarte exige uma mão com dez cartas.");
            }
            if (mao.stream().noneMatch(carta -> carta.id().equals(descarte.cartaId()))) {
                rejeitar("A carta escolhida não pertence à mão do jogador.");
            }
            return;
        }

        rejeitar("A ação não pertence ao jogo de Trinca.");
    }

    private static void rejeitar(String mensagem) {
        throw new JogadaInvalidaException(mensagem);
    }
}
