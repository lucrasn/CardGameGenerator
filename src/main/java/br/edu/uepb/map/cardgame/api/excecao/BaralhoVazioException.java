package br.edu.uepb.map.cardgame.api.excecao;

/**
 * Indica que uma compra foi solicitada quando o baralho não possuía cartas.
 *
 * <p>A exceção apenas comunica a ausência de cartas. Cabe ao jogo cliente decidir
 * se deve reconstruir o baralho, encerrar a partida ou adotar outra regra.
 *
 * @author Lívia
 * @since 1.0
 */
public final class BaralhoVazioException extends PartidaException {


    /** Cria a exceção com a mensagem padrão. */
    public BaralhoVazioException() {
        super("Não há cartas disponíveis no baralho.");
    }

    /**
     * Cria a exceção com uma mensagem específica.
     *
     * @param mensagem descrição da tentativa de compra que falhou
     */
    public BaralhoVazioException(String mensagem) {
        super(mensagem);
    }

}
