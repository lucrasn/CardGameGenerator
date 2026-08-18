package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;

import br.edu.uepb.map.blackjack.evento.CartaPedidaBlackjack;
import br.edu.uepb.map.blackjack.evento.MaoDaCasaReveladaBlackjack;
import br.edu.uepb.map.blackjack.evento.ParticipanteParouBlackjack;
import br.edu.uepb.map.cardgame.api.EntradaSaida;
import br.edu.uepb.map.cardgame.api.EventoDePartida;
import br.edu.uepb.map.cardgame.api.PartidaListener;
import br.edu.uepb.map.cardgame.api.evento.JogadaRejeitada;

/** Observer que transforma eventos do Blackjack em uma apresentação de console. */
final class ApresentadorBlackjackConsole implements PartidaListener {

    private final EntradaSaida entradaSaida;

    ApresentadorBlackjackConsole(EntradaSaida entradaSaida) {
        this.entradaSaida = Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.");
    }

    @Override
    public void aoOcorrer(EventoDePartida evento) {
        Objects.requireNonNull(evento, "O evento não pode ser nulo.");
        if (evento instanceof MaoDaCasaReveladaBlackjack revelacao) {
            apresentarRevelacao(revelacao);
        } else if (evento instanceof CartaPedidaBlackjack compra
                && compra.papel() == PapelBlackjack.CASA) {
            apresentarCompraDaCasa(compra);
        } else if (evento instanceof ParticipanteParouBlackjack parada
                && parada.papel() == PapelBlackjack.CASA) {
            entradaSaida.exibir(CorTerminalBlackjack.DOURADO.aplicarComDestaque(
                    "\nA casa parou com "
                            + TelaBlackjack.formatarPontuacao(parada.pontuacao()) + "."));
        } else if (evento instanceof JogadaRejeitada rejeitada) {
            entradaSaida.exibir(CorTerminalBlackjack.VERMELHO.aplicarComDestaque(
                    "Jogada rejeitada: " + rejeitada.motivo()));
        }
    }

    private void apresentarRevelacao(MaoDaCasaReveladaBlackjack evento) {
        TelaBlackjack.apagar(entradaSaida);
        entradaSaida.exibir(CorTerminalBlackjack.DOURADO.aplicarComDestaque(
                TelaBlackjack.SEPARADOR
                        + "\n  VEZ DA CASA — CARTA FECHADA REVELADA"
                        + "\n" + TelaBlackjack.SEPARADOR));
        entradaSaida.exibir("\n" + CorTerminalBlackjack.CIANO.aplicarComDestaque("VOCÊ")
                + "  " + TelaBlackjack.formatarMao(evento.maoDoJogador(), java.util.Optional.empty())
                + "\nTotal: " + TelaBlackjack.formatarPontuacao(evento.pontosDoJogador()));
        entradaSaida.exibir("\n" + CorTerminalBlackjack.DOURADO.aplicarComDestaque("CASA")
                + "  " + TelaBlackjack.formatarMao(evento.maoDaCasa(), java.util.Optional.empty())
                + "\nTotal: " + TelaBlackjack.formatarPontuacao(evento.pontosDaCasa()));
        solicitarContinuacao("A casa joga automaticamente: pede abaixo de 17 e para em 17 ou mais.");
    }

    private void apresentarCompraDaCasa(CartaPedidaBlackjack evento) {
        entradaSaida.exibir(CorTerminalBlackjack.DOURADO.aplicarComDestaque(
                "\nA casa pediu e recebeu " + TelaBlackjack.formatarCarta(evento.carta(), true)
                        + "."));
        entradaSaida.exibir("Mão da casa: "
                + TelaBlackjack.formatarMao(
                        evento.maoAtual(), java.util.Optional.of(evento.carta()))
                + "\nTotal: " + TelaBlackjack.formatarPontuacao(evento.pontuacao()));
        solicitarContinuacao(evento.pontuacao().estourou()
                ? "A casa ultrapassou 21."
                : "Acompanhe a próxima decisão da casa.");
    }

    private void solicitarContinuacao(String mensagem) {
        int indice = entradaSaida.solicitarOpcao(mensagem, List.of("Continuar"));
        if (indice != 0) {
            throw new IllegalStateException(
                    "A entrada e saída retornou uma opção inválida.");
        }
    }
}
