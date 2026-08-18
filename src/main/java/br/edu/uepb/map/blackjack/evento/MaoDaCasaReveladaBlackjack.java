package br.edu.uepb.map.blackjack.evento;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.blackjack.CartaBlackjack;
import br.edu.uepb.map.blackjack.PontuacaoDaMaoBlackjack;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;

/**
 * Evento público emitido quando termina a fase de informação oculta da casa.
 *
 * @param numeroDoTurno primeiro turno da casa
 * @param jogador participante adversário da casa
 * @param maoDoJogador mão completa do jogador
 * @param pontosDoJogador pontuação do jogador
 * @param casa participante automatizado
 * @param maoDaCasa mão agora revelada da casa
 * @param pontosDaCasa pontuação da casa
 */
public record MaoDaCasaReveladaBlackjack(
        long numeroDoTurno,
        Jogador jogador,
        List<CartaBlackjack> maoDoJogador,
        PontuacaoDaMaoBlackjack pontosDoJogador,
        Jogador casa,
        List<CartaBlackjack> maoDaCasa,
        PontuacaoDaMaoBlackjack pontosDaCasa
) implements EventoDePartida {

    /**
     * Valida os participantes, as mãos e suas pontuações.
     *
     * @throws NullPointerException se algum componente for nulo
     * @throws IllegalArgumentException se o turno for inválido, os participantes
     *         coincidirem ou uma pontuação divergir de sua mão
     */
    public MaoDaCasaReveladaBlackjack {
        if (numeroDoTurno < 1) {
            throw new IllegalArgumentException("O número do turno deve ser positivo.");
        }
        Objects.requireNonNull(jogador, "O jogador não pode ser nulo.");
        Objects.requireNonNull(jogador.id(), "O ID do jogador não pode ser nulo.");
        Objects.requireNonNull(casa, "A casa não pode ser nula.");
        Objects.requireNonNull(casa.id(), "O ID da casa não pode ser nulo.");
        if (jogador.id().equals(casa.id())) {
            throw new IllegalArgumentException("Jogador e casa devem ser distintos.");
        }
        maoDoJogador = List.copyOf(Objects.requireNonNull(
                maoDoJogador, "A mão do jogador não pode ser nula."));
        maoDaCasa = List.copyOf(Objects.requireNonNull(
                maoDaCasa, "A mão da casa não pode ser nula."));
        Objects.requireNonNull(pontosDoJogador, "Os pontos do jogador não podem ser nulos.");
        Objects.requireNonNull(pontosDaCasa, "Os pontos da casa não podem ser nulos.");
        if (!pontosDoJogador.equals(PontuacaoDaMaoBlackjack.calcular(maoDoJogador))) {
            throw new IllegalArgumentException(
                    "A pontuação do jogador deve corresponder à sua mão.");
        }
        if (!pontosDaCasa.equals(PontuacaoDaMaoBlackjack.calcular(maoDaCasa))) {
            throw new IllegalArgumentException(
                    "A pontuação da casa deve corresponder à sua mão.");
        }
    }
}
