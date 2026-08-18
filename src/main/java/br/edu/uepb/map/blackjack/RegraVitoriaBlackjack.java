package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.uepb.map.cardgame.api.DesfechoDePartida;
import br.edu.uepb.map.cardgame.api.Jogador;
import br.edu.uepb.map.cardgame.api.RegraDeVitoriaStrategy;
import br.edu.uepb.map.cardgame.api.VisaoDaPartida;

/** Strategy que reconhece naturais, estouros, maior pontuação e empate. */
final class RegraVitoriaBlackjack implements RegraDeVitoriaStrategy<CartaBlackjack> {

    private final MesaBlackjack mesa;

    RegraVitoriaBlackjack(MesaBlackjack mesa) {
        this.mesa = Objects.requireNonNull(mesa, "A mesa não pode ser nula.");
    }

    @Override
    public Optional<DesfechoDePartida> avaliar(VisaoDaPartida<CartaBlackjack> contexto) {
        Objects.requireNonNull(contexto, "O contexto não pode ser nulo.");
        Jogador jogador = mesa.jogador();
        Jogador casa = mesa.casa();
        PontuacaoDaMaoBlackjack pontosDoJogador =
                PontuacaoDaMaoBlackjack.calcular(contexto.maoDe(jogador));
        PontuacaoDaMaoBlackjack pontosDaCasa =
                PontuacaoDaMaoBlackjack.calcular(contexto.maoDe(casa));

        if (contexto.numeroDoTurno() == 0) {
            return avaliarBlackjacksNaturais(
                    jogador, casa, pontosDoJogador, pontosDaCasa);
        }
        if (pontosDoJogador.estourou()) {
            return vitoria(casa, MotivoBlackjack.JOGADOR_ESTOUROU);
        }
        if (pontosDaCasa.estourou()) {
            return vitoria(jogador, MotivoBlackjack.CASA_ESTOUROU);
        }
        if (!mesa.todosPararam()) {
            return Optional.empty();
        }
        if (pontosDoJogador.total() > pontosDaCasa.total()) {
            return vitoria(jogador, MotivoBlackjack.MAIOR_PONTUACAO);
        }
        if (pontosDaCasa.total() > pontosDoJogador.total()) {
            return vitoria(casa, MotivoBlackjack.MAIOR_PONTUACAO);
        }
        return Optional.of(new DesfechoDePartida(
                List.of(jogador, casa), MotivoBlackjack.PONTUACOES_IGUAIS));
    }

    private static Optional<DesfechoDePartida> avaliarBlackjacksNaturais(
            Jogador jogador,
            Jogador casa,
            PontuacaoDaMaoBlackjack pontosDoJogador,
            PontuacaoDaMaoBlackjack pontosDaCasa) {
        if (pontosDoJogador.blackjackNatural() && pontosDaCasa.blackjackNatural()) {
            return Optional.of(new DesfechoDePartida(
                    List.of(jogador, casa), MotivoBlackjack.BLACKJACKS_IGUAIS));
        }
        if (pontosDoJogador.blackjackNatural()) {
            return vitoria(jogador, MotivoBlackjack.BLACKJACK_DO_JOGADOR);
        }
        if (pontosDaCasa.blackjackNatural()) {
            return vitoria(casa, MotivoBlackjack.BLACKJACK_DA_CASA);
        }
        return Optional.empty();
    }

    private static Optional<DesfechoDePartida> vitoria(
            Jogador vencedor, MotivoBlackjack motivo) {
        return Optional.of(new DesfechoDePartida(List.of(vencedor), motivo));
    }
}
