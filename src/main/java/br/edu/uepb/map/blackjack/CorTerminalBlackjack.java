package br.edu.uepb.map.blackjack;

import java.util.Objects;

/** Paleta ANSI usada pela aplicação de console do Blackjack. */
enum CorTerminalBlackjack {
    SEM_COR(""),
    CIANO("\u001B[38;5;45m"),
    DOURADO("\u001B[38;5;220m"),
    VERDE("\u001B[38;5;46m"),
    VERMELHO("\u001B[38;5;196m"),
    AMARELO("\u001B[38;5;226m"),
    BRANCO("\u001B[38;5;255m"),
    CINZA("\u001B[38;5;244m");

    private static final String RESET = "\u001B[0m";
    private static final String NEGRITO = "\u001B[1m";

    private final String codigo;

    CorTerminalBlackjack(String codigo) {
        this.codigo = codigo;
    }

    String aplicar(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR ? textoValido : codigo + textoValido + RESET;
    }

    String aplicarComDestaque(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR
                ? textoValido
                : NEGRITO + codigo + textoValido + RESET;
    }
}
