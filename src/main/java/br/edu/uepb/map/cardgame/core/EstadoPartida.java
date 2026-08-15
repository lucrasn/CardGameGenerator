package br.edu.uepb.map.cardgame.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Estados pelos quais uma partida passa, do momento em que é montada até o
 * encerramento, junto com as transições consideradas legais entre eles.
 *
 * <p>O conhecimento sobre <em>qual estado pode suceder qual</em> mora aqui dentro,
 * e não no {@link MotorDePartida}: é a aplicação do princípio GRASP de
 * <strong>Especialista na Informação</strong> a um tipo de valor. O motor guarda
 * apenas o estado corrente e consulta este enum antes de mudar; a regra de
 * transição em si é responsabilidade do próprio estado.
 *
 * <p><strong>Decisão de projeto.</strong> Optamos por um {@code enum} com tabela de
 * transições em vez do padrão GoF <em>State</em>. O padrão State compensa quando cada
 * estado carrega comportamento próprio a ser despachado polimorficamente; aqui os
 * quatro estados apenas <em>restringem</em> transições e não possuem comportamento
 * algum. Aplicá-lo trocaria um enum por quatro classes vazias — engenharia excessiva
 * sem ganho de extensibilidade.
 *
 * <p>Grafo de transições legais:
 * <pre>
 * AGUARDANDO_JOGADORES → DISTRIBUINDO_CARTAS
 * DISTRIBUINDO_CARTAS  → TURNO_EM_ANDAMENTO
 * TURNO_EM_ANDAMENTO   → TURNO_EM_ANDAMENTO | FINALIZADO
 * FINALIZADO           → (terminal)
 * </pre>
 *
 * @see MotorDePartida
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 * @since 2026-06-15
 */
public enum EstadoPartida {

    /** Partida montada, jogadores definidos, nenhuma carta criada ainda. Estado inicial. */
    AGUARDANDO_JOGADORES,

    /** Baralho criado e cartas sendo entregues às mãos dos jogadores. */
    DISTRIBUINDO_CARTAS,

    /** Laço de turnos em execução; permanece neste estado a cada novo turno. */
    TURNO_EM_ANDAMENTO,

    /** Resultado apurado e partida encerrada. Estado terminal: não transita para nenhum outro. */
    FINALIZADO;

    /**
     * Tabela de transições legais.
     *
     * <p>Precisa ser montada em bloco {@code static}, e não no construtor do enum:
     * durante a construção das constantes elas ainda não estão todas inicializadas,
     * e referenciar umas às outras ali dentro resultaria em {@code null}.
     */
    private static final Map<EstadoPartida, Set<EstadoPartida>> TRANSICOES_LEGAIS;

    static {
        Map<EstadoPartida, Set<EstadoPartida>> tabela = new EnumMap<>(EstadoPartida.class);
        tabela.put(AGUARDANDO_JOGADORES, EnumSet.of(DISTRIBUINDO_CARTAS));
        tabela.put(DISTRIBUINDO_CARTAS, EnumSet.of(TURNO_EM_ANDAMENTO));
        tabela.put(TURNO_EM_ANDAMENTO, EnumSet.of(TURNO_EM_ANDAMENTO, FINALIZADO));
        tabela.put(FINALIZADO, EnumSet.noneOf(EstadoPartida.class));

        // Congela cada conjunto individualmente antes de congelar a tabela: sem isso
        // um cliente conseguiria alterar as regras de transição por fora (requisito 7).
        tabela.replaceAll((estado, destinos) -> Collections.unmodifiableSet(destinos));
        TRANSICOES_LEGAIS = Collections.unmodifiableMap(tabela);
    }

    /**
     * Indica se a partida pode migrar deste estado para o estado informado.
     *
     * @param destino estado de destino pretendido; não pode ser {@code null}
     * @return {@code true} se a transição é legal, {@code false} caso contrário
     * @throws NullPointerException se {@code destino} for {@code null}
     */
    public boolean podeTransitarPara(EstadoPartida destino) {
        Objects.requireNonNull(destino, "O estado de destino não pode ser nulo.");
        return TRANSICOES_LEGAIS.get(this).contains(destino);
    }

    /**
     * Indica se este é um estado terminal, do qual a partida não sai mais.
     *
     * @return {@code true} se nenhuma transição parte deste estado
     */
    public boolean ehTerminal() {
        return TRANSICOES_LEGAIS.get(this).isEmpty();
    }

    /**
     * Devolve os estados alcançáveis a partir deste, em coleção imutável.
     *
     * @return conjunto imutável de destinos legais; vazio se este estado for terminal
     */
    public Set<EstadoPartida> destinosLegais() {
        return TRANSICOES_LEGAIS.get(this);
    }
}
