package br.edu.uepb.map.cardgame.api;

import java.util.List;

/**
 * Visão somente leitura do estado genérico de uma partida em curso.
 *
 * <p>Separar esta interface de {@link ContextoDePartida} aplica o princípio da
 * segregação de interfaces: componentes que apenas avaliam a partida não recebem
 * operações capazes de alterá-la. As coleções devolvidas são snapshots imutáveis.
 *
 * @param <C> tipo de carta usado pela partida
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public interface VisaoDaPartida<C extends Carta> {

    /** @return estado atual do ciclo de vida */
    EstadoPartida estado();

    /** @return participantes em lista imutável e na ordem de turnos */
    List<Jogador> jogadores();

    /** @return participante de quem é a vez */
    Jogador jogadorAtual();

    /**
     * Consulta a mão principal de um participante sem expor a mão mutável.
     *
     * @param jogador participante da partida
     * @return snapshot imutável das cartas da mão
     */
    List<C> maoDe(Jogador jogador);

    /** @return número de cartas restantes no baralho compartilhado */
    int quantidadeNoBaralho();

    /** @return número do turno corrente, começando em um; zero durante a preparação */
    long numeroDoTurno();
}
