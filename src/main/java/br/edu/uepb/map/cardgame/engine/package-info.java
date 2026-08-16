/**
 * Runtime da partida.
 *
 * <p>{@link br.edu.uepb.map.cardgame.engine.MotorDePartida} é o único tipo público
 * deste pacote; os colaboradores que preservam estado e turnos são internos.
 *
 * <p>Manter turnos, ciclo de vida, mãos mutáveis e contexto concreto sem {@code public}
 * é a garantia mecânica de que um jogo cliente não consegue depender deles — o que
 * permite reescrevê-los sem quebrar Trinca nem Blackjack.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
package br.edu.uepb.map.cardgame.engine;
