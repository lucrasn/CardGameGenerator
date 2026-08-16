package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;

/** Paleta ANSI usada para identificar visualmente os jogadores no console. */
enum CorTerminal {
    SEM_COR(""),
    AZUL_CELESTE("\u001B[38;5;39m"),
    LARANJA("\u001B[38;5;208m"),
    VERDE_LIMA("\u001B[38;5;46m"),
    MAGENTA("\u001B[38;5;201m"),
    AMARELO("\u001B[38;5;220m"),
    VERMELHO("\u001B[38;5;196m");

    private static final String RESET = "\u001B[0m";
    private static final String NEGRITO = "\u001B[1m";

    private final String codigo;

    CorTerminal(String codigo) {
        this.codigo = codigo;
    }

    String aplicar(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR ? textoValido : codigo + textoValido + RESET;
    }

    String aplicarComDestaque(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR ? textoValido : NEGRITO + codigo + textoValido + RESET;
    }

    static List<CorTerminal> coresDeJogador() {
        return List.of(
                AZUL_CELESTE, LARANJA, VERDE_LIMA, MAGENTA, AMARELO);
    }
}
