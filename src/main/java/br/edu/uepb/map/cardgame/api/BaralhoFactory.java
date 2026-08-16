package br.edu.uepb.map.cardgame.api;

/**
 * Cria baralhos com a composição definida por uma aplicação cliente.
 *
 * <p>Este é o ponto de extensão baseado em Factory Method que permite criar, por
 * exemplo, um baralho francês de 52 cartas, dois baralhos combinados ou um
 * baralho de Uno sem acoplar o motor a nenhuma dessas representações.
 *
 * @param <C> tipo de carta produzido pela fábrica
 * @author Júlio
 * @version 0.0.1
 */
@FunctionalInterface
public interface BaralhoFactory<C extends Carta> {

    /**
     * Cria um novo baralho pronto para ser usado por uma partida.
     *
     * <p>Chamadas diferentes devem devolver baralhos independentes. As cartas de
     * cada baralho devem possuir identificadores únicos e estáveis.
     *
     * @return novo baralho, nunca {@code null}
     */
    Baralho<C> criar();
}
