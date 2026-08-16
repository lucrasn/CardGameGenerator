package br.edu.uepb.map.cardgame.api;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Estados pelos quais uma partida passa, do momento em que é configurada até o
 * encerramento, junto com as transições consideradas legais entre eles.
 *
 * <p>O conhecimento sobre <em>qual estado pode suceder qual</em> mora aqui dentro, e
 * não em quem controla o ciclo de vida: é o princípio GRASP de
 * <strong>Especialista na Informação</strong> aplicado a um tipo de valor. O engine
 * guarda apenas o estado corrente e consulta este enum antes de mudar; a regra de
 * transição em si pertence ao próprio estado.
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
 * CONFIGURADA  → PREPARANDO
 * PREPARANDO   → EM_ANDAMENTO
 * EM_ANDAMENTO → FINALIZADA
 * FINALIZADA   → (terminal)
 * </pre>
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
public enum EstadoPartida {

    /** Configuração aceita; a execução ainda não começou. Estado inicial. */
    CONFIGURADA,

    /** Baralho, mãos e distribuição inicial estão sendo preparados. */
    PREPARANDO,

    /** Turnos podem ser executados e regras podem encerrar a partida. */
    EM_ANDAMENTO,

    /** Resultado criado; nenhuma mutação de cartas ou turnos é permitida. Terminal. */
    FINALIZADA;

    /**
     * Tabela de transições legais.
     *
     * <p>Precisa ser montada em bloco {@code static}, e não no construtor do enum:
     * durante a construção das constantes elas ainda não estão todas inicializadas, e
     * referenciar umas às outras ali dentro resultaria em {@code null}.
     */
    private static final Map<EstadoPartida, Set<EstadoPartida>> TRANSICOES_LEGAIS;

    static {
        Map<EstadoPartida, Set<EstadoPartida>> tabela = new EnumMap<>(EstadoPartida.class);
        tabela.put(CONFIGURADA, EnumSet.of(PREPARANDO));
        tabela.put(PREPARANDO, EnumSet.of(EM_ANDAMENTO));
        tabela.put(EM_ANDAMENTO, EnumSet.of(FINALIZADA));
        tabela.put(FINALIZADA, EnumSet.noneOf(EstadoPartida.class));

        // Congela cada conjunto individualmente antes de congelar a tabela: sem isso um
        // cliente conseguiria alterar as regras de transição por fora (requisito 7).
        tabela.replaceAll((estado, destinos) -> Collections.unmodifiableSet(destinos));
        TRANSICOES_LEGAIS = Collections.unmodifiableMap(tabela);
    }

    /**
     * Indica se a partida pode migrar deste estado para o estado informado.
     *
     * @param destino estado de destino pretendido
     * @return {@code true} se a transição é legal, {@code false} caso contrário
     * @throws NullPointerException se {@code destino} for {@code null}
     */
    public boolean podeTransitarPara(EstadoPartida destino) {
        Objects.requireNonNull(destino, "O estado de destino não pode ser nulo.");
        return TRANSICOES_LEGAIS.get(this).contains(destino);
    }

    /**
     * Indica se este é o estado terminal, do qual a partida não sai mais.
     *
     * @return {@code true} somente para {@link #FINALIZADA}
     */
    public boolean ehTerminal() {
        return TRANSICOES_LEGAIS.get(this).isEmpty();
    }

    /**
     * Estados alcançáveis a partir deste.
     *
     * @return conjunto imutável de destinos legais; vazio se este estado for terminal
     */
    public Set<EstadoPartida> destinosLegais() {
        return TRANSICOES_LEGAIS.get(this);
    }
}
