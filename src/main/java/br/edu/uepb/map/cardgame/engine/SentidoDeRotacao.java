package br.edu.uepb.map.cardgame.engine;

/**
 * Direção em que a vez circula pela mesa.
 *
 * <p>Modelar o sentido como um passo numérico ({@code +1} ou {@code -1}) permite que o
 * avanço de turno seja uma única expressão de aritmética modular, sem nenhum desvio
 * condicional sobre a direção. Um {@code boolean} exigiria um {@code if} em cada
 * avanço.
 *
 * <p>Detalhe interno do engine: os jogos clientes influenciam a rotação devolvendo
 * {@code ResultadoDoTurno.inverter()}, nunca manipulando este tipo.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
enum SentidoDeRotacao {

    /** Avança para o próximo participante da lista. */
    HORARIO(1),

    /** Avança para o participante anterior da lista. */
    ANTI_HORARIO(-1);

    private final int passo;

    SentidoDeRotacao(int passo) {
        this.passo = passo;
    }

    /**
     * Deslocamento aplicado ao índice a cada avanço.
     *
     * @return {@code 1} no sentido horário, {@code -1} no anti-horário
     */
    int passo() {
        return passo;
    }

    /**
     * Sentido oposto a este.
     *
     * @return o outro sentido; aplicar duas vezes devolve o original
     */
    SentidoDeRotacao inverso() {
        return this == HORARIO ? ANTI_HORARIO : HORARIO;
    }
}
