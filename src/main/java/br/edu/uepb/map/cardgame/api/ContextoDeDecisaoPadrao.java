package br.edu.uepb.map.cardgame.api;

import java.util.List;
import java.util.Objects;

/**
 * Implementação reutilizável e imutável de {@link ContextoDeDecisao}.
 *
 * <p>O contexto mantém apenas a etapa atual e um snapshot das ações permitidas. Por
 * estar na API pública, pode ser usado por jogos clientes sem depender de detalhes
 * internos de {@code core} ou {@code engine}. Jogos que precisem fornecer outras
 * informações públicas podem implementar uma subinterface especializada de
 * {@code ContextoDeDecisao}.
 *
 * @param etapa etapa atual do turno
 * @param jogadasPermitidas snapshot das ações disponíveis
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 */
public record ContextoDeDecisaoPadrao(
        EtapaDeTurno etapa,
        List<Jogada> jogadasPermitidas
) implements ContextoDeDecisao {

    /**
     * Cria um contexto por cópia defensiva.
     *
     * @param etapa etapa atual do turno
     * @param jogadasPermitidas ações disponíveis, sem elementos nulos
     * @throws NullPointerException se a etapa, a lista ou algum elemento for nulo
     */
    public ContextoDeDecisaoPadrao {
        etapa = Objects.requireNonNull(etapa, "etapa");
        jogadasPermitidas = List.copyOf(Objects.requireNonNull(
                jogadasPermitidas,
                "jogadasPermitidas"
        ));
    }
}
