package br.edu.uepb.map.cardgame.api.excecao;

/**
 * Exceção-base para falhas relacionadas ao domínio de uma partida.
 *
 * <p>As aplicações clientes devem tratar preferencialmente as subclasses
 * específicas. Este tipo existe para permitir o tratamento conjunto de qualquer
 * falha de domínio do framework.
 *
 * @author Lívia
 */
public abstract class PartidaException extends RuntimeException {

    /**
     * Cria uma falha de domínio com uma mensagem explicativa.
     *
     * @param mensagem descrição da falha
     */
    protected PartidaException(String mensagem) {
        super(mensagem);
    }

}
