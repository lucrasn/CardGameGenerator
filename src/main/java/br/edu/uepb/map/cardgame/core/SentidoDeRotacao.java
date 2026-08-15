package br.edu.uepb.map.cardgame.core;

/**
 * Sentido em que a vez circula entre os jogadores.
 *
 * <p>Existe como tipo próprio, e não como {@code int} ou {@code boolean} solto dentro
 * do {@link GerenciadorDeTurnos}, para que o passo aritmético do avanço fique
 * associado ao conceito que ele representa — um campo {@code int sentido = -1}
 * espalhado pelo código não diz a ninguém o que significa.
 *
 * @see GerenciadorDeTurnos#inverterSentido()
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public enum SentidoDeRotacao {

    /** Vez circula na ordem em que os jogadores foram informados. */
    HORARIO(1),

    /** Vez circula na ordem inversa. */
    ANTI_HORARIO(-1);

    private final int passo;

    SentidoDeRotacao(int passo) {
        this.passo = passo;
    }

    /**
     * Deslocamento a somar ao índice do jogador atual para chegar ao próximo.
     *
     * @return {@code 1} no sentido horário, {@code -1} no anti-horário
     */
    public int passo() {
        return passo;
    }

    /**
     * Devolve o sentido oposto a este.
     *
     * @return {@link #ANTI_HORARIO} se este for {@link #HORARIO}, e vice-versa
     */
    public SentidoDeRotacao inverso() {
        return this == HORARIO ? ANTI_HORARIO : HORARIO;
    }
}
