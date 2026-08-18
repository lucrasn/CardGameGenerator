package br.edu.uepb.map.blackjack.evento;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.blackjack.CartaBlackjack;
import br.edu.uepb.map.blackjack.PapelBlackjack;
import br.edu.uepb.map.blackjack.PontuacaoDaMaoBlackjack;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento publicado depois que uma carta pedida entra na mão do participante.
 *
 * @param numeroDoTurno número lógico do turno
 * @param participante participante que pediu
 * @param papel papel desempenhado na rodada
 * @param carta carta recebida
 * @param maoAtual snapshot da mão após a compra
 * @param pontuacao pontuação da mão após a compra
 */
public record CartaPedidaBlackjack(
        long numeroDoTurno,
        Jogador participante,
        PapelBlackjack papel,
        CartaBlackjack carta,
        List<CartaBlackjack> maoAtual,
        PontuacaoDaMaoBlackjack pontuacao
) implements EventoDePartida {

    /**
     * Valida o evento e protege o snapshot da mão.
     *
     * @throws NullPointerException se algum componente for nulo
     * @throws IllegalArgumentException se o turno for inválido, a carta não estiver
     *         na mão ou a pontuação não corresponder ao snapshot
     */
    public CartaPedidaBlackjack {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(participante, "O participante não pode ser nulo.");
        Objects.requireNonNull(participante.id(), "O ID do participante não pode ser nulo.");
        Objects.requireNonNull(papel, "O papel não pode ser nulo.");
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        maoAtual = List.copyOf(Objects.requireNonNull(
                maoAtual, "A mão atual não pode ser nula."));
        if (maoAtual.stream().noneMatch(atual -> atual.id().equals(carta.id()))) {
            throw new IllegalArgumentException("A carta pedida deve estar na mão atual.");
        }
        Objects.requireNonNull(pontuacao, "A pontuação não pode ser nula.");
        if (!pontuacao.equals(PontuacaoDaMaoBlackjack.calcular(maoAtual))) {
            throw new IllegalArgumentException("A pontuação deve corresponder à mão atual.");
        }
    }
}
