package br.edu.uepb.map.cardgame.api;

import java.util.List;

/**
 * Visão mínima e somente leitura oferecida a uma estratégia de decisão.
 *
 * <p><strong>Porta de contexto da Strategy.</strong> Jogos podem fornecer
 * subinterfaces com informações públicas adicionais. O contrato base nunca revela
 * mãos adversárias, a ordem do baralho ou estado interno mutável.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public interface ContextoDeDecisao {

    /**
     * Etapa do turno em que a decisão será tomada.
     *
     * <p>O tipo concreto pertence ao jogo cliente. O framework conhece apenas esta
     * abstração e, portanto, não incorpora fases específicas como comprar, descartar,
     * pedir carta ou parar.
     *
     * @return etapa atual, nunca {@code null}
     */
    EtapaDeTurno etapa();

    /**
     * Ações válidas para a etapa atual do turno.
     *
     * @return lista imutável, sem elementos nulos
     */
    List<Jogada> jogadasPermitidas();
}
