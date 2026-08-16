package br.edu.uepb.map.cardgame.engine;

import java.util.Objects;

import br.edu.uepb.map.cardgame.api.EstadoPartida;
import br.edu.uepb.map.cardgame.api.excecao.EstadoDePartidaInvalidoException;

/**
 * Guarda o estado corrente da partida e recusa transições ilegais.
 *
 * <p>Esta classe <em>aplica</em> a regra de transição, mas não a <em>conhece</em>: a
 * tabela de estados sucessores mora em {@link EstadoPartida}, que é o especialista na
 * informação. Aqui fica apenas a decisão de o que fazer quando a transição é recusada.
 *
 * <p>Package-private de propósito: o ciclo de vida é detalhe interno do engine, e os
 * jogos clientes só enxergam o {@link EstadoPartida} corrente.
 *
 * @author Lucas N. de Araújo
 * @version 0.0.1
 */
final class CicloDeVidaDaPartida {

    private EstadoPartida estado = EstadoPartida.CONFIGURADA;

    /**
     * Estado corrente da partida.
     *
     * @return estado corrente, nunca {@code null}
     */
    EstadoPartida estado() {
        return estado;
    }

    /**
     * Migra para o estado informado, se a transição for legal.
     *
     * @param destino estado de destino pretendido
     * @throws EstadoDePartidaInvalidoException se a transição não constar da tabela de
     *         {@link EstadoPartida}
     * @throws NullPointerException se {@code destino} for {@code null}
     */
    void transicionarPara(EstadoPartida destino) {
        Objects.requireNonNull(destino, "O estado de destino não pode ser nulo.");
        if (!estado.podeTransitarPara(destino)) {
            throw new EstadoDePartidaInvalidoException(
                    "Transição ilegal de " + estado + " para " + destino
                            + ". Destinos legais: " + estado.destinosLegais() + ".");
        }
        estado = destino;
    }

    /**
     * Exige que a partida esteja exatamente no estado informado.
     *
     * @param esperado estado exigido pela operação
     * @throws EstadoDePartidaInvalidoException se o estado corrente for outro
     */
    void exigir(EstadoPartida esperado) {
        if (estado != esperado) {
            throw new EstadoDePartidaInvalidoException(
                    "A operação exige o estado " + esperado + ", mas a partida está em " + estado + ".");
        }
    }
}
