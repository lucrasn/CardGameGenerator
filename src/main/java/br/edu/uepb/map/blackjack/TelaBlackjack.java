package br.edu.uepb.map.blackjack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import br.edu.uepb.map.cardgame.api.EntradaSaida;

/** Formatação e operações ANSI compartilhadas pelas telas do Blackjack. */
final class TelaBlackjack {

    static final String APAGAR_TELA_E_HISTORICO = "\u001B[H\u001B[2J\u001B[3J";
    static final String SEPARADOR = "═".repeat(62);

    private TelaBlackjack() {
    }

    static void apagar(EntradaSaida entradaSaida) {
        Objects.requireNonNull(
                entradaSaida, "A entrada e saída não pode ser nula.")
                .exibir(APAGAR_TELA_E_HISTORICO);
    }

    static String formatarCarta(CartaBlackjack carta) {
        return formatarCarta(carta, false);
    }

    static String formatarCarta(CartaBlackjack carta, boolean destacar) {
        Objects.requireNonNull(carta, "A carta não pode ser nula.");
        String texto = "[" + carta.valor().simbolo() + carta.naipe().simbolo() + "]";
        if (destacar) {
            return CorTerminalBlackjack.AMARELO.aplicarComDestaque(texto);
        }
        return carta.naipe().vermelho()
                ? CorTerminalBlackjack.VERMELHO.aplicarComDestaque(texto)
                : CorTerminalBlackjack.BRANCO.aplicarComDestaque(texto);
    }

    static String formatarMao(
            List<CartaBlackjack> mao, Optional<CartaBlackjack> cartaDestacada) {
        List<CartaBlackjack> cartas = List.copyOf(Objects.requireNonNull(
                mao, "A mão não pode ser nula."));
        Optional<CartaBlackjack> destaque = Objects.requireNonNull(
                cartaDestacada, "A carta destacada não pode ser nula.");
        if (cartas.isEmpty()) {
            return "(vazia)";
        }
        return cartas.stream()
                .map(carta -> formatarCarta(
                        carta,
                        destaque.filter(atual -> atual.id().equals(carta.id())).isPresent()))
                .collect(Collectors.joining(" "));
    }

    static String formatarPontuacao(PontuacaoDaMaoBlackjack pontuacao) {
        Objects.requireNonNull(pontuacao, "A pontuação não pode ser nula.");
        if (pontuacao.blackjackNatural()) {
            return pontuacao.total() + " — BLACKJACK!";
        }
        if (pontuacao.estourou()) {
            return pontuacao.total() + " — ESTOUROU";
        }
        return pontuacao.total() + (pontuacao.suave() ? " (mão suave)" : "");
    }

    static String montarMesaDoJogador(
            ContextoDecisaoBlackjack contexto, CorTerminalBlackjack corDoJogador) {
        Objects.requireNonNull(contexto, "O contexto não pode ser nulo.");
        Objects.requireNonNull(corDoJogador, "A cor não pode ser nula.");
        StringBuilder tela = new StringBuilder();
        tela.append(CorTerminalBlackjack.DOURADO.aplicarComDestaque("CASA"))
                .append("  ")
                .append(formatarMao(contexto.cartasVisiveisDoOponente(), Optional.empty()));
        for (int indice = 0; indice < contexto.cartasOcultasDoOponente(); indice++) {
            tela.append(' ').append(CorTerminalBlackjack.CINZA.aplicarComDestaque("[??]"));
        }
        tela.append('\n').append(CorTerminalBlackjack.CINZA.aplicar(
                "      Uma carta permanece fechada até a vez da casa."));
        tela.append("\n\n").append(corDoJogador.aplicarComDestaque("SUA MÃO"))
                .append("  ")
                .append(formatarMao(contexto.mao(), contexto.ultimaCartaComprada()));
        tela.append("\nTotal: ").append(corDoJogador.aplicarComDestaque(
                formatarPontuacao(contexto.pontuacao())));
        contexto.ultimaCartaComprada().ifPresent(carta -> tela
                .append('\n')
                .append(CorTerminalBlackjack.AMARELO.aplicarComDestaque(
                        "➜ Carta recebida na última jogada: " + formatarCarta(carta))));
        tela.append("\nCartas restantes no monte: ").append(contexto.cartasNoBaralho());
        return tela.toString();
    }
}
