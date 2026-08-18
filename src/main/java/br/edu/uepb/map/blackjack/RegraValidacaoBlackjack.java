package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.cardgame.api.ContextoDeValidacao;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.RegraDeValidacaoStrategy;
import br.edu.uepb.map.cardgame.api.excecao.JogadaInvalidaException;

/** Strategy que valida as ações do Blackjack antes de qualquer mutação. */
final class RegraValidacaoBlackjack
        implements RegraDeValidacaoStrategy<CartaBlackjack> {

    private final MesaBlackjack mesa;

    RegraValidacaoBlackjack(MesaBlackjack mesa) {
        this.mesa = Objects.requireNonNull(mesa, "A mesa não pode ser nula.");
    }

    @Override
    public void validar(ContextoDeValidacao<CartaBlackjack> contexto) {
        Objects.requireNonNull(contexto, "O contexto não pode ser nulo.");
        Jogador participante = contexto.partida().jogadorAtual();
        if (mesa.parou(participante)) {
            rejeitar("O participante já encerrou suas decisões.");
        }
        if (!(contexto.jogada() instanceof AcaoBlackjack acao)) {
            rejeitar("A ação não pertence ao Blackjack.");
            return;
        }
        List<CartaBlackjack> mao = contexto.partida().maoDe(participante);
        PontuacaoDaMaoBlackjack pontuacao = PontuacaoDaMaoBlackjack.calcular(mao);
        if (acao == AcaoBlackjack.PEDIR) {
            if (pontuacao.total() >= 21) {
                rejeitar("Não é permitido pedir carta com 21 pontos ou mais.");
            }
            if (contexto.partida().quantidadeNoBaralho() == 0) {
                rejeitar("Não há cartas disponíveis no baralho.");
            }
        }
    }

    private static void rejeitar(String mensagem) {
        throw new JogadaInvalidaException(mensagem);
    }
}
