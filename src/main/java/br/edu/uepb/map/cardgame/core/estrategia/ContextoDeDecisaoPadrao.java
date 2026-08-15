package br.edu.uepb.map.cardgame.core.estrategia;

import br.edu.uepb.map.cardgame.api.ContextoDeDecisao;
import br.edu.uepb.map.cardgame.api.EtapaDeTurno;
import br.edu.uepb.map.cardgame.api.Jogada;

import java.util.List;
import java.util.Objects;

/**
 * Snapshot interno das ações que podem ser escolhidas na etapa atual.
 *
 * <p>A lista recebida é copiada e exposta de forma imutável. O contexto não contém
 * mãos adversárias, ordem do baralho ou qualquer estrutura mutável da partida.
 *
 * @author Allan Guilherme da S. Vieira
 * @version 0.0.1
 * @since 2026-08-15
 */
public record ContextoDeDecisaoPadrao(
        EtapaDeTurno etapa,
        List<Jogada> jogadasPermitidas
)
        implements ContextoDeDecisao {

    /**
     * Cria um contexto por cópia defensiva.
     *
     * @param etapa etapa atual do turno
     * @param jogadasPermitidas ações disponíveis
     */
    public ContextoDeDecisaoPadrao {
        etapa = Objects.requireNonNull(etapa, "etapa");
        jogadasPermitidas = List.copyOf(jogadasPermitidas);
    }
}
