package br.edu.uepb.map.cardgame.api.excecao;

/**
 * Indica que uma ação foi rejeitada pelas regras do jogo.
 *
 * <p>A partida deve permanecer em um estado válido após a rejeição, permitindo
 * que a aplicação cliente informe o problema e solicite outra jogada.
 *
 * @author Lívia
 * @since 1.0
 */
public final class JogadaInvalidaException extends PartidaException {

    /** Cria a exceção com a mensagem padrão. */
    public JogadaInvalidaException() {
        super("A jogada informada é inválida.");
    }

    /**
     * Cria a exceção com uma mensagem que explica a regra violada.
     *
     * @param mensagem descrição do motivo pelo qual a jogada foi rejeitada
     */
    public JogadaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
