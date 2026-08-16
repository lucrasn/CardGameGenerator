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
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
public interface VisaoDaPartida<C extends Carta> {

    /**
     * Informa a etapa atual do ciclo de vida.
     *
     * @return estado atual do ciclo de vida
     */
    EstadoPartida estado();

    /**
     * Consulta os participantes na ordem configurada para os turnos.
     *
     * @return snapshot imutável dos participantes
     */
    List<Jogador> jogadores();

    /**
     * Consulta o participante de quem é a vez.
     *
     * @return jogador atual, nunca {@code null}
     */
    Jogador jogadorAtual();

    /**
     * Consulta a mão principal de um participante sem expor a mão mutável.
     *
     * @param jogador participante da partida
     * @return snapshot imutável das cartas da mão
     * @throws NullPointerException se o jogador ou sua identidade forem nulos
     * @throws IllegalArgumentException se o jogador não pertencer à partida
     */
    List<C> maoDe(Jogador jogador);

    /**
     * Consulta o tamanho atual do baralho compartilhado.
     *
     * @return número de cartas restantes no baralho
     */
    int quantidadeNoBaralho();

    /**
     * Consulta o número lógico do turno.
     *
     * @return número do turno corrente, começando em um; zero enquanto nenhum
     *         turno tiver sido iniciado
     */
    long numeroDoTurno();
}
