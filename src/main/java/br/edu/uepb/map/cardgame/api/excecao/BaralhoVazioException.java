package br.edu.uepb.map.cardgame.api.excecao;

/**
 * Indica que uma operação exigiu mais cartas do que havia disponível.
 *
 * <p>Esta exceção é não verificada porque o estado pode ser consultado antes da
 * operação. A Trilha D poderá integrá-la à hierarquia geral de exceções de domínio
 * sem modificar os contratos da Trilha B.
 *
 * @author Júlio
 * @since 1.0
 */
public class BaralhoVazioException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem descrição da falha
     */
    public BaralhoVazioException(String mensagem) {
        super(mensagem);
    }
}
