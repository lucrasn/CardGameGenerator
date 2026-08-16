package br.edu.uepb.map.cardgame.api.excecao;

/**
 * Indica que uma operação foi solicitada em uma fase incompatível da partida.
 *
 * <p>Essa exceção representa uma violação do ciclo de vida da partida, como tentar
 * realizar uma jogada antes do início dos turnos ou depois do encerramento.
 *
 * @author Lívia
 */
public final class EstadoDePartidaInvalidoException extends PartidaException {

    /** Cria a exceção com a mensagem padrão. */
    public EstadoDePartidaInvalidoException() {
        super("O estado atual da partida não permite esta operação.");
    }

    /**
     * Cria a exceção com uma mensagem que explica a operação incompatível.
     *
     * @param mensagem descrição do motivo pelo qual o estado é inválido
     */
    public EstadoDePartidaInvalidoException(String mensagem) {
        super(mensagem);
    }
}
