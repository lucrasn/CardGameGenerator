package br.edu.uepb.map.trinca;

import java.util.List;
import java.util.Objects;

/**
 * Paleta ANSI usada para identificar visualmente os jogadores no console.
 *
 * <p>Cada constante encapsula o código necessário para aplicar uma cor ao
 * texto. A opção {@link #SEM_COR} mantém o conteúdo sem sequências ANSI.</p>
 */
enum CorTerminal {
    /** Mantém o texto sem formatação de cor. */
    SEM_COR(""),

    /** Aplica a cor azul-celeste. */
    AZUL_CELESTE("\u001B[38;5;39m"),

    /** Aplica a cor laranja. */
    LARANJA("\u001B[38;5;208m"),

    /** Aplica a cor verde-lima. */
    VERDE_LIMA("\u001B[38;5;46m"),

    /** Aplica a cor magenta. */
    MAGENTA("\u001B[38;5;201m"),

    /** Aplica a cor amarela. */
    AMARELO("\u001B[38;5;220m"),

    /** Aplica a cor vermelha, usada principalmente em alertas. */
    VERMELHO("\u001B[38;5;196m");

    private static final String RESET = "\u001B[0m";
    private static final String NEGRITO = "\u001B[1m";

    private final String codigo;

    CorTerminal(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Aplica a cor ao texto informado.
     *
     * @param texto conteúdo que será formatado
     * @return texto envolvido pelos códigos ANSI, ou inalterado para
     *         {@link #SEM_COR}
     * @throws NullPointerException se o texto for nulo
     */
    String aplicar(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR ? textoValido : codigo + textoValido + RESET;
    }

    /**
     * Aplica simultaneamente a cor e o estilo de negrito ao texto.
     *
     * @param texto conteúdo que será destacado
     * @return texto destacado, ou inalterado para {@link #SEM_COR}
     * @throws NullPointerException se o texto for nulo
     */
    String aplicarComDestaque(String texto) {
        String textoValido = Objects.requireNonNull(texto, "O texto não pode ser nulo.");
        return this == SEM_COR ? textoValido : NEGRITO + codigo + textoValido + RESET;
    }

    /**
     * Retorna as cores disponíveis para distinguir os participantes.
     *
     * @return lista imutável com cinco cores de jogador
     */
    static List<CorTerminal> coresDeJogador() {
        return List.of(
                AZUL_CELESTE, LARANJA, VERDE_LIMA, MAGENTA, AMARELO);
    }
}
